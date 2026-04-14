package io.github.taybct.tool.scheduling.prop;

import io.github.taybct.tool.core.constant.PropertiesPrefixConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.system.JavaVersion;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xijieyin <br> 2022/11/1 9:51
 * @since 1.1.0
 */
@Data
@RefreshScope
@ConfigurationProperties(PropertiesPrefixConstants.SCHEDULED)
public class ScheduledProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 2640772240908496927L;

    /**
     * 池大小
     */
    private Integer poolSize = 20;
    /**
     * 任务线程名（前缀）
     */
    private String threadNamePrefix = "Scheduler-";
    /**
     * 是否开启虚拟线程（兼容 jdk 21 以下版本）
     */
    private boolean virtualThreads = JavaVersion.getJavaVersion().isEqualOrNewerThan(JavaVersion.TWENTY_ONE);
    /**
     * 关闭程序，等待任务完成
     */
    private Boolean waitForTasksToCompleteOnShutdown = true;
    /**
     * 等待结束时间，单位秒
     */
    private Integer awaitTerminationSeconds = 60;
    /**
     * 扫描包，用于扫描任务类
     */
    private List<String> packagesToScan;
    /**
     * 任务配置，key 为唯一标识，用于配置文件里面表示一个任务，value 为任务配置信息，如果是数据库任务，这个 key 应该是任务的 id，而非任务 key
     */
    private Map<String, ScheduledTaskBean> tasks = new ConcurrentHashMap<>();

}
