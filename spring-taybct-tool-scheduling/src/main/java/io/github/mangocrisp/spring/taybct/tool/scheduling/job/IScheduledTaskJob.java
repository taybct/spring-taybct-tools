package io.github.mangocrisp.spring.taybct.tool.scheduling.job;

import java.util.Map;

/**
 * 任务调度接口
 *
 * @author xijieyin <br> 2022/10/31 15:03
 * @since 1.1.0
 */
public interface IScheduledTaskJob extends Runnable {

    /**
     * 初始化 key
     */
    void setKey(String taskKey);

    /**
     * 设置 cron 表达式
     *
     * @param cron cron 表达式
     */
    void setCron(String cron);

    /**
     * 初始化任务描述
     */
    void setDescription(String description);

    /**
     * 初始化属性配置
     */
    void setParams(Map<String, Object> params);

}
