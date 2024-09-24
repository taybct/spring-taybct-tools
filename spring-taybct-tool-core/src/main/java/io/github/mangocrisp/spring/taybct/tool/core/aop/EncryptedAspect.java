package io.github.mangocrisp.spring.taybct.tool.core.aop;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import io.github.mangocrisp.spring.taybct.tool.core.annotation.Encrypted;
import io.github.mangocrisp.spring.taybct.tool.core.dto.EncryptedDTO;
import io.github.mangocrisp.spring.taybct.tool.core.enums.EncryptedType;
import io.github.mangocrisp.spring.taybct.tool.core.result.R;
import io.github.mangocrisp.spring.taybct.tool.core.util.AOPUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.ServletUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.rsa.RSACoder;
import io.github.mangocrisp.spring.taybct.tool.core.util.sm.SM2Coder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Objects;
import java.util.UUID;

/**
 * EncryptedAspect 切面
 *
 * @author xijieyin <br> 2022/8/5 13:39
 * @see Encrypted
 * @since 1.0.0
 */
@Aspect
@AutoConfiguration
@Slf4j
public class EncryptedAspect {

    /**
     * @param point 切点
     */
    @Around("@annotation(encrypted)")
    public Object doAround(ProceedingJoinPoint point, Encrypted encrypted) throws Throwable {
        log.debug("加密传输处理开始======");
        // 请求类型 GET/POST
        String requestMethod = Objects.requireNonNull(ServletUtil.getRequest()).getMethod();
        if (requestMethod.equalsIgnoreCase(RequestMethod.GET.name())) {
            return getMethod(point, encrypted);
        } else if (requestMethod.equalsIgnoreCase(RequestMethod.POST.name()) ||
                requestMethod.equalsIgnoreCase(RequestMethod.PUT.name()) ||
                requestMethod.equalsIgnoreCase(RequestMethod.PATCH.name())) {
            return postMethod(point, encrypted);
        }
        // 获取到所有的参数
        Object[] args = point.getArgs();
        return point.proceed(args);

    }

