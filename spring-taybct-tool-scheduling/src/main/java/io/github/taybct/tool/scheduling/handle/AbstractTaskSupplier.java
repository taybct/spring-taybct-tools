package io.github.taybct.tool.scheduling.handle;

import io.github.taybct.tool.core.annotation.Scheduler;
import io.github.taybct.tool.core.util.SpringUtil;
import io.github.taybct.tool.core.util.StringUtil;
import io.github.taybct.tool.scheduling.job.IScheduledTaskJob;
import io.github.taybct.tool.scheduling.prop.ScheduledProperties;
import io.github.taybct.tool.scheduling.prop.ScheduledTaskBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 默认的任务提供者，这里默认是使用配置文件 properties/yml 来配置，你可以继承，然后通过数据库来动态管理
 *
 * @author xijieyin <br> 2022/10/31 17:17
 * @since 1.1.0
 */
@Slf4j
public abstract class AbstractTaskSupplier implements ITaskSupplier {

    @Override
    public Map<String, IScheduledTaskJob> configTaskJob() {
        return Optional.ofNullable(SpringUtil.getContext()).map(context -> {
            Map<String, IScheduledTaskJob> concurrentHashMap = new HashMap<>();
            context.getBeansWithAnnotation(Scheduler.class)
                    .forEach((k, v) -> {
                        if (IScheduledTaskJob.class.isAssignableFrom(v.getClass())) {
                            Scheduler annotation = v.getClass().getAnnotation(Scheduler.class);
                            String taskName = k;
                            if (StringUtil.isNotEmpty(annotation.value())) {
                                taskName = annotation.value();
                            }
                            concurrentHashMap.put(taskName, (IScheduledTaskJob) v);
                        }
                    });
            return concurrentHashMap;
        }).orElseGet(ConcurrentHashMap::new);
    }

    @Autowired
    protected ScheduledProperties scheduledProperties;

    @Override
    public ScheduledTaskBean getByKey(String taskKey) {
        return scheduledProperties.getTasks().get(taskKey);
    }

    @Override
    public List<ScheduledTaskBean> getAllTask() {
        return scheduledProperties.getTasks().values().stream().
                sorted(Comparator.comparingInt(ScheduledTaskBean::getSort)).collect(Collectors.toList());
    }

    @Override
    public List<ScheduledTaskBean> getAllNeedStartTask() {
        return getAllTask().stream().filter(s -> s.getAutoStart() == 1).collect(Collectors.toList());
    }

}
