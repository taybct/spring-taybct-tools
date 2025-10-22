package io.github.taybct.tool.core.util.rsa;

import io.github.taybct.tool.core.exception.def.BaseException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.utils.Base64;
import org.aspectj.util.FileUtil;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.X509KeyUsage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.core.io.ClassPathResource;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * RSA 工具类
 *
 * @author xijieyin <br> 2022/10/12 12:06
 * @since 1.0.5
 */
@Slf4j
public class RSACoder {
    public static final String KEY_ALGORITHM = "RSA";
    public static final String SIGNATURE_ALGORITHM = "MD5withRSA";

    /**
     * 对于 1024 长度的密钥，1024 / 4 也就是 128 然后 - 11 = 117 / 4 = 29.25 <br>
     * 但是 0<m<n 所以这里取了一个 floor 的值 29 <br>
     * 如果密钥是 256 也是同样的计算方式：(256-11) / 4 = 61
     */
    private static final int encryptSplitSize = 29;
    /**
     * 如果默认不用证书的加密，长度就是 172
     */
    private static int decryptSplitSize = 172;
    /**
     * 要拿一下加密的数据长度，这里直接生成一下先
     */
    private static final String testStr = "123";
    /**
     * 证书类型
     */
    public final static String CERTIFICATE_TYPE = "X.509";
    /**
     * PROVIDER
     */
    public final static String PROVIDER = "BC";
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
    public final static String PRIVATE_KEY_NAME = "rsa.key";
    /**
     * 证书名
     */
    public final static String CER_NAME = "rsa.cer";
    /**
     * 私钥对名
     */
    public final static String KEY_STORE_NAME = "rsa.jks";

    private static RSAProperties rsaProperties;

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
    public static void ini(RSAProperties properties) {
        if (rsaProperties != null) {
            return;
        }
        rsaProperties = properties;
        decryptSplitSize = encryptBase64StringByPublicKey(testStr).length();
    }

    public static byte[] decryptBASE64(String key) {
        return Base64.decodeBase64(key);
    }

    public static String encryptBASE64(byte[] bytes) {
        return Base64.encodeBase64String(bytes);
    }

    /**
     * 用私钥对信息生成数字签名
     */
    public static String sign(byte[] data, String privateKey) throws Exception {
        // 解密由base64编码的私钥
        byte[] keyBytes = decryptBASE64(privateKey);
        // 构造PKCS8EncodedKeySpec对象
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        // KEY_ALGORITHM 指定的加密算法
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        // 取私钥匙对象
        PrivateKey priKey = keyFactory.generatePrivate(pkcs8KeySpec);
        // 用私钥对信息生成数字签名
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(priKey);
        signature.update(data);
        return encryptBASE64(signature.sign());
    }

    /**
     * 获取私钥
     */
    public static PrivateKey strToPrivateKey(String privateKey) throws Exception {
        // 解密由base64编码的私钥
        byte[] keyBytes = decryptBASE64(privateKey);
        // 构造PKCS8EncodedKeySpec对象
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        // KEY_ALGORITHM 指定的加密算法
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        // 取私钥匙对象
        return keyFactory.generatePrivate(pkcs8KeySpec);
    }

    /**
     * 校验数字签名
     */
    public static boolean verify(byte[] data, String publicKey, String sign) throws Exception {
        // 解密由base64编码的公钥
        byte[] keyBytes = decryptBASE64(publicKey);
        // 构造X509EncodedKeySpec对象
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        // KEY_ALGORITHM 指定的加密算法
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        // 取公钥匙对象
        PublicKey pubKey = keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(pubKey);
        signature.update(data);
        // 验证签名是否正常
        return signature.verify(decryptBASE64(sign));
    }

