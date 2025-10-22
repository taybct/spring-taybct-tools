package io.github.taybct.tool.scheduling.prop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * 任务调度 bean
 *
 * @author xijieyin <br> 2022/10/31 15:08
 * @since 1.1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledTaskBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 536536468628021961L;

    /**
     * 任务key值 唯一
     */
    private String taskKey;
    /**
     * 任务描述
     */
    private String description;
    /**
     * 任务表达式
     */
    private String cron;
    /**
     * 当前是否已启动 1 已启动 0 未启动
     */
    private int startFlag = 0;
    /**
     * 是否自动启动 1 是 0否
     */
    private int autoStart = 0;
    /**
     * 启动顺序
     */
    private int sort = 0;
    /**
     * 需要传递到任务的属性配置
     */
    private Map<String, Object> params = Collections.emptyMap();

}
