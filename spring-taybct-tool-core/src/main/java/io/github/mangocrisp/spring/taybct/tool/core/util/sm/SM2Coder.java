package io.github.mangocrisp.spring.taybct.tool.core.util.sm;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import io.github.mangocrisp.spring.taybct.tool.core.exception.def.BaseException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.util.FileUtil;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.X509KeyUsage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveGenParameterSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * SM2加密工具
 *
 * @author XiJieYin <br> 2024/4/29 11:22
 */
@Slf4j
public class SM2Coder {
    /**
     * 国密类型的数字证书使用的签名算法
     */
    public final static String SIGNATURE_ALGORITHM = "SM3withSM2";
    /**
     * 证书类型
     */
    public final static String CERTIFICATE_TYPE = "X.509";
    /**
     * ALGORITHM
     */
    public final static String ALGORITHM = "EC";
    /**
     * PROVIDER
     */
    public final static String PROVIDER = "BC";
    /**
     * SM2默认曲线
     */
    public static final String SM2_CURVE_NAME = "sm2p256v1";
    /**
     * KeyStore 类型
     */
    public static final String PKCS12 = "PKCS12";
    /**
     * 证书路径
     */
    public final static String CER_PATH = "./";
    /**
     * 私钥名
     */
    public final static String PRIVATE_KEY_NAME = "sm2.key";
    /**
     * 证书名
     */
    public final static String CER_NAME = "sm2.cer";
    /**
     * 私钥对名
     */
    public final static String KEY_STORE_NAME = "sm2.jks";

    /**
     * 加密配置
     */
    private static SM2Properties rsaProperties;
    /**
     * huTool 工具库的 SM2 工具
     */
    private static SM2 sm2;

    private final static Map<String, KeyPair> keyPair = new ConcurrentHashMap<>();

    static {
        // BouncyCastle算法提供者
        Security.addProvider(new BouncyCastleProvider());
        BouncyCastleProvider bc = new BouncyCastleProvider();
        Set<Provider.Service> services = bc.getServices();
        for (Provider.Service s : services) {
            if (s.toString().toUpperCase().contains("CIPHER")) log.trace(s.toString());
        }
    }

    /**
     * 密钥对类型
     */
    public interface KeypairType {
        /**
         * 固定的密钥对
         */
        String FIXED = "FIXED";
    }

    /**
     * 初始化配置参数
     *
     * @param properties 配置
     * @author xijieyin <br> 2022/10/12 14:39
     * @since 1.0.5
     */
    public static void ini(SM2Properties properties) throws InvalidCipherTextException, InvalidKeySpecException, NoSuchAlgorithmException {
        if (rsaProperties != null) {
            return;
        }
        rsaProperties = properties;
        // 初始化的时候加密一次检查是否有效
        encryptBase64StringByPublicKey(System.currentTimeMillis() + "");
        // 初始化 sm2
        sm2 = new SM2(keyPair().getPrivate(), keyPair().getPublic());
    }

    /**
     * 解密 base64 字符串
     *
     * @param str base64 字符串
     * @return 解密之后数据
     */
    public static byte[] decryptBASE64(String str) {
        return Base64.decode(str);
    }

    /**
     * base64 加密成字符串
     *
     * @param bytes 需要加密的数据
     * @return base64 字符串
     */
    public static String encryptBASE64(byte[] bytes) {
        return Base64.toBase64String(bytes);
    }

    /**
     * 用私钥对信息生成数字签名
     *
     * @param data       待签名的内容
     * @param privateKey 私钥（base64 加密）
     * @return 签名结果
     */
    public static String sign(String data, String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        return sign(data.getBytes(StandardCharsets.UTF_8), getPrivateKey(privateKey));
    }

    /**
     * 用私钥对信息生成数字签名
     *
     * @param data       待签名的内容
     * @param privateKey 私钥（base64 加密）
     * @return 签名结果
     */
    public static String sign(byte[] data, String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        return sign(data, getPrivateKey(privateKey));
    }