    public static byte[] decryptByPrivateKey(byte[] data, String key) throws Exception {
        // 对密钥解密
        byte[] keyBytes = decryptBASE64(key);
        // 取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        // 对数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    /**
     * 解密<br>
     * 用私钥解密
     */
    public static byte[] decryptByPrivateKey(String data, String key) throws Exception {
        return decryptByPrivateKey(decryptBASE64(data), key);
    }

    /**
     * 解密<br>
     * 用公钥解密
     */
    public static byte[] decryptByPublicKey(byte[] data, String key) throws Exception {
        // 对密钥解密
        byte[] keyBytes = decryptBASE64(key);
        // 取得公钥
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicKey = keyFactory.generatePublic(x509KeySpec);
        // 对数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    /**
     * 加密<br>
     * 用公钥加密
     *
     * @param data 明文数据
     * @param key  公钥
     * @return byte[]  密文数据
     * @author xijieyin <br> 2022/10/12 14:56
     * @since 1.0.5
     */
    public static byte[] encryptByPublicKey(String data, String key) throws Exception {
        // 对公钥解密
        byte[] keyBytes = decryptBASE64(key);
        // 取得公钥
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicKey = keyFactory.generatePublic(x509KeySpec);
        // 对数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data.getBytes());
    }

    /**
     * 加密<br>
     * 用私钥加密
     *
     * @param data 明文数据
     * @param key  私钥
     * @return byte[] 加密的数据
     * @author xijieyin <br> 2022/10/12 14:55
     * @since 1.0.5
     */
    public static byte[] encryptByPrivateKey(byte[] data, String key) throws Exception {
        // 对密钥解密
        byte[] keyBytes = decryptBASE64(key);
        // 取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        // 对数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    /**
     * 取得私钥
     *
     * @return String
     * @author xijieyin <br> 2022/10/12 14:55
     * @since 1.0.5
     */
    public static String getPrivateKey() {
        return getPrivateKey(keyPair());
    }

    /**
     * 取得公钥
     *
     * @return String 公钥
     * @author xijieyin <br> 2022/10/12 14:55
     * @since 1.0.5
     */
    public static String getPublicKey() {
        return getPublicKey(keyPair());
    }

    /**
     * 获取密钥对 <br>
     * 生成密钥对：<br>
     * {@code keytool -genkey -alias rsa -keyalg RSA -keystore rsa.jks} <br>
     * {@code keytool -importkeystore -srckeystore rsa.jks -destkeystore rsa.jks -deststoretype pkcs12}
     *
     * @return KeyPair
     * @author xijieyin <br> 2022/10/12 14:54
     * @since 1.0.5
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
        RSAProperties properties = rsaProperties.getType().get(keypairType);
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
    public static KeyPair newKeyPair(RSAProperties properties) {
        return Optional.ofNullable(properties).map(p -> {
            ClassPathResource resource = new ClassPathResource(p.getResource());
            if (resource.exists()) {
                // 如果文件存在才加载
                if (p.getExpireCheck()) {
                    try {
                        expireCheck(p.getResource(), resource.getInputStream(), p.getPassword());
                    } catch (IOException e) {
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
        }).orElseGet(RSACoder::generateKeyPair);
    }

    /**
     * 生成密钥对
     *
     * @return 密钥对
     */
    private static KeyPair generateKeyPair() {
        KeyPairGenerator keyPairGen;
        try {
            keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyPairGen.initialize(2048);
        return keyPairGen.generateKeyPair();
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
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException |
                 NoSuchProviderException e) {
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
    public static String encryptBase64StringByPublicKey(String publicKey, String sourceStr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sourceStr.length(); i += encryptSplitSize) {
            byte[] encryptStrByte;
            try {
                encryptStrByte = RSACoder.encryptByPublicKey(sourceStr.substring(i, Math.min(i + encryptSplitSize, sourceStr.length())), publicKey);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            sb.append(new String(Base64.encodeBase64(encryptStrByte)));
        }
        return sb.toString();
    }

    /**
     * 用公钥加密
     *
     * @param sourceStr 明文
     * @return String 密文
     * @author xijieyin <br> 2022/10/12 14:51
     * @since 1.0.5
     */
    public static String encryptBase64StringByPublicKey(String sourceStr) {
        return encryptBase64StringByPublicKey(getPublicKey(), sourceStr);
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
    public static String encryptBase64StringByTypePublicKey(String sourceStr, String keypairType) {
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
    public static String decryptBase64StringByPrivateKey(String encryptStr, String privateKey) {
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < encryptStr.length(); i += decryptSplitSize) {
                sb.append(new String(RSACoder.decryptByPrivateKey(Base64.decodeBase64(encryptStr.substring(i, Math.min(i + decryptSplitSize, encryptStr.length()))), privateKey)));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 用私钥解密
     *
     * @param encryptStr 密文
     * @return String 明文
     * @author xijieyin <br> 2022/10/12 14:53
     * @since 1.0.5
     */
    public static String decryptBase64StringByPrivateKey(String encryptStr) {
        return decryptBase64StringByPrivateKey(encryptStr, getPrivateKey());
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
    public static String decryptBase64StringByTypePrivateKey(String encryptStr, String keypairType) {
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
    public static void genRSACACert(String alias
            , String password
            , long expiresIn
            , Consumer<X509v3CertificateBuilder> extension) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException, OperatorCreationException {
        X500Name build = createStdBuilder().build();
        genRSACACert(alias, password, build, build, expiresIn, extension, new String[]{CER_PATH + PRIVATE_KEY_NAME
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
    public static void genRSACACert(String alias
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
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
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
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
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

}
