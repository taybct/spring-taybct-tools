package io.github.mangocrisp.spring.taybct.tool.file.util;

import com.alibaba.fastjson2.JSONArray;
import io.github.mangocrisp.spring.taybct.tool.core.exception.def.BaseException;
import io.github.mangocrisp.spring.taybct.tool.core.result.ResultCode;
import io.github.mangocrisp.spring.taybct.tool.file.config.FileConfig;
import io.github.mangocrisp.spring.taybct.tool.file.config.LocalConfig;
import io.github.mangocrisp.spring.taybct.tool.file.config.MinioConfig;
import io.github.mangocrisp.spring.taybct.tool.file.config.OSSConfig;
import io.github.mangocrisp.spring.taybct.tool.file.exception.FileUploadException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * 文件上传工具类
 *
 * @author xijieyin <br> 2022/8/4 16:33
 * @since 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties({LocalConfig.class
        , MinioConfig.class
        , OSSConfig.class
        , FileConfig.class})
public class FileUploadUtil {

    private static LocalConfig localConfig;

    private static FileConfig fileConfig;

    public FileUploadUtil(LocalConfig localConfig, FileConfig fileConfig) {
        FileUploadUtil.localConfig = localConfig;
        FileUploadUtil.fileConfig = fileConfig;
    }

    /**
     * 根据文件路径上传
     *
     * @param baseDir 相对应用的基目录
     * @param file    上传的文件
     * @return 文件名称
     * @throws IOException IOException
     */
    public static String upload(String baseDir, MultipartFile file) throws IOException {
        try {
            return upload(baseDir, file, localConfig.getUploadAllowedExtensions());
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * 根据文件路径上传
     *
     * @param baseDir  相对应用的基目录
     * @param file     上传的文件
     * @param path     路径
     * @param filename 文件名
     * @return 文件名称
     * @throws IOException IOException
     */
    public static String upload(String baseDir, MultipartFile file, String path, String filename) throws IOException {
        try {
            fileCheck(file, localConfig.getUploadAllowedExtensions());
            String fileName = groupPathFilename(path, filename);
            File desc = getAbsoluteFile(baseDir, fileName);
            // 直接把文件传输到指定的文件
            file.transferTo(desc);
            return getPathFileName(fileName).replaceAll("//", "");
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * 组合路径和文件名
     *
     * @param path     路径
     * @param filename 文件名
     * @return 文件名
     */
    @NotNull
    public static String groupPathFilename(String path, String filename) {
        String fileName = (path + "/" + filename).replaceAll("//", "");
        if (fileName.indexOf("/") == 0) {
            fileName = fileName.substring(1);
        }
        return fileName;
    }

    /**
     * 文件上传
     *
     * @param baseDir          相对应用的基目录
     * @param file             上传的文件
     * @param allowedExtension 上传文件类型
     * @return 返回上传成功的文件名
     * @throws IOException 比如读写文件出错时
     */
    public static String upload(String baseDir, MultipartFile file, String[] allowedExtension) throws IOException {
        fileCheck(file, allowedExtension);
        String fileName = extractFilename(file);
        File desc = getAbsoluteFile(baseDir, fileName);
        // 直接把文件传输到指定的文件
        file.transferTo(desc);
        return getPathFileName(fileName);
    }

    /**
     * 文件查检
     *
     * @param file             文件
     * @param allowedExtension 允许的类型
     */
    private static void fileCheck(MultipartFile file, String[] allowedExtension) {
        int fileNameLength = Objects.requireNonNull(file.getOriginalFilename()).length();
        if (fileNameLength > localConfig.getUploadFileNameLength()) {
            throw new FileUploadException(String.format("文件上传失败，文件名限制长为：%s", localConfig.getUploadFileNameLength()));
        }
        assertAllowed(file, allowedExtension);
    }

    /**
     * 编码文件名
     */
    public static String extractFilename(MultipartFile file) {
        String extension = getExtension(file);
        return DateFormatUtils.format(new Date(), "yyyy/MM/dd") + "/" + UUID.randomUUID().toString().replaceAll("-", "") + "." + extension;
    }

    /**
     * 生成本地文件，生成父路径等
     *
     * @param uploadDir 本地上传路径
     * @param fileName  文件名
     * @return File
     */
    private static File getAbsoluteFile(String uploadDir, String fileName) {
        File desc = new File(uploadDir + File.separator + fileName);

        if (!desc.exists()) {
            if (!desc.getParentFile().exists()) {
                desc.getParentFile().mkdirs();
            }
        }
        return desc.isAbsolute() ? desc : desc.getAbsoluteFile();
    }

    /**
     * 获取文件路径
     *
     * @param fileName 文件名
     * @return String
     */
    private static String getPathFileName(String fileName) {
        return "/" + fileName;
    }

    /**
     * 文件大小校验
     *
     * @param file             文件
     * @param allowedExtension 允许的类型
     */
    public static void assertAllowed(MultipartFile file, String[] allowedExtension) {
        long size = file.getSize();
        Long fileUploadFileSize = localConfig.getUploadFileSize();
        if (fileUploadFileSize > 0 && size > fileUploadFileSize) {
            throw new FileUploadException(String.format("文件上传失败，文件大小制长为：%s MB", (fileUploadFileSize / 1024 / 1024)));
        }
        String extension = getExtension(file);
        if (allowedExtension != null && allowedExtension.length > 0 && !isAllowedExtension(extension, allowedExtension)) {
            throw new FileUploadException(String.format("文件上传失败，允许上传的文件为以下类型：%s", (JSONArray.toJSONString(allowedExtension))));
        }
    }

    /**
     * 判断MIME类型是否是允许的MIME类型
     *
     * @param extension        上传文件类型
     * @param allowedExtension 允许上传文件类型
     * @return true/false
     */
    public static boolean isAllowedExtension(String extension, String[] allowedExtension) {
        for (String str : allowedExtension) {
            if (str.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 这里，因为，如果没有文件后缀，就会根据文件类型来判断，但是，配置文件的例如 {@literal "image/png": "png"} 放到 map 里面
     * key 就会自动把 / 去掉，变成了 imagepng:png 这就不是我们想要的了，所以，拿到上传文件的文件类型的时候，也要把这个 / 给 replace 掉
     * 获取文件名的后缀
     *
     * @param file 上传文件
     * @return 返回文件后缀
     */
    public static String getExtension(MultipartFile file) {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (StringUtils.isEmpty(extension)) {
            extension = fileConfig.getFileContentType().get(
                    Optional.ofNullable(file.getContentType()).orElse("").replaceAll("/", ""));
        }
        if (StringUtils.isEmpty(extension)) {
            throw new BaseException(ResultCode.VALIDATE_ERROR, "文件类型不支持！");
        }
        return extension;
    }
}
