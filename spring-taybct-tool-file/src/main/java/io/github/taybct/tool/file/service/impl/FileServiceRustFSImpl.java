package io.github.taybct.tool.file.service.impl;

import io.github.taybct.tool.file.config.RustFSConfig;
import io.github.taybct.tool.file.service.IFileService;
import io.github.taybct.tool.file.util.FileUploadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

/**
 *
 * <pre>
 * RustFS 实现
 * </pre>
 *
 * @author XiJieYin
 * @since 2026/1/30 15:52
 */
@Service
@AutoConfiguration
@RefreshScope
@Slf4j
public class FileServiceRustFSImpl implements IFileService {

    @Autowired
    private RustFSConfig rustFSConfig;

    public S3Client getS3Client() {
        String key = RustFSConfig.genKey(rustFSConfig);
        S3Client client = RustFSConfig.s3ClientPool.get(key);
        if (client == null) {
            client = S3Client.builder()
                    .endpointOverride(URI.create(rustFSConfig.getUrl())) // RustFS address
                    .region(Region.US_EAST_1) // RustFS does not validate regions
                    .credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(rustFSConfig.getAccessKey(), rustFSConfig.getSecretKey())
                            )
                    )
                    .forcePathStyle(true) // Required for RustFS compatibility
                    .build();
        }
        RustFSConfig.s3ClientPool.put(key, client);
        return client;
    }

    private void checkAndCreateBucket(S3Client s3Client){
        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(rustFSConfig.getBucketName()).build());
            log.info("Bucket created: " + rustFSConfig.getBucketName());
        } catch (BucketAlreadyExistsException | BucketAlreadyOwnedByYouException e) {
            log.debug("Bucket already exists.");
        }
    }

    @Override
    public String upload(MultipartFile file) throws Exception {
        Assert.notNull(file, "file cannot be empty!");
        String fileName = FileUploadUtil.extractFilename(file);
        S3Client s3Client = getS3Client();
        checkAndCreateBucket(s3Client);
        s3Client.putObject(PutObjectRequest.builder().bucket(rustFSConfig.getBucketName()).key(fileName).build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        return fileName;
    }

    @Override
    public String upload(MultipartFile file, String path, String filename) throws Exception {
        Assert.notNull(file, "file cannot be empty!");
        String fileName = FileUploadUtil.groupPathFilename(path, filename);
        S3Client s3Client = getS3Client();
        checkAndCreateBucket(s3Client);
        s3Client.putObject(PutObjectRequest.builder().bucket(rustFSConfig.getBucketName()).key(fileName).build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        return fileName;
    }

    @Override
    public Boolean delete(String path) throws Exception {
        Assert.notNull(path, "path cannot be empty!");
        S3Client s3Client = getS3Client();
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(rustFSConfig.getBucketName()).key(path).build());
        return true;
    }

    @Override
    public InputStream get(String path) throws Exception {
        Assert.notNull(path, "path cannot be empty!");
        S3Client s3Client = getS3Client();
        ResponseInputStream<GetObjectResponse> object = s3Client.getObject(GetObjectRequest.builder().bucket(rustFSConfig.getBucketName()).key(path).build());
        return new ByteArrayInputStream(object.readAllBytes());
    }

}
