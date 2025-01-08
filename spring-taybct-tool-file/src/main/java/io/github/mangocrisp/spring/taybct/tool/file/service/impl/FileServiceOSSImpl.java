package io.github.mangocrisp.spring.taybct.tool.file.service.impl;

import cn.hutool.core.codec.Base64;
import io.github.mangocrisp.spring.taybct.tool.file.config.OSSConfig;
import io.github.mangocrisp.spring.taybct.tool.file.service.IFileService;
import io.github.mangocrisp.spring.taybct.tool.file.util.FileUploadUtil;
import io.github.mangocrisp.spring.taybct.tool.file.util.OSSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * OSS 文件管理，这个类主要是操作 OSS 上的文件
 *
 * @author xijieyin <br> 2022/8/4 15:11
 * @see IFileService
 * @since 1.0.0
 */
@Service("fileServiceOSSImpl")
@AutoConfiguration
@RefreshScope
public class FileServiceOSSImpl implements IFileService {

    @Autowired
    OSSConfig ossConfig;

    @Override
    public String upload(MultipartFile file) throws Exception {
        Assert.notNull(file, "file cannot be empty!");
        return upload(file.getInputStream(), String.format("images/%s", FileUploadUtil.extractFilename(file)));
    }

    @Override
    public String upload(MultipartFile file, String path, String filename) throws Exception {
        Assert.notNull(file, "file cannot be empty!");
        return upload(file.getInputStream(), FileUploadUtil.groupPathFilename(path, filename));
    }

    public String upload(InputStream inputStream, String filePath) throws Exception {
        OSSUtil.ini(ossConfig).upload(filePath, inputStream);
        return filePath;
    }

    @Override
    public Boolean delete(String path) throws Exception {
        Assert.notNull(path, "path cannot be empty!");
        OSSUtil.ini(ossConfig).delete(path);
        return true;
    }

    @Override
    public InputStream get(String path) throws Exception {
        Assert.notNull(path, "path cannot be empty!");
        String base64File = OSSUtil.ini(ossConfig).get(path);
        byte[] fileByte = Base64.decode(base64File);
        return new ByteArrayInputStream(fileByte);
    }

}
