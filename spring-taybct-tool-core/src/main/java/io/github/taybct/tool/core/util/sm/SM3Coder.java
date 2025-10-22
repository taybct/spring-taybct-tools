package io.github.taybct.tool.core.util.sm;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.crypto.digest.SM3;
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
 * SM3 加密工具类
 *
 * @author XiJieYin <br> 2024/5/9 10:54
 */
public class SM3Coder {

    public static final String KEY_PATH = "sm3-key-path";

    private static PropConfig prop;

    private static byte[] key;

    private static SM3 sm3;

    private static final String ALGORITHM = "HmacSM3";
    /**
     * PROVIDER
     */
    public final static String PROVIDER = "BC";
    /**
     * 初始大小
     */
    private static final int DEFAULT_KEY_SIZE = 2048;
    /**
     * 证书路径
     */
    public final static String CER_PATH = "./";
    /**
     * 密钥名
     */
    public final static String PRIVATE_KEY_NAME = "sm3.key";

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
        if (ObjectUtil.isEmpty(SM3Coder.prop)) {
            SM3Coder.prop = prop;
        }
    }

    public static byte[] getKey() throws IOException, NoSuchAlgorithmException, NoSuchProviderException {
        if (ArrayUtil.isEmpty(SM3Coder.key)) {
            byte[] key = null;
            if (prop != null) {
                String keyPath = (String) prop.get(SM3Coder.KEY_PATH);
                if (StringUtil.isNotBlank(keyPath)) {
                    key = getSM3SecretKey(new FileInputStream(keyPath));
                }
            }
            if (ArrayUtil.isEmpty(key)) {
                key = getSM3SecretKey();
            }
            SM3Coder.key = key;
        }
        return SM3Coder.key;
    }

    public static SM3 getSM3() throws IOException, NoSuchAlgorithmException, NoSuchProviderException {
        if (ObjectUtil.isEmpty(SM3Coder.sm3)) {
            sm3 = new SM3(SM3Coder.getKey());
        }
        return SM3Coder.sm3;
    }

    public static byte[] generateKey() throws NoSuchAlgorithmException, NoSuchProviderException {
        return generateKey(DEFAULT_KEY_SIZE);
    }

    public static byte[] generateKey(int size) throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM, PROVIDER);
        kg.init(size, new SecureRandom());
        return kg.generateKey().getEncoded();
    }

    public static void genSM3SecretKey() throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
        genSM3SecretKey(CER_PATH + PRIVATE_KEY_NAME);
    }

    public static void genSM3SecretKey(String path) throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
        writeFile(generateKey(), path);
    }

    public static byte[] getSM3SecretKey() throws IOException, NoSuchAlgorithmException, NoSuchProviderException {
        ClassPathResource resource = new ClassPathResource(CER_PATH + PRIVATE_KEY_NAME);
        if (resource.exists()) {
            return getSM3SecretKey(resource.getInputStream());
        }
        return generateKey();
    }

    public static byte[] getSM3SecretKey(InputStream inputStream) throws IOException {
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
            return SM3Coder.getSM3().digestHex(s, StandardCharsets.UTF_8);
        }
    }


}
