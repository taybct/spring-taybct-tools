package io.github.taybct.tool.core.util.sm;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import io.github.taybct.tool.core.config.PropConfig;
import io.github.taybct.tool.core.util.ObjectUtil;
import io.github.taybct.tool.core.util.StringUtil;
import lombok.SneakyThrows;
import org.aspectj.util.FileUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.core.io.ClassPathResource;

import javax.crypto.KeyGenerator;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.function.Function;

/**
 * SM4 加解密工具类
 *
 * @author XiJieYin <br> 2024/5/9 10:54
 */
public class SM4Coder {

    public static final String KEY_PATH = "sm4-key-path";

    private static PropConfig prop;

    private static byte[] key;

    private static SymmetricCrypto sm4;

    private static final String ALGORITHM = "SM4CMAC";
    private static final String ALGORITHM_ECB_PKCS5PADDING = "SM4/ECB/PKCS5Padding";
    /**
     * PROVIDER
     */
    public final static String PROVIDER = "BC";
    /**
     * SM4算法目前只支持128位（即密钥16字节）
     */
    private static final int DEFAULT_KEY_SIZE = 128;
    /**
     * 证书路径
     */
    public final static String CER_PATH = "./";
    /**
     * 密钥名
     */
    public final static String PRIVATE_KEY_NAME = "sm4.key";

    static {
        // BouncyCastle算法提供者
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 初始化参数配置
     *
     * @param prop 全局配置
     */
    public static void ini(PropConfig prop) {
        if (ObjectUtil.isEmpty(SM4Coder.prop)) {
            SM4Coder.prop = prop;
        }
    }

    public static byte[] getKey() throws IOException, NoSuchAlgorithmException, NoSuchProviderException {
        if (ArrayUtil.isEmpty(SM4Coder.key)) {
            byte[] key = null;
            if (prop != null) {
                String keyPath = (String) prop.get(SM4Coder.KEY_PATH);
                if (StringUtil.isNotBlank(keyPath)) {
                    key = getSM4SecretKey(new FileInputStream(keyPath));
                }
            }
            if (ArrayUtil.isEmpty(key)) {
                key = getSM4SecretKey();
            }
            SM4Coder.key = key;
        }
        return SM4Coder.key;
    }

    public static SymmetricCrypto getSM4() throws IOException, NoSuchAlgorithmException, NoSuchProviderException {
        if (ObjectUtil.isEmpty(SM4Coder.sm4)) {
            SM4Coder.sm4 = new SymmetricCrypto(ALGORITHM_ECB_PKCS5PADDING, SM4Coder.getKey());
        }
        return SM4Coder.sm4;
    }

    public static byte[] generateKey() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM, PROVIDER);
        kg.init(DEFAULT_KEY_SIZE, new SecureRandom());
        return kg.generateKey().getEncoded();
    }

    public static void genSM4SecretKey() throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
        genSM4SecretKey(CER_PATH + PRIVATE_KEY_NAME);
    }

    public static void genSM4SecretKey(String path) throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
        writeFile(generateKey(), path);
    }

    public static byte[] getSM4SecretKey() throws IOException, NoSuchAlgorithmException, NoSuchProviderException {
        ClassPathResource resource = new ClassPathResource(CER_PATH + PRIVATE_KEY_NAME);
        if (resource.exists()) {
            return getSM4SecretKey(resource.getInputStream());
        }
        return generateKey();
    }

    public static byte[] getSM4SecretKey(InputStream inputStream) throws IOException {
        return FileUtil.readAsByteArray(inputStream);
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
     * 加密
     */
    public static class En implements Function<String, String> {

        @SneakyThrows
        @Override
        public String apply(String s) {
            return SM4Coder.getSM4().encryptBase64(s, StandardCharsets.UTF_8);
        }
    }

    /**
     * 解密
     */
    public static class De implements Function<String, String> {

        @SneakyThrows
        @Override
        public String apply(String s) {
            return SM4Coder.getSM4().decryptStr(s, StandardCharsets.UTF_8);
        }
    }
}
