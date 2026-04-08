package io.github.taybct.tool.scheduling.job;

import io.github.taybct.tool.scheduling.prop.ScheduledTaskBean;

import java.util.Map;

/**
 * 任务调度接口
 *
 * @author xijieyin <br> 2022/10/31 15:03
 * @since 1.1.0
 */
public interface IScheduledTaskJob extends Runnable {

    /**
     * 配置任务属性
     *
     * @param taskBean 任务属性
     */
    void config(ScheduledTaskBean taskBean);

    /**
     * 这个 run 需要用户自己支写逻辑
     *
     * @param params 参数就是配置的参数
     */
    void run(Map<String, Object> params) throws Exception;

}
