package io.github.taybct.tool.file.service.impl;

import io.github.taybct.tool.file.config.MinioConfig;
import io.github.taybct.tool.file.service.IFileService;
import io.github.taybct.tool.file.util.FileUploadUtil;
import io.minio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * MinIO 文件管理，这个类主要是操作 MinIO 上的文件
 *
 * @author xijieyin <br> 2022/8/4 15:24
 * @see IFileService
 * @since 1.0.0
 */
@Service
@AutoConfiguration
@RefreshScope
public class FileServiceMinioImpl implements IFileService {
    @Autowired
    private MinioConfig minioConfig;

    public MinioClient getMinioClient() {
        String key = MinioConfig.genKey(minioConfig);
        MinioClient client = MinioConfig.minioClientPool.get(key);
        if (client == null) {
            client = MinioClient.builder().endpoint(minioConfig.getUrl()).credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey()).build();
        }
        MinioConfig.minioClientPool.put(key, client);
        return client;
    }

    @Override
    public String upload(MultipartFile file) throws Exception {
        Assert.notNull(file, "file cannot be empty!");
        String fileName = FileUploadUtil.extractFilename(file);
        PutObjectArgs params = PutObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(fileName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build();
        getMinioClient().putObject(params);
        // 这样返回可以直接访问，但是需要 Minio 服务端设置这个 minioConfig.getBucketName() 是公开的，如果不公开也不能用，而且，如果环境变了，又需要改 prefix
        //return minioConfig.getUrl() + "/" + minioConfig.getBucketName() + "/" + fileName;
        return fileName;
    }

    @Override
    public String upload(MultipartFile file, String path, String filename) throws Exception {
        Assert.notNull(file, "file cannot be empty!");
        String fileName = FileUploadUtil.groupPathFilename(path, filename);
        PutObjectArgs params = PutObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(fileName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build();
        getMinioClient().putObject(params);
        return fileName;
    }

    private String genFileByUrl(String url) {
        return url;
    }

    @Override
    public Boolean delete(String path) throws Exception {
        Assert.notNull(path, "path cannot be empty!");
        RemoveObjectArgs parms = RemoveObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(genFileByUrl(path))
                .build();
        getMinioClient().removeObject(parms);
        return true;
    }

    @Override
    public InputStream get(String path) throws Exception {
        Assert.notNull(path, "path cannot be empty!");
        GetObjectArgs parms = GetObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(genFileByUrl(path))
                .build();
        GetObjectResponse object = getMinioClient().getObject(parms);
        return new ByteArrayInputStream(object.readAllBytes());
    }
}