    private static Object postMethod(ProceedingJoinPoint point, Encrypted encrypted) throws Throwable {
        // 获取到所有的参数
        Object[] args = point.getArgs();
        // 数据对象
        JSONObject data = null;
        // 如果传进来的内容是加密的
        int index = AOPUtil.getParamIndex(point, encrypted.key());
        // 如果需要输入转换
        if (index > -1 && index < point.getArgs().length) {
            if (args[index] instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) args[index];
                data = jsonObject;
            } else if (args[index] instanceof EncryptedDTO) {
                EncryptedDTO dto = (EncryptedDTO) args[index];
                data = JSONObject.from(dto, JSONWriter.Feature.WriteMapNullValue);
            }
        }
        if (encrypted.decryptInput()) {
            if (data != null && data.getString(encrypted.content()) != null) {
                if (encrypted.type().equals(EncryptedType.RSA)) {
                    data.put(encrypted.content(), rsaDecode(data.getString(encrypted.content())));
                } else if (data.getString(encrypted.aesKey()) != null) {
                    // RSA 解密成明文的 AES Key
                    String serveAesKeyStr = rsaDecode(data.getString(encrypted.aesKey()));
                    data.put(encrypted.aesKey(), serveAesKeyStr);
                    data.put(encrypted.content()
                            , aesDecode(serveAesKeyStr, data.getString(encrypted.content())));
                }
            }
        }
        if (data != null) {
            args[index] = data;
        }
        Object proceed = point.proceed(args);
        if (encrypted.encryptOutput()) {
            if (data != null && data.getString(encrypted.outputEncryptPublicKey()) != null) {
                return encryptOutput(proceed, encrypted, data.getString(encrypted.outputEncryptPublicKey()));
            }
        }
        return proceed;
    }

    private static Object getMethod(ProceedingJoinPoint point, Encrypted encrypted) throws Throwable {
        // 获取到所有的参数
        Object[] args = point.getArgs();
        if (encrypted.decryptInput()) {
            // 如果传进来的内容是加密的

            if (encrypted.type().equals(EncryptedType.RSA)) {
                int index = AOPUtil.getParamIndex(point, encrypted.key());
                // 如果需要输入转换
                if (index > -1 &&
                        index < point.getArgs().length &&
                        args[index] instanceof String) {
                    args[index] = rsaDecode((String) args[index]);
                }
            } else {
                int aesKeyIndex = AOPUtil.getParamIndex(point, encrypted.aesKey());
                int contentIndex = AOPUtil.getParamIndex(point, encrypted.content());
                // 如果需要输入转换
                if (aesKeyIndex > -1 &&
                        aesKeyIndex < point.getArgs().length &&
                        args[aesKeyIndex] instanceof String) {
                    if (contentIndex > -1 &&
                            contentIndex < point.getArgs().length &&
                            args[contentIndex] instanceof String) {
                        // RSA 解密成明文的 AES Key
                        String serveAesKeyStr = rsaDecode((String) args[aesKeyIndex]);
                        args[aesKeyIndex] = serveAesKeyStr;
                        args[contentIndex] = aesDecode(serveAesKeyStr, (String) args[contentIndex]);
                    }
                }
            }
        }
        Object proceed = point.proceed(args);
        if (encrypted.encryptOutput()) {
            int publicKeyIndex = AOPUtil.getParamIndex(point, encrypted.outputEncryptPublicKey());
            if (publicKeyIndex > -1 &&
                    publicKeyIndex < point.getArgs().length &&
                    args[publicKeyIndex] instanceof String) {
                if (args[publicKeyIndex] != null) {
                    return encryptOutput(proceed, encrypted, (String) args[publicKeyIndex]);
                }
            }
        }
        return proceed;
    }

    /**
     * 加密输出
     *
     * @param proceed      返回结果
     * @param encrypted    加密注解
     * @param rsaPublicKey 前端传过来的 RSA 公钥
     * @return 根据注释返回加密或者不加密的数据
     * @throws Throwable 报异常
     */
    private static Object encryptOutput(Object proceed, Encrypted encrypted, String rsaPublicKey) throws Throwable {

        // 如果需要传出内容加密
        if (proceed instanceof R) {
            // 不管返回结果是什么，只要是能加密的数据，这里都统一返回 JSONObject，content 是加密内容，如果有 AES 加密，这里 aesKey 就是 AES Key
            JSONObject data = new JSONObject();
            // 需要加密的内容
            String content;
            if (((R) proceed).getData() instanceof String) {
                R<String> r = (R<String>) proceed;
                content = r.getData();
            } else if (((R) proceed).getData() != null) {
                R<Object> r = (R<Object>) proceed;
                content = JSONObject.toJSONString(r.getData());
            } else {
                return proceed;
            }
            if (encrypted.type().equals(EncryptedType.RSA)) {
                data.put(encrypted.content(), encodeRSAy(rsaPublicKey, content));
            } else {
                String aesKeyStr = getAesKeyStr();
                String encryptHex = encodeAES(aesKeyStr, content);
                data.put(encrypted.content(), encryptHex);
                data.put(encrypted.aesKey(), encodeRSAy(rsaPublicKey, aesKeyStr));
            }

            return R.status(((R) proceed).getCode(), ((R) proceed).getMessage(), data);
        }
        return proceed;
    }

    /**
     * AES 解密
     *
     * @param serveAesKeyStr RSA 解密后的 AES Key
     * @param content        需要解密的内容
     * @return 解密后的内容
     */
    private static String aesDecode(String serveAesKeyStr, String content) {
        SymmetricCrypto aes = new SymmetricCrypto(SymmetricAlgorithm.AES, serveAesKeyStr.getBytes());
        // AES 解密
        String decryptStr = aes.decryptStr(content, CharsetUtil.CHARSET_UTF_8);
        return decryptStr;
    }

    /**
     * RSA 解密
     *
     * @param content 需要解密的内容，也可以是 RSA 加密的 AES Key
     * @return 解密后的内容
     */
    private static String rsaDecode(String content) {
        if (content.startsWith("04")) {
            // 04 开头的 key 是 sm2 加密
            return SM2Coder.decryptWebData(content);
        }
        return RSACoder.decryptBase64StringByPrivateKey(content);
    }

    /**
     * 对称加密
     *
     * @param aesKeyStr aes key
     * @param content   内容
     * @return 加密后的内容
     */
    private static String encodeAES(String aesKeyStr, String content) {
        // 前端加密明文
        SymmetricCrypto webAES = new SymmetricCrypto(SymmetricAlgorithm.AES, aesKeyStr.getBytes());
        // 加密为16进制密文
        return webAES.encryptHex(content);
    }

    /**
     * 非对称加密
     *
     * @param rsaPublicKey 非对称公钥
     * @param content      内容
     * @return 加密后的 aes key
     */
    private static String encodeRSAy(String rsaPublicKey, String content) {
        if (rsaPublicKey.startsWith("04")) {
            // 04 开头的 key 是 sm2 加密
            return new SM2(null, rsaPublicKey).encryptHex(content, KeyType.PublicKey);
        }
        return RSACoder.encryptBase64StringByPublicKey(rsaPublicKey, content);
    }

    /**
     * 获取 AES key
     *
     * @return AES key
     */
    private static String getAesKeyStr() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
