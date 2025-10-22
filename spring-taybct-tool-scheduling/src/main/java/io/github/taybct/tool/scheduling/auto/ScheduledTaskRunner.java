package io.github.taybct.tool.scheduling.auto;

import io.github.taybct.tool.scheduling.handle.ITaskSupplier;
import io.github.taybct.tool.scheduling.prop.ScheduledTaskBean;
import io.github.taybct.tool.scheduling.service.ISchedulingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;

import java.util.List;

/**
 * 自动启动任务
 *
 * @author xijieyin <br> 2022/11/1 10:31
 * @since 1.1.0
 */
@AutoConfiguration
@Slf4j
@RequiredArgsConstructor
public class ScheduledTaskRunner implements ApplicationRunner {

    final ITaskSupplier taskSupplier;

    final ISchedulingService scheduledTaskService;

    @Override
    public void run(ApplicationArguments args) {
        log.debug(" >>>>>> 项目启动完毕, 开启 => 需要自启的任务 开始!");
        List<ScheduledTaskBean> scheduledTaskBeanList = taskSupplier.getAllNeedStartTask();
        scheduledTaskService.initAllTask(scheduledTaskBeanList);
        log.debug(" >>>>>> 项目启动完毕, 开启 => 需要自启的任务 结束！");
    }

}
