package io.github.mangocrisp.spring.taybct.tool.file.service.impl;

import io.github.mangocrisp.spring.taybct.tool.file.config.LocalConfig;
import io.github.mangocrisp.spring.taybct.tool.file.service.IFileService;
import io.github.mangocrisp.spring.taybct.tool.file.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * 本地文件存储，上传的文件会存储在服务器的某个文件夹，这是没有配置对象存储器的情况下会使用的一种文件管理方式，如果使用这种方式，得做好文件的后续管理工作。
 *
 * @author xijieyin <br> 2022/8/4 15:27
 * @since 1.0.0
 */
@Service
@AutoConfiguration
@RefreshScope
public class FileServiceLocalImpl implements IFileService {

    @Autowired
    LocalConfig localConfig;

    @Override
    public String upload(MultipartFile file) throws Exception {
        Assert.notNull(file, "file cannot be empty!");
        return FileUploadUtil.upload(localConfig.getLocalFilePath(), file);
    }

    @Override
    public String upload(MultipartFile file, String path, String filename) throws Exception {
        Assert.notNull(file, "file cannot be empty!");
        return FileUploadUtil.upload(localConfig.getLocalFilePath(), file, path, filename);
    }

    /**
     * 根据 path 解析成本地路径的文件
     *
     * @param path 上传成功后的文件请求路径
     * @return 获取到文件
     */
    private File genLocalFileByUrl(String path) {
        String filePath = String.format("%s%s", localConfig.getLocalFilePath(), path);
        return new File(filePath);
    }

    @Override
    public Boolean delete(String path) throws Exception {
        Assert.notNull(path, "path cannot be empty!");
        File file = genLocalFileByUrl(path);
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }

    @Override
    public InputStream get(String path) throws Exception {
        Assert.notNull(path, "path cannot be empty!");
        File file = genLocalFileByUrl(path);
        return Files.newInputStream(file.toPath());
    }
}
