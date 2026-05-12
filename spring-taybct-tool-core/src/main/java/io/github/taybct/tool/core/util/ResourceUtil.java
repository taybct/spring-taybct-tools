package io.github.taybct.tool.core.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.FileNotFoundException;

/**
 *
 * <pre>
 * 资源文件工具类
 * </pre>
 *
 * @author XiJieYin
 * @since 2026/5/12 16:12
 */
public class ResourceUtil {

    /**
     * 获取spring resource
     *
     * @param path 路径
     * @return resource
     */
    public static Resource getSpringResource(String path) throws FileNotFoundException {
        FileSystemResource fileSystemResource = new FileSystemResource(path);
        if (fileSystemResource.exists()) {
            return fileSystemResource;
        }
        ClassPathResource classPathResource = new ClassPathResource(path);
        if (classPathResource.exists()) {
            return classPathResource;
        }
        return new FileSystemResource(path);
    }

}