    /**
     * 用私钥对信息生成数字签名
     *
     * @param data       待签名的内容
     * @param privateKey 私钥
     * @return 签名结果
     */
    public static String sign(byte[] data, PrivateKey privateKey) throws SignatureException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        // 用私钥对信息生成数字签名
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM, PROVIDER);
        signature.initSign(privateKey);
        signature.update(data);
        return encryptBASE64(signature.sign());
    }

    /**
     * 获取私钥
     *
     * @param privateKey 私钥（base64 加密）
     * @return 私钥
     */
    public static PrivateKey strToPrivateKey(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return getPrivateKey(privateKey);
    }

    /**
     * 校验数字签名
     *
     * @param data      需要验证的数据
     * @param publicKey 公钥（base64加密）
     * @param sign      签名值
     * @return 校验结果
     */
    public static boolean verify(String data, String publicKey, String sign) throws InvalidKeySpecException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException {
        return verify(data.getBytes(StandardCharsets.UTF_8), publicKey, sign);
    }

    /**
     * 校验数字签名
     *
     * @param data      需要验证的数据
     * @param publicKey 公钥（base64加密）
     * @param sign      签名值
     * @return 校验结果
     */
    public static boolean verify(byte[] data, String publicKey, String sign) throws InvalidKeySpecException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException {
        return verify(data, getPublicKey(publicKey), sign);
    }

    /**
     * 校验数字签名
     *
     * @param data      需要验证的数据
     * @param publicKey 公钥
     * @param sign      签名值
     * @return 校验结果
     */
    public static boolean verify(byte[] data, PublicKey publicKey, String sign) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException {
        // 取公钥匙对象
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM, PROVIDER);
        signature.initVerify(publicKey);
        signature.update(data);
        // 验证签名是否正常
        return signature.verify(decryptBASE64(sign));
    }


    /**
     * 使用私钥解密
     *
     * @param data       需要解密的数据
     * @param privateKey 私钥（base64加密）
     * @return 解密结果
     */
    public static byte[] decryptByPrivateKey(String data, String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidCipherTextException {
        return decryptByPrivateKey(decryptBASE64(data), getPrivateKey(privateKey));
    }

    /**
     * 使用私钥解密
     *
     * @param data       需要解密的数据
     * @param privateKey 私钥
     * @return 解密结果
     */
    public static byte[] decryptByPrivateKey(byte[] data, PrivateKey privateKey) throws InvalidCipherTextException {
        //获取解密参数
        BCECPrivateKey bcecPrivateKey = (BCECPrivateKey) privateKey;
        ECParameterSpec ecParameterSpec = bcecPrivateKey.getParameters();
        ECDomainParameters ecDomainParameters = new ECDomainParameters(
                ecParameterSpec.getCurve(), ecParameterSpec.getG(),
                ecParameterSpec.getN());
        ECPrivateKeyParameters localECPrivateKeyParameters = new ECPrivateKeyParameters(
                bcecPrivateKey.getD(), ecDomainParameters);
        //初始化解密引擎
        SM2Engine sm2Engine = new SM2Engine();
        sm2Engine.init(false, localECPrivateKeyParameters);
        return sm2Engine.processBlock(data, 0, data.length);
    }

    /**
     * 使用公钥解密
     *
     * @param data      需要解密的数据
     * @param publicKey 私钥（base64加密）
     * @return 解密结果
     */
    public static byte[] decryptByPublicKey(String data, String publicKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
        return decryptByPublicKey(decryptBASE64(data), getPublicKey(publicKey));
    }

    /**
     * 使用公钥解密
     *
     * @param data      需要解密的数据
     * @param publicKey 私钥
     * @return 解密结果
     */
    public static byte[] decryptByPublicKey(byte[] data, PublicKey publicKey) {
        throw new RuntimeException("not support decrypt by public key!");
    }

    /**
     * 用公钥加密
     *
     * @param data      明文数据
     * @param publicKey 公钥（base64加密）
     * @return byte[]  密文数据
     */
    public static byte[] encryptByPublicKey(String data, String publicKey) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidCipherTextException {
        return encryptByPublicKey(data.getBytes(StandardCharsets.UTF_8), getPublicKey(publicKey));
    }

    /**
     * 用公钥加密
     *
     * @param data      明文数据
     * @param publicKey 公钥
     * @return byte[]  密文数据
     */
    public static byte[] encryptByPublicKey(byte[] data, PublicKey publicKey) throws InvalidCipherTextException {
        //获取加密参数
        BCECPublicKey bcecPublicKey = (BCECPublicKey) publicKey;
        ECParameterSpec ecParameterSpec = bcecPublicKey.getParameters();
        ECDomainParameters ecDomainParameters = new ECDomainParameters(
                ecParameterSpec.getCurve(), ecParameterSpec.getG(),
                ecParameterSpec.getN());
        ECPublicKeyParameters ecPublicKeyParameters = new ECPublicKeyParameters(bcecPublicKey.getQ(),
                ecDomainParameters);
        //初始化加密引擎
        SM2Engine sm2Engine = new SM2Engine();
        sm2Engine.init(true, new ParametersWithRandom(ecPublicKeyParameters));
        //加密
        return sm2Engine.processBlock(data, 0, data.length);
    }

    /**
     * 加密<br>
     * 用私钥加密
     *
     * @param data       明文数据
     * @param privateKey 私钥（base64加密）
     * @return byte[] 加密的数据
     */
    public static byte[] encryptByPrivateKey(String data, String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return encryptByPrivateKey(data.getBytes(StandardCharsets.UTF_8), getPrivateKey(privateKey));
    }

    /**
     * 用私钥加密
     *
     * @param data       明文数据
     * @param privateKey 私钥
     * @return byte[] 加密的数据
     */
    public static byte[] encryptByPrivateKey(byte[] data, PrivateKey privateKey) {
        throw new RuntimeException("not support encrypt by private key!");
    }

    /**
     * 获取秘钥对
     *
     * @return 秘钥对
     */
    public static KeyPair keyPair() {
        return keyPair(KeypairType.FIXED);
    }

    /**
     * 获取密钥对
     *
     * @param keypairType 密钥对类型
     * @return 密钥对
     * @since 3.1.0
     */
    public static KeyPair keyPair(String keypairType) {
        if (keyPair.get(keypairType) != null) {
            return keyPair.get(keypairType);
        }
        // 拿到配置的不同类型的证书
        SM2Properties properties = rsaProperties.getType().get(keypairType);
        if (properties == null) {
            properties = rsaProperties;
        }
        KeyPair key = newKeyPair(properties);
        keyPair.put(keypairType, key);
        return key;
    }

    /**
     * 新密钥对（非证书）
     *
     * @return KeyPair 密钥对
     * @author xijieyin <br> 2022/10/13 9:16
     * @since 1.0.5
     */
    public static KeyPair newKeyPair() {
        return newKeyPair(null);
    }

    /**
     * 新密钥对
     *
     * @param properties rsa 证书配置
     * @return KeyPair 密钥对
     * @author xijieyin <br> 2022/10/13 9:16
     * @since 1.0.5
     */
    public static KeyPair newKeyPair(SM2Properties properties) {
        return Optional.ofNullable(properties).map(p -> {
            ClassPathResource resource = new ClassPathResource(p.getResource());
            if (resource.exists()) {
                if (p.getExpireCheck()) {
                    try {
                        expireCheck(p.getResource(), resource.getInputStream(), p.getPassword());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    KeyStore keyStore = getKeyStore(resource.getInputStream(), p.getPassword());
                    return getKeyPair(keyStore, p.getAlias(), p.getPassword());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return generateKeyPair();
        }).orElseGet(SM2Coder::generateKeyPair);
    }

    /**
     * 生成密钥对
     *
     * @return 密钥对
     */
    private static KeyPair generateKeyPair() {
        KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM, PROVIDER);
            keyPairGenerator.initialize(new ECNamedCurveGenParameterSpec(SM2_CURVE_NAME));
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
        return keyPairGenerator.generateKeyPair();
    }


    /**
     * 获取到秘钥对
     *
     * @param keyStore 秘钥库
     * @return 秘钥对
     */
    public static KeyPair getKeyPair(KeyStore keyStore, String alias, String password) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
        return getKeyPair(getPrivateKey(keyStore, alias, password), getPublicKey(keyStore, alias));
    }

    /**
     * 获取到秘钥对
     *
     * @param privateKey 私钥
     * @param publicKey  公钥
     * @return 秘钥对
     */
    public static KeyPair getKeyPair(PrivateKey privateKey, PublicKey publicKey) {
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * 证书过期检查
     *
     * @param resource 证书路径
     * @param is       证书
     * @param password 密码
     */
    public static void expireCheck(String resource, InputStream is, String password) {
        try {
            KeyStore keyStore = getKeyStore(is, password);
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (keyStore.getCertificate(alias).getType().equals(CERTIFICATE_TYPE)) {
                    Date endDate = ((X509Certificate) keyStore.getCertificate(alias)).getNotAfter();
                    Date beginDate = ((X509Certificate) keyStore.getCertificate(alias)).getNotBefore();
                    if (endDate.before(new Date())) {
                        log.error("证书【{}】已过期，请及时更换！", resource);
                        throw new BaseException("证书已过期，请及时更换！");
                    }
                    if (beginDate.after(new Date())) {
                        log.error("证书【{}】不在可用时间范围内", resource);
                        throw new BaseException("证书不在可用时间范围内！");
                    }
                }
            }
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | NoSuchProviderException |
                 CertificateException e) {
            throw new BaseException("证书校验失败", e);
        }
    }

    /**
     * 用公钥加密
     *
     * @param publicKey 公钥
     * @param sourceStr 明文
     * @return String 密文
     * @author xijieyin <br> 2022/10/12 14:54
     * @since 1.0.5
     */
    @NotNull
    public static String encryptBase64StringByPublicKey(String publicKey, String sourceStr) throws InvalidCipherTextException, InvalidKeySpecException, NoSuchAlgorithmException {
        return encryptBASE64(SM2Coder.encryptByPublicKey(sourceStr, publicKey));
    }

    /**
     * 用公钥加密
     *
     * @param sourceStr 明文
     * @return String 密文
     * @author xijieyin <br> 2022/10/12 14:51
     * @since 1.0.5
     */
    public static String encryptBase64StringByPublicKey(String sourceStr) throws InvalidCipherTextException, InvalidKeySpecException, NoSuchAlgorithmException {
        return encryptBase64StringByPublicKey(getPublicKey(keyPair()), sourceStr);
    }

    /**
     * 用公钥加密
     *
     * @param sourceStr   明文
     * @param keypairType 密钥对类型
     * @return String 密文
     * @author xijieyin
     * @since 3.1.0
     */
    public static String encryptBase64StringByTypePublicKey(String sourceStr, String keypairType) throws InvalidCipherTextException, InvalidKeySpecException, NoSuchAlgorithmException {
        return encryptBase64StringByPublicKey(getPublicKey(keyPair(keypairType)), sourceStr);
    }

    /**
     * 用私钥解密
     *
     * @param encryptStr 密文
     * @param privateKey 私钥
     * @return String 明文
     * @author xijieyin <br> 2022/10/12 14:53
     * @since 1.0.5
     */
    public static String decryptBase64StringByPrivateKey(String encryptStr, String privateKey) throws InvalidCipherTextException, NoSuchAlgorithmException, InvalidKeySpecException {
        return new String(SM2Coder.decryptByPrivateKey(encryptStr, privateKey));
    }

    /**
     * 用私钥解密
     *
     * @param encryptStr 密文
     * @return String 明文
     * @author xijieyin <br> 2022/10/12 14:53
     * @since 1.0.5
     */
    public static String decryptBase64StringByPrivateKey(String encryptStr) throws InvalidCipherTextException, NoSuchAlgorithmException, InvalidKeySpecException {
        return decryptBase64StringByPrivateKey(encryptStr, getPrivateKey(keyPair()));
    }

    /**
     * 用私钥解密
     *
     * @param encryptStr  密文
     * @param keypairType 密钥对类型
     * @return String 明文
     * @author xijieyin
     * @since 3.1.0
     */
    public static String decryptBase64StringByTypePrivateKey(String encryptStr, String keypairType) throws InvalidCipherTextException, NoSuchAlgorithmException, InvalidKeySpecException {
        return decryptBase64StringByPrivateKey(encryptStr, getPrivateKey(keyPair(keypairType)));
    }

    /**
     * 使用者信息、颁发者信息，使用Distinct Name的方式来描述，用来确定“实体” 身份/信息 的一系列列键值对组成的字符串
     *
     * @return X500NameBuilder
     */
    public static X500NameBuilder createStdBuilder() {
        X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
        // 国家代码
        builder.addRDN(BCStyle.C, "CN");
        // 省份
        builder.addRDN(BCStyle.ST, "GuangDong");
        // 地区
        builder.addRDN(BCStyle.L, "GuanZhou");
        // 组织
        builder.addRDN(BCStyle.O, "TayBct");
        return builder;
    }

    /**
     * 生成公钥/私钥/私钥对
     *
     * @param alias     私钥对别名
     * @param password  私钥密码
     * @param expiresIn 超时（秒）
     * @param extension 扩展
     */
    public static void genSM2CACert(String alias
            , String password
            , long expiresIn
            , Consumer<X509v3CertificateBuilder> extension) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException, OperatorCreationException {
        X500Name build = SM2Coder.createStdBuilder().build();
        genSM2CACert(alias, password, build, build, expiresIn, extension, new String[]{CER_PATH + PRIVATE_KEY_NAME
                , CER_PATH + CER_NAME
                , CER_PATH + KEY_STORE_NAME});
    }

    /**
     * 生成公钥/私钥/私钥对
     *
     * @param alias     私钥对别名
     * @param password  私钥密码
     * @param issuer    颁发者信息
     * @param subject   订阅者信息
     * @param expiresIn 超时（秒）
     * @param extension 扩展
     * @param path      存储文件路径[私钥][公钥][私钥对]
     */
    public static void genSM2CACert(String alias
            , String password
            , X500Name issuer
            , X500Name subject
            , long expiresIn
            , Consumer<X509v3CertificateBuilder> extension
            , String[] path) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, InvalidKeyException, OperatorCreationException {
        log.info("开始生成CA证书");
        KeyPair keyPair = newKeyPair();

        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        log.info("CA 公钥:\r\n{}", encryptBASE64(publicKey.getEncoded()));
        log.info("CA 私钥:\r\n{}", encryptBASE64(privateKey.getEncoded()));

        // 这里使用私钥创建算法提供容器
        ContentSigner sigGen = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM)
                .setProvider(PROVIDER)
                .build(privateKey);

        // 构造X.509 第3版的证书构建者
        // 设置证书的基本数据：使用者信息、颁发者信息、证书序号、证书生效日期、证书失效日期，以及证书扩展属性
        X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
                // 颁发者信息
                issuer
                // 证书序列号
                , BigInteger.valueOf(System.currentTimeMillis())
                // 证书生效日期
                , new Date(System.currentTimeMillis() - 50 * 1000)
                // 证书失效日期
                , new Date(System.currentTimeMillis() + expiresIn * 1000)
                // 使用者信息（PS：由于是自签证书，所以颁发者和使用者DN都相同）
                , subject
                // 证书公钥
                , keyPair.getPublic())
                /*
                设置证书扩展
                证书扩展属性，请根据需求设定，参数请参考 《RFC 5280》
                 */
                // 设置密钥用法
                .addExtension(Extension.keyUsage, true, new X509KeyUsage(0xfe))
                // 设置扩展密钥用法
                .addExtension(Extension.extendedKeyUsage, true, new DERSequence(KeyPurposeId.anyExtendedKeyUsage));

        extension.accept(certificateBuilder);

        // 将证书构造参数装换为X.509证书对象
        X509Certificate certificate = new JcaX509CertificateConverter().setProvider(PROVIDER).getCertificate(certificateBuilder.build(sigGen));

        // 检查过期
        certificate.checkValidity(new Date());

        // 查检公钥
        certificate.verify(publicKey);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(certificate.getEncoded());
        CertificateFactory certificateFactory = CertificateFactory.getInstance(CERTIFICATE_TYPE, PROVIDER);

        certificate = (X509Certificate) certificateFactory.generateCertificate(byteArrayInputStream);
        // 保存私钥
        writeFile(privateKey.getEncoded(), path[0]);
        // 保存证书（公钥）
        writeFile(certificate.getEncoded(), path[1]);
        // 保存私钥对
        saveKeyStore(alias, password, path[2], privateKey, certificate);
    }

    /**
     * 保存为 keyStore 秘钥对
     *
     * @param alias       别名
     * @param password    密码
     * @param path        存在路径
     * @param privateKey  私钥
     * @param certificate 证书=
     */
    public static void saveKeyStore(String alias
            , String password
            , String path
            , PrivateKey privateKey
            , Certificate certificate) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException {
        KeyStore keyStore = KeyStore.getInstance(PKCS12, PROVIDER);
        keyStore.load(null, null);
        keyStore.setKeyEntry(alias, privateKey, password.toCharArray(), new Certificate[]{certificate});
        keyStore.store(new FileOutputStream(path), password.toCharArray());
    }

    /**
     * 检测证书是否过期
     *
     * @param cer 证书
     */
    public static void checkValidity(Certificate cer) throws CertificateNotYetValidException, CertificateExpiredException {
        if (cer instanceof X509Certificate x509Certificate) {
            x509Certificate.checkValidity(new Date());
        }
    }

    /**
     * 获取公钥
     *
     * @param alias    私钥对别名
     * @param password 密码
     * @return 私钥
     */
    public static PublicKey getPublicKey(String alias, String password) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException {
        Certificate cer = getCer(alias, password);
        return cer.getPublicKey();
    }

    /**
     * 获取公钥
     *
     * @param alias       私钥对别名
     * @param password    密码
     * @param inputStream 文件流
     * @return 私钥
     */
    public static PublicKey getPublicKey(String alias, String password, InputStream inputStream) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, NoSuchProviderException {
        Certificate cer = getCer(alias, password, inputStream);
        return cer.getPublicKey();
    }

    /**
     * 获取公钥
     *
     * @param keyStore 秘钥库
     * @param alias    别名
     * @return 公钥
     */
    public static PublicKey getPublicKey(KeyStore keyStore, String alias) throws KeyStoreException {
        return keyStore.getCertificate(alias).getPublicKey();
    }

    /**
     * 获取私钥
     *
     * @param alias    私钥对别名
     * @param password 密码
     * @return 私钥
     */
    public static PrivateKey getPrivateKey(String alias, String password) throws IOException, UnrecoverableKeyException, CertificateException, KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException {
        ClassPathResource resource = new ClassPathResource(CER_PATH + KEY_STORE_NAME);
        return getPrivateKey(alias, password, resource.getInputStream());
    }

    /**
     * 获取私钥
     *
     * @param alias       私钥对别名
     * @param password    密码
     * @param inputStream 文件流
     * @return 私钥
     */
    public static PrivateKey getPrivateKey(String alias, String password, InputStream inputStream) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, NoSuchProviderException {
        KeyStore keyStore = getKeyStore(inputStream, password);
        return getPrivateKey(keyStore, alias, password);
    }

    /**
     * 获取私钥
     *
     * @param keyStore 秘钥库
     * @param alias    别名
     * @param password 密码
     * @return 私钥
     */
    public static PrivateKey getPrivateKey(KeyStore keyStore, String alias, String password) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        return (PrivateKey) keyStore.getKey(alias, password.toCharArray());
    }

    /**
     * 获取到公钥证书
     *
     * @param alias    别名
     * @param password 密码
     * @return 证书
     */
    public static Certificate getCer(String alias, String password) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException {
        ClassPathResource resource = new ClassPathResource(CER_PATH + KEY_STORE_NAME);
        return getCer(alias, password, resource.getInputStream());
    }

    /**
     * 获取到公钥证书
     *
     * @param alias       别名
     * @param password    密码
     * @param inputStream 文件流
     * @return 证书
     */
    public static Certificate getCer(String alias, String password, InputStream inputStream) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, NoSuchProviderException {
        return getKeyStore(inputStream, password).getCertificate(alias);
    }

    /**
     * 获取到秘钥对
     *
     * @param inputStream 文件流
     * @param password    密码
     * @return 私钥对
     */
    public static KeyStore getKeyStore(InputStream inputStream, String password) throws KeyStoreException, NoSuchProviderException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance(PKCS12, PROVIDER);
        keyStore.load(inputStream, password.toCharArray());
        return keyStore;
    }

    /**
     * 获取公钥
     *
     * @return 公钥
     */
    public static PublicKey getPublic() throws CertificateException, IOException, NoSuchProviderException {
        return getCer().getPublicKey();
    }

    /**
     * 获得公钥
     *
     * @param keyPair 密钥对
     * @return String 转成base64的字符串
     */
    public static String getPublicKey(KeyPair keyPair) {
        return encryptBASE64(keyPair.getPublic().getEncoded());
    }

    /**
     * 获取公钥
     *
     * @param publicKey base64 加密后的公钥
     * @return 公钥
     */
    public static PublicKey getPublicKey(String publicKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
        // 解密由base64编码的公钥
        byte[] keyBytes = decryptBASE64(publicKey);
        // 取私钥匙对象
        return getPublicKey(keyBytes);
    }

    /**
     * 获取公钥
     *
     * @param bytes 公钥 byte 数组
     * @return 公钥
     */
    public static PublicKey getPublicKey(byte[] bytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(bytes);
        return keyFactory.generatePublic(x509KeySpec);
    }

    /**
     * 获取证书(公钥)
     *
     * @return 证书
     */
    public static Certificate getCer() throws IOException, CertificateException, NoSuchProviderException {
        ClassPathResource resource = new ClassPathResource(CER_PATH + CER_NAME);
        return getCer(resource.getInputStream());
    }

    /**
     * 获取证书(公钥)
     *
     * @param inputStream 证书文件输入流
     * @return 证书
     */
    public static Certificate getCer(InputStream inputStream) throws CertificateException, NoSuchProviderException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance(CERTIFICATE_TYPE, PROVIDER);
        return certificateFactory.generateCertificate(inputStream);
    }

    /**
     * 获得私钥
     *
     * @param keyPair 密钥对
     * @return String  转成 base64 的字符串
     */
    public static String getPrivateKey(KeyPair keyPair) {
        return encryptBASE64(keyPair.getPrivate().getEncoded());
    }

    /**
     * 获取私钥
     *
     * @return 私钥
     */
    public static PrivateKey getPrivate() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        ClassPathResource resource = new ClassPathResource(CER_PATH + PRIVATE_KEY_NAME);
        InputStream inputStream = resource.getInputStream();
        byte[] bytes = FileUtil.readAsByteArray(inputStream);
        return getPrivateKey(bytes);
    }

    /**
     * 获取私钥
     */
    public static PrivateKey getPrivateKey(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // 解密由base64编码的私钥
        byte[] keyBytes = decryptBASE64(privateKey);
        // 取私钥匙对象
        return getPrivateKey(keyBytes);
    }

    /**
     * 获取私钥
     *
     * @param bytes 私钥输入流
     * @return 私钥
     */
    public static PrivateKey getPrivateKey(byte[] bytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(bytes);
        return keyFactory.generatePrivate(pkcs8EncodedKeySpec);
    }

    /**
     * 写 byte 数组为文件
     *
     * @param data 数据
     * @param path 路径
     */
    public static void writeFile(byte[] data, String path) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(path);
        fileOutputStream.write(data);
        fileOutputStream.flush();
        fileOutputStream.close();
    }


    /**
     * 获取前端需要的公钥
     *
     * @return 加密 sm-crypto 工具需要用来加密的公钥
     */
    public static String getWebPublicKey() {
        ECPoint ecPoint = getEcPoint();
        // 公钥前面的02或者03表示是压缩公钥,04表示未压缩公钥,04的时候,可以去掉前面的04
        return Hex.toHexString(ecPoint.getEncoded(false));
    }

    /**
     * 获取到 EC 点
     *
     * @return 点
     */
    private static ECPoint getEcPoint() {
        PublicKey publicKey = keyPair().getPublic();
        BCECPublicKey bcecPublicKey = (BCECPublicKey) publicKey;
        return bcecPublicKey.getQ();
    }

    /**
     * 获取前端解密私钥
     *
     * @return 私钥
     */
    public static String getWebPrivateKey() {
        PrivateKey privateKey = keyPair().getPrivate();
        BCECPrivateKey bcecPrivateKey = (BCECPrivateKey) privateKey;
        BigInteger intPrivateKey = bcecPrivateKey.getD();
        return intPrivateKey.toString(16);
    }

    /**
     * 根据16进制公钥字符串获取公钥
     * @param pubKeyHex 16进制加密公钥字符串
     * @return 公钥
     */
    public static BCECPublicKey getECPublicKeyByPublicKeyHex(String pubKeyHex) {
        if (pubKeyHex.length() > 128) {
            pubKeyHex = pubKeyHex.substring(pubKeyHex.length() - 128);
        }
        String stringX = pubKeyHex.substring(0, 64);
        String stringY = pubKeyHex.substring(stringX.length());
        BigInteger x = new BigInteger(stringX, 16);
        BigInteger y = new BigInteger(stringY, 16);
        X9ECParameters x9ECParameters = GMNamedCurves.getByName(SM2_CURVE_NAME);
        ECParameterSpec ecDomainParameters = new ECParameterSpec(x9ECParameters.getCurve(), x9ECParameters.getG(), x9ECParameters.getN());
        ECPublicKeySpec ecPublicKeySpec = new ECPublicKeySpec(x9ECParameters.getCurve().createPoint(x, y), ecDomainParameters);
        return new BCECPublicKey(ALGORITHM, ecPublicKeySpec, BouncyCastleProvider.CONFIGURATION);
    }

    /**
     * 根据16进制公钥字符串获取私钥
     * @param privateKeyHex 16进制加密私钥字符串
     * @return 私钥
     */
    public static BCECPrivateKey getECPrivateKeyByPrivateKeyHex(String privateKeyHex) {
        BigInteger d = new BigInteger(privateKeyHex, 16);
        X9ECParameters x9ECParameters = GMNamedCurves.getByName(SM2_CURVE_NAME);
        ECParameterSpec ecDomainParameters = new ECParameterSpec(x9ECParameters.getCurve(), x9ECParameters.getG(), x9ECParameters.getN());
        ECPrivateKeySpec ecPrivateKeySpec = new ECPrivateKeySpec(d, ecDomainParameters);
        return new BCECPrivateKey(ALGORITHM, ecPrivateKeySpec, BouncyCastleProvider.CONFIGURATION);
    }

    /**
     * 加密数据给前端
     *
     * @param data 明文
     * @return 密文（会返回 04 开头的数据）
     */
    public static String encryptWebData(String data) {
        return sm2.encryptHex(data, KeyType.PublicKey);
    }

    /**
     * 解密前端传入的数据
     *
     * @param data 密文（需要 04 开头）
     * @return 明文
     */
    public static String decryptWebData(String data) {
        return sm2.decryptStr(data, KeyType.PrivateKey);
    }

}
