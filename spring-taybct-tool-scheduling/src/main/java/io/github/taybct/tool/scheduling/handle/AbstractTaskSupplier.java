package io.github.taybct.tool.scheduling.handle;

import io.github.taybct.tool.core.annotation.Scheduler;
import io.github.taybct.tool.core.util.AnnotationScanner;
import io.github.taybct.tool.core.util.StringUtil;
import io.github.taybct.tool.scheduling.job.IScheduledTaskJob;
import io.github.taybct.tool.scheduling.prop.ScheduledProperties;
import io.github.taybct.tool.scheduling.prop.ScheduledTaskBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 默认的任务提供者，这里默认是使用配置文件 properties/yml 来配置，你可以继承，然后通过数据库来动态管理
 *
 * @author xijieyin <br> 2022/10/31 17:17
 * @since 1.1.0
 */
@Slf4j
public abstract class AbstractTaskSupplier implements ITaskSupplier {

    @Autowired
    protected ScheduledProperties scheduledProperties;

    @Override
    public Map<String, Class<? extends IScheduledTaskJob>> configTaskJob() {
        Map<String, Class<? extends IScheduledTaskJob>> concurrentHashMap = new HashMap<>();
        scheduledProperties.getPackagesToScan().forEach(packageName -> {
            List<? extends Class<?>> classes = AnnotationScanner.scanWithSpring(packageName, Scheduler.class);
            classes.forEach(clazz -> {
                if (IScheduledTaskJob.class.isAssignableFrom(clazz)) {
                    Scheduler annotation = clazz.getAnnotation(Scheduler.class);
                    String taskName = StringUtil.underlineToHump(clazz.getName());
                    if (StringUtil.isNotEmpty(annotation.value())) {
                        taskName = annotation.value();
                    }
                    concurrentHashMap.put(taskName, (Class<? extends IScheduledTaskJob>) clazz);
                }
            });
        });
        return concurrentHashMap;
    }

    @Override
    public ScheduledTaskBean getByUniKey(String uniKey) {
        return scheduledProperties.getTasks().get(uniKey);
    }

    @Override
    public List<ScheduledTaskBean> getAllTask() {
        scheduledProperties.getTasks().forEach((k, v) -> v.setUniKey(k));
        return scheduledProperties.getTasks().values().stream().
                sorted(Comparator.comparingInt(ScheduledTaskBean::getSort)).collect(Collectors.toList());
    }

    @Override
    public List<ScheduledTaskBean> getAllNeedStartTask() {
        return getAllTask().stream().filter(s -> s.getAutoStart() == 1).collect(Collectors.toList());
    }

}
