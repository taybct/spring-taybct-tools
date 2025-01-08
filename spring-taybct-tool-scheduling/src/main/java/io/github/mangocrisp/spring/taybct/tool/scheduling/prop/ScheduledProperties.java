package io.github.mangocrisp.spring.taybct.tool.scheduling.prop;

import io.github.mangocrisp.spring.taybct.tool.core.constant.PropertiesPrefixConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.io.Serial;
import java.io.Serializable;
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
     * 关闭程序，等待任务完成
     */
    private Boolean waitForTasksToCompleteOnShutdown = true;
    /**
     * 等待结束时间，单位秒
     */
    private Integer awaitTerminationSeconds = 60;
    /**
     * 任务配置
     */
    private Map<String, ScheduledTaskBean> tasks = new ConcurrentHashMap<>();

}
