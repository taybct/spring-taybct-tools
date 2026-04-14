package io.github.taybct.tool.scheduling.service;

import io.github.taybct.tool.scheduling.prop.ScheduledTaskBean;

import java.util.List;
import java.util.Map;

/**
 * 定时任务接口
 *
 * @author xijieyin <br> 2022/10/31 15:20
 * @since 1.1.0
 */
public interface ISchedulingService {

    /**
     * 所有任务列表
     */
    List<ScheduledTaskBean> taskList();

    /**
     * 尝试执行一次
     *
     * @param taskKey 任务 key
     * @param params  参数
     * @return 结果
     */
    boolean tryOnce(String taskKey, Map<String, Object> params);

    /**
     * 根据 唯一标识 启动任务
     */
    Boolean start(String uniKey);

    /**
     * 根据 唯一标识 停止任务
     */
    Boolean stop(String uniKey);

    /**
     * 根据 唯一标识 重启任务
     */
    Boolean restart(String uniKey);

    /**
     * 程序启动时初始化  启动所有正常状态的任务
     */
    void initAllTask(List<ScheduledTaskBean> scheduledTaskBeanList);

    /**
     * 检查任务是否已经启动
     *
     * @param uniKey 唯一标识
     */
    Boolean isStart(String uniKey);

}
