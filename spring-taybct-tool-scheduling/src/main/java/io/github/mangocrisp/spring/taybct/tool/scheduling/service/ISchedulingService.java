package io.github.mangocrisp.spring.taybct.tool.scheduling.service;

import io.github.mangocrisp.spring.taybct.tool.scheduling.prop.ScheduledTaskBean;

import java.util.List;

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
     * 根据任务key 启动任务
     */
    Boolean start(String taskKey);

    /**
     * 根据任务key 停止任务
     */
    Boolean stop(String taskKey);

    /**
     * 根据任务key 重启任务
     */
    Boolean restart(String taskKey);

    /**
     * 程序启动时初始化  {@code ==>} 启动所有正常状态的任务
     */
    void initAllTask(List<ScheduledTaskBean> scheduledTaskBeanList);

    /**
     * 检查任务是否已经启动
     *
     * @param taskKey 任务 key
     */
    Boolean isStart(String taskKey);

}
