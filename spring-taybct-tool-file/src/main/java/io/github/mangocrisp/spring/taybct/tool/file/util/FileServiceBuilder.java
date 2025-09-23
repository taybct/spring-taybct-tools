package io.github.mangocrisp.spring.taybct.tool.file.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.SpringUtil;
import io.github.mangocrisp.spring.taybct.tool.file.config.FileConfig;
import io.github.mangocrisp.spring.taybct.tool.file.dto.MultipartFileDto;
import io.github.mangocrisp.spring.taybct.tool.file.enums.FileManageType;
import io.github.mangocrisp.spring.taybct.tool.file.exception.FileUploadException;
import io.github.mangocrisp.spring.taybct.tool.file.service.IFileService;
import org.apache.hc.core5.http.ContentType;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.*;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件服务构建器<br>
 * 根据配置的使用不同的文件对象存储服务，动态的改变来构建文件服务，这是考虑到线上一些不可知的因素，如果要临时换文件服务，那不同的文件服务，
 * 请求接口肯定是不尽相同的，所以这个类相当于是统一了请求的接口，让请求变得相同，然后去获取不同的文件服务
 *
 * @author xijieyin <br> 2022/8/4 14:44
 * @since 1.0.0
 */
public class FileServiceBuilder {

    static {
        try {
            // 创建一个信任所有证书的 TrustManager
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                        }
                    }
            };
            // 初始化 SSLContext
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            // 创建一个信任所有主机名的 HostnameVerifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 文件配置
     */
    static FileConfig getConfig() {
        FileConfig fileConfig = SpringUtil.getBean(FileConfig.class);
        if (!fileConfig.getEnable()) {
            throw new FileUploadException("目前系统不允许文件相关操作！");
        }
        return fileConfig;
    }

    /**
     * 实现类实例
     */
    private static final ConcurrentHashMap<String, IFileService> IMPL_POOL = new ConcurrentHashMap<>();

    /**
     * 获取文件服务器
     *
     * @param type 类型
     * @return 根据类型的不同，文件管理的实现类也不同
     */
    public static IFileService build(FileManageType type) {
        return Optional.ofNullable(IMPL_POOL.get(type.getKey())).orElseGet(() -> {
            IFileService service = SpringUtil.getBean(type.getImplClass());
            IMPL_POOL.put(type.getKey(), service);
            return service;
        });
    }

    /**
     * 获取文件管理类型
     */
    public static FileManageType getType() {
        FileConfig fileConfig = getConfig();
        String type = fileConfig.getType();
        if (type == null) {
            return FileManageType.LOCAL;
        }
        if (type.equalsIgnoreCase(FileManageType.LOCAL.getKey())) {
            return FileManageType.LOCAL;
        }
        if (type.equalsIgnoreCase(FileManageType.OSS.getKey())) {
            return FileManageType.OSS;
        }
        if (type.equalsIgnoreCase(FileManageType.FAST_DFS.getKey())) {
            return FileManageType.FAST_DFS;
        }
        if (type.equalsIgnoreCase(FileManageType.MIN_IO.getKey())) {
            return FileManageType.MIN_IO;
        }
        if (fileConfig.getClazz() != null) {
            return new FileManageType(type, fileConfig.getClazz());
        }
        return FileManageType.LOCAL;
    }

    /**
     * 根据传的类型使用不同的文件服务器类型上传
     *
     * @param file 需要上传的文件
     * @return 文件上传后的访问地址
     * @throws Exception 这里有很多种异常，MinIO的异常最多
     */
    public static String upload(MultipartFile file) throws Exception {
        checkWordFileEmpty(file);
        return FileServiceBuilder.build(getType()).upload(file);
    }

    /**
     * 根据传的类型使用不同的文件服务器类型上传
     *
     * @param file     需要上传的文件
     * @param path     路径
     * @param filename 文件名
     * @return 文件上传后的访问地址
     * @throws Exception 这里有很多种异常，MinIO的异常最多
     */
    public static String upload(MultipartFile file, String path, String filename) throws Exception {
        checkWordFileEmpty(file);
        if (path == null) {
            path = "";
        }
        if (filename == null) {
            return upload(file);
        }
        return FileServiceBuilder.build(getType()).upload(file, path, filename);
    }

    /**
     * 根据传的类型使用不同的文件服务器类型上传
     *
     * @param fileByteArray 需要上传的文件的字节数组
     * @param contentType   媒体格式类型
     * @param filename      需要上传的文件名
     * @return 文件上传后的访问地址
     * @throws Exception 这里有很多种异常，MinIO的异常最多
     */
    public static String upload(byte[] fileByteArray, ContentType contentType, @Nullable String filename) throws Exception {
        return upload(fileByteArray, contentType.getMimeType(), filename);
    }

    /**
     * 根据传的类型使用不同的文件服务器类型上传
     *
     * @param fileByteArray 需要上传的文件的字节数组
     * @param contentType   媒体格式类型
     * @param filename      需要上传的文件名
     * @return 文件上传后的访问地址
     * @throws Exception 这里有很多种异常，MinIO的异常最多
     */
    public static String upload(byte[] fileByteArray, String contentType, @Nullable String filename) throws Exception {
        String name = Convert.toStr(filename, UUID.randomUUID().toString());
        return upload(new MultipartFileDto(name, name, contentType, fileByteArray));
    }

    /**
     * 根据传的类型使用不同的文件服务器类型上传
     *
     * @param fileBase64  需要上传的文件的 base64 字符串
     * @param contentType 媒体格式类型
     * @param filename    需要上传的文件名
     * @return 文件上传后的访问地址
     * @throws Exception 这里有很多种异常，MinIO的异常最多
     */
    public static String upload(String fileBase64, ContentType contentType, @Nullable String filename) throws Exception {
        return upload(fileBase64, contentType.getMimeType(), filename);
    }

    /**
     * 根据传的类型使用不同的文件服务器类型上传
     *
     * @param fileBase64  需要上传的文件的 base64 字符串
     * @param contentType 媒体格式类型
     * @param filename    需要上传的文件名
     * @return 文件上传后的访问地址
     * @throws Exception 这里有很多种异常，MinIO的异常最多
     */
    public static String upload(String fileBase64, String contentType, @Nullable String filename) throws Exception {
        return upload(Base64.getDecoder().decode(fileBase64), contentType, filename);
    }

    /**
     * 根据传的类型使用不同的文件服务器类型上传
     *
     * @param fileInputStream 需要上传的文件的流
     * @param contentType     媒体格式类型
     * @param filename        需要上传的文件名
     * @return 文件上传后的访问地址
     * @throws Exception 这里有很多种异常，MinIO的异常最多
     */
    public static String upload(InputStream fileInputStream, ContentType contentType, @Nullable String filename) throws Exception {
        return upload(fileInputStream, contentType.getMimeType(), filename);
    }

    /**
     * 根据传的类型使用不同的文件服务器类型上传
     *
     * @param fileInputStream 需要上传的文件的流
     * @param contentType     媒体格式类型
     * @param filename        需要上传的文件名
     * @return 文件上传后的访问地址
     * @throws Exception 这里有很多种异常，MinIO的异常最多
     */
    public static String upload(InputStream fileInputStream, String contentType, @Nullable String filename) throws Exception {
        String name = Convert.toStr(filename, UUID.randomUUID().toString());
        return upload(new MultipartFileDto(name, name, contentType, fileInputStream));
    }

    /**
     * 根据传的类型使用不同的文件服务器类型删除文件
     *
     * @param path 文件路径
     * @return boolean
     * @author xijieyin <br> 2022/8/4 19:15
     * @since 1.0.0
     */
    public static boolean delete(String path) throws Exception {
        return FileServiceBuilder.build(getType()).delete(path);
    }

    /**
     * 根据传的类型使用不同的文件服务器类型下载
     *
     * @param path 文件路径
     */
    public static InputStream get(String path) throws Exception {
        return FileServiceBuilder.build(getType()).get(path);
    }

    /**
     * 根据传的类型使用不同的文件服务器类型下载,获取到 byte 数组
     *
     * @param path 文件路径
     */
    public static byte[] getByteArray(String path) throws Exception {
        return IoUtil.readBytes(get(path));
    }

    /**
     * 根据传的类型使用不同的文件服务器类型下载,获取到 base64
     *
     * @param path 文件路径
     */
    public static String getBase64(String path) throws Exception {
        return Base64.getEncoder().encodeToString(getByteArray(path));
    }

    /**
     * 保存网址文件
     *
     * @param downloadUri 的文件地址
     * @param fileName    上传的文件名（主要用来获取文件类型）
     * @return 上传后的文件地址
     * @throws Exception 异常
     */
    private static String saveFile(String downloadUri, String fileName) throws Exception {
        URL url = new URL(downloadUri);
        HttpURLConnection connection = null;
        HttpsURLConnection connections = null;
        if (downloadUri.startsWith("https://")) {
            connections = (HttpsURLConnection) url.openConnection();
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }
        if (connections == null) {
            connection = (HttpURLConnection) url.openConnection();
        }
        String path;
        try (InputStream stream = connections != null ? connections.getInputStream() : connection.getInputStream()) {
            path = FileServiceBuilder.upload(stream, ContentType.MULTIPART_FORM_DATA, fileName);
        }
        if (connection != null) {
            connection.disconnect();
        }
        if (connections != null) {
            connections.disconnect();
        }
        return path;
    }

    /**
     * 判断文件是否为空
     *
     * @param wordFile 文件
     * @return boolean
     */
    private static boolean isWordFileEmpty(MultipartFile wordFile) {
        return wordFile == null || wordFile.isEmpty();
    }

    /**
     * 检测文件是否为空
     *
     * @param wordFile 文件
     */
    private static void checkWordFileEmpty(MultipartFile wordFile) {
        if (isWordFileEmpty(wordFile)) {
            throw new FileUploadException("上传文件不能为空");
        }
    }

}
