package io.github.mangocrisp.spring.taybct.tool.scheduling.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import io.github.mangocrisp.spring.taybct.tool.scheduling.handle.ITaskSupplier;
import io.github.mangocrisp.spring.taybct.tool.scheduling.job.IScheduledTaskJob;
import io.github.mangocrisp.spring.taybct.tool.scheduling.prop.ScheduledTaskBean;
import io.github.mangocrisp.spring.taybct.tool.scheduling.service.ISchedulingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xijieyin <br> 2022/10/31 16:31
 * @since 1.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class SchedulingTaskServiceImpl implements ISchedulingService {

    /**
     * 任务提供者
     */
    final ITaskSupplier taskSupplier;
    /**
     * 定时任务线程池
     */
    final ThreadPoolTaskScheduler threadPoolTaskScheduler;
    /**
     * 所有定时任务存放Map
     * key :任务 key
     * value:任务实现
     */
    final Map<String, IScheduledTaskJob> scheduledTaskJobMap;
    /**
     * 存放已经启动的任务map
     */
    private final Map<String, ScheduledFuture> scheduledFutureMap = new ConcurrentHashMap<>();

    /**
     * 可重入锁
     */
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public List<ScheduledTaskBean> taskList() {
        log.debug(">>>>>> 获取任务列表开始 >>>>>> ");
        // 查询所有任务 => 未做分页
        List<ScheduledTaskBean> taskBeanList = taskSupplier.getAllTask();
        if (CollectionUtil.isEmpty(taskBeanList)) {
            return new ArrayList<>();
        }

        for (ScheduledTaskBean taskBean : taskBeanList) {
            String taskKey = taskBean.getTaskKey();
            //是否启动标记处理
            taskBean.setStartFlag(this.isStart(taskKey) ? 1 : 0);
        }
        log.debug(">>>>>> 获取任务列表结束 >>>>>> ");
        return taskBeanList;
    }

    @Override
    public boolean tryOnce(String taskKey, Map<String, Object> params) {
        Optional.ofNullable(scheduledTaskJobMap.get(taskKey)).ifPresent(scheduledTaskJob -> {
            log.debug(">>>>>> 尝试执行一次任务 [ {} ] ", taskKey);
            try {
                scheduledTaskJob.run(params);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return true;
    }

    @Override
    public Boolean start(String taskKey) {
        log.debug(">>>>>> 启动任务 {} 开始 >>>>>>", taskKey);
        //添加锁放一个线程启动，防止多人启动多次
        log.debug(">>>>>> 添加任务启动锁完毕");
        // 加锁
        lock.lock();
        try {
            //校验是否已经启动
            if (this.isStart(taskKey)) {
                log.warn(">>>>>> 当前任务已经启动，无需重复启动！");
                return false;
            }
            //校验任务是否存在
            if (!scheduledTaskJobMap.containsKey(taskKey)) {
                return false;
            }
            //根据key获取任务配置信息
            //启动任务
            Optional.ofNullable(taskSupplier.getByKey(taskKey)).ifPresent(this::doStartTask);
        } finally {
            // 释放锁
            lock.unlock();
            log.debug(">>>>>> 释放任务启动锁完毕");
        }
        log.debug(">>>>>> 启动任务 {} 结束 >>>>>>", taskKey);
        return true;
    }

    @Override
    public Boolean stop(String taskKey) {
        log.debug(">>>>>> 进入停止任务 {}  >>>>>>", taskKey);
        //当前任务实例是否存在
        boolean taskStartFlag = scheduledFutureMap.containsKey(taskKey);
        log.debug(">>>>>> 当前任务实例是否存在 {}", taskStartFlag);
        if (taskStartFlag) {
            //获取任务实例
            ScheduledFuture scheduledFuture = scheduledFutureMap.get(taskKey);
            //关闭实例
            scheduledFuture.cancel(true);
        }
        log.debug(">>>>>> 结束停止任务 {}  >>>>>>", taskKey);
        return taskStartFlag;
    }

    @Override
    public Boolean restart(String taskKey) {
        log.debug(">>>>>> 进入重启任务 {}  >>>>>>", taskKey);
        //先停止
        this.stop(taskKey);
        //再启动
        return this.start(taskKey);
    }

    @Override
    public void initAllTask(List<ScheduledTaskBean> scheduledTaskBeanList) {
        log.info("程序启动 ==> 初始化所有任务开始 ！size={}", scheduledTaskBeanList.size());
        if (CollectionUtil.isEmpty(scheduledTaskBeanList)) {
            return;
        }
        for (ScheduledTaskBean scheduledTask : scheduledTaskBeanList) {
            //任务 key
            String taskKey = scheduledTask.getTaskKey();
            //校验是否已经启动
            if (this.isStart(taskKey)) {
                continue;
            }
            //启动任务
            this.doStartTask(scheduledTask);
        }
        log.info("程序启动 ==> 初始化所有任务结束 ！size={}", scheduledTaskBeanList.size());
    }

    /**
     * 任务是否已经启动
     */
    @Override
    public Boolean isStart(String taskKey) {
        //校验是否已经启动
        if (scheduledFutureMap.containsKey(taskKey)) {
            return !scheduledFutureMap.get(taskKey).isCancelled();
        }
        return false;
    }

    /**
     * 执行启动任务
     */
    private void doStartTask(ScheduledTaskBean scheduledTask) {
        //任务key
        String taskKey = scheduledTask.getTaskKey();
        //定时表达式
        String taskCron = scheduledTask.getCron();
        //获取需要定时调度的接口
        Optional.ofNullable(scheduledTaskJobMap.get(taskKey)).ifPresent(scheduledTaskJob -> {
            log.debug(">>>>>> 任务 [ {} ] ,cron={}", scheduledTask.getDescription(), taskCron);
            // 配置任务属性配置
            scheduledTaskJob.setParams(scheduledTask.getParams());
            scheduledTaskJob.setKey(taskKey);
            scheduledTaskJob.setCron(taskCron);
            scheduledTaskJob.setDescription(scheduledTask.getDescription());

            ScheduledFuture scheduledFuture = threadPoolTaskScheduler.schedule(scheduledTaskJob,
                    triggerContext -> {
                        CronTrigger cronTrigger = new CronTrigger(taskCron);
                        return cronTrigger.nextExecution(triggerContext);
                    });
            //将启动的任务放入 map
            scheduledFutureMap.put(taskKey, scheduledFuture);
        });
    }
}
