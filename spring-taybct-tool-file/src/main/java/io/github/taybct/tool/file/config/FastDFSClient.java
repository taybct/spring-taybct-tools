package io.github.taybct.tool.file.config;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadCallback;
import com.github.tobato.fastdfs.service.DefaultFastFileStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.io.*;
import java.util.Base64;

/**
 * 配置 FastDFS 客户端，用于操作 FastDF 接口
 *
 * @author xijieyin <br> 2022/8/4 16:07
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(FastDFSConfig.class)
public class FastDFSClient {

    private final DefaultFastFileStorageClient client;

    public FastDFSClient(DefaultFastFileStorageClient client) {
        this.client = client;
    }

    public byte[] download(String storageCode) {
        StorePath storePath = StorePath.parseFromUrl(storageCode);
        DownloadByteArray callback = new DownloadByteArray();
        byte[] bytes = client.downloadFile(storePath.getGroup(), storePath.getPath(), callback);
        if (bytes == null) {
            log.error("从FastDFS下载到空文件：{}", storageCode);
            bytes = new byte[0];
        }
        return bytes;
    }

    public String downloadBase64(String storageCode) {
        byte[] bytes = download(storageCode);
        if (bytes.length == 0) {
            return null;
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    public <T> T download(String code, DownloadCallback<T> callback) {
        StorePath storePath = StorePath.parseFromUrl(code);
        return client.downloadFile(storePath.getGroup(), storePath.getPath(), callback);
    }

    public String upload(String imageStr, String extName) throws IOException {
        if (StringUtils.isBlank(imageStr)) {
            return null;
        }
        imageStr = imageStr.replaceAll("[\r\n]", "");
        imageStr = imageStr.substring(imageStr.indexOf(",") + 1);
        byte[] bytes = Base64.getDecoder().decode(imageStr);
        for (int i = 0; i < bytes.length; ++i) {
            //调整异常数据
            if (bytes[i] < 0) {
                bytes[i] += 256;
            }
        }
        String filePath = upload(bytes, extName);
        log.trace("上传文件到FastDFS返回地址：{}", filePath);
        return filePath;
    }

    public String upload(byte[] bytes, String extName) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            return upload(inputStream, bytes.length, extName);
        }
    }

    public String upload(File file) throws IOException {
        String extName = FilenameUtils.getExtension(file.getName());
        try (FileInputStream fis = new FileInputStream(file)) {
            return upload(fis, file.length(), extName);
        }
    }

    public String upload(InputStream inputStream, long fileSize, String extName) {
        String fileExtName = "unknown";

        if (StringUtils.isNotEmpty(extName)) {
            fileExtName = extName;
        }
        StorePath storePath = client.uploadFile(inputStream, fileSize, fileExtName, null);
        return storePath.getFullPath();
    }

    public void delete(String storageCode) {
        try {
            client.deleteFile(storageCode);
        } catch (Exception e) {
            log.error("删除文件失败：{}", storageCode);
        }
    }
}
