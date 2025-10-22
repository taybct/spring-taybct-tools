package io.github.taybct.tool.file.service.impl;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import io.github.taybct.tool.file.config.FastDFSConfig;
import io.github.taybct.tool.file.service.IFileService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * FastDFS 文件存储
 *
 * @author xijieyin <br> 2022/8/4 16:31
 * @since 1.0.0
 */
@Service
@RefreshScope
@AutoConfiguration
public class FileServiceFastDFSImpl implements IFileService {
    @Autowired
    FastDFSConfig fastDFSConfig;
    @Autowired
    private FastFileStorageClient storageClient;

    @Override
    public String upload(MultipartFile file) throws Exception {
        Assert.notNull(file, "file cannot be empty!");
        return upload(file, fastDFSConfig.getGroup());
    }

    @Override
    public String upload(MultipartFile file, String path, String filename) throws Exception {
        Assert.notNull(file, "file cannot be empty!");
        StorePath storePath = storageClient.uploadSlaveFile(fastDFSConfig.getGroup()
                , filename
                , file.getInputStream()
                , file.getSize()
                , ""
                , FilenameUtils.getExtension(file.getOriginalFilename()));
        return storePath.getPath();
    }

    /**
     * 可以指定分组名来上传
     *
     * @param file      上传的文件
     * @param groupName 分组名
     * @return Boolean
     * @author xijieyin <br> 2022/8/4 15:44
     * @since 1.0.0
     */
    public String upload(MultipartFile file, String groupName) throws Exception {
        StorePath storePath = storageClient.uploadFile(groupName, file.getInputStream(),
                file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()));
        return storePath.getPath();
    }

    @Override
    public Boolean delete(String path) throws Exception {
        Assert.notNull(path, "path cannot be empty!");
        return delete(path, fastDFSConfig.getGroup());
    }

    /**
     * 可以指定分组名来删除
     *
     * @param path      文件路径
     * @param groupName 分组名
     * @return Boolean
     * @author xijieyin <br>2022/8/4 15:44
     * @since 1.0.0
     */
    public Boolean delete(String path, String groupName) throws Exception {
        Assert.notNull(groupName, "group name cannot be empty!");
        storageClient.deleteFile(groupName, path);
        return true;
    }

    @Override
    public InputStream get(String path) throws Exception {
        Assert.notNull(path, "args cannot be empty!");
        return get(path, fastDFSConfig.getGroup());
    }

    /**
     * 可以指定分组名来下载
     *
     * @param path      文件路径
     * @param groupName 分组名
     * @return Boolean
     * @author xijieyin <br> 2022/8/4 15:44
     * @since 1.0.0
     */
    public InputStream get(String path, String groupName) throws Exception {
        Assert.notNull(groupName, "group name cannot be empty!");
        DownloadByteArray downloadFileWriter = new DownloadByteArray();
        byte[] remoteFile = storageClient.downloadFile(groupName, path, downloadFileWriter);
        return new ByteArrayInputStream(remoteFile);
    }
}
