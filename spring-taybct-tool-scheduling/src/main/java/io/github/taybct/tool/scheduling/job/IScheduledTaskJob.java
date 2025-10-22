package io.github.taybct.tool.scheduling.job;

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

    /**
     * 这个 run 需要用户自己支写逻辑
     *
     * @param params 参数就是配置的参数
     */
    void run(Map<String, Object> params) throws Exception;

}
