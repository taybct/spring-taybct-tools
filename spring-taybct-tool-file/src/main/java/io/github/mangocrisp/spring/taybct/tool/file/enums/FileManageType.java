package io.github.mangocrisp.spring.taybct.tool.file.enums;

import io.github.mangocrisp.spring.taybct.tool.file.service.IFileService;
import io.github.mangocrisp.spring.taybct.tool.file.service.impl.FileServiceFastDFSImpl;
import io.github.mangocrisp.spring.taybct.tool.file.service.impl.FileServiceLocalImpl;
import io.github.mangocrisp.spring.taybct.tool.file.service.impl.FileServiceMinioImpl;
import io.github.mangocrisp.spring.taybct.tool.file.service.impl.FileServiceOSSImpl;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * 配置 对象存储类型，以及类型对应的管理实现类，用于方便随时切换对象存储
 *
 * @author xijieyin <br> 2022/8/4 16:21
 * @since 1.0.0
 */
@Getter
public final class FileManageType implements Serializable {
    /**
     * key键
     */
    private final String key;
    /**
     * 应该获取哪个类
     */
    private final Class<? extends IFileService> implClass;
    /**
     * 存本地指定文件夹
     */
    public static final FileManageType LOCAL = new FileManageType("local", FileServiceLocalImpl.class);
    /**
     * 阿里云对象管理
     */
    public static final FileManageType OSS = new FileManageType("oss", FileServiceOSSImpl.class);
    /**
     * FastDFS，目前没有在维护了
     */
    public static final FileManageType FAST_DFS = new FileManageType("fdfs", FileServiceFastDFSImpl.class);
    /**
     * MinIO 对象存储
     */
    public static final FileManageType MIN_IO = new FileManageType("minio", FileServiceMinioImpl.class);

    public FileManageType(String key, Class<? extends IFileService> implClass) {
        this.key = key;
        this.implClass = implClass;
    }

    public FileManageType(String key) {
        this.key = key;
        switch (key) {
            case "local":
                this.implClass = LOCAL.implClass;
                break;
            case "oss":
                this.implClass = OSS.implClass;
                break;
            case "fdfs":
                this.implClass = FAST_DFS.implClass;
                break;
            case "minio":
                this.implClass = MIN_IO.implClass;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + key);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        FileManageType that = (FileManageType) obj;
        return this.getKey().equals(that.getKey());
    }

    @Override
    public int hashCode() {
        return this.getKey().hashCode();
    }
}
