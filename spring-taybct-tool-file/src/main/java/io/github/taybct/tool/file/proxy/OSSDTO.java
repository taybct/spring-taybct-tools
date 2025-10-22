package io.github.taybct.tool.file.proxy;

import io.github.taybct.tool.file.config.OSSConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * OSS 数据传输对象
 *
 * @author xijieyin <br> 2022/8/4 16:29
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OSSDTO extends OSSConfig {
    @Serial
    private static final long serialVersionUID = 3256280032061302809L;
    /**
     * 文件路径
     */
    private String filePath;
    /**
     * 流(Base64格式)
     */
    private String streamBase64;
    /**
     * 对象名
     */
    private String objectName;
}
