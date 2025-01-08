package io.github.mangocrisp.spring.taybct.tool.scheduling.handle;

import io.github.mangocrisp.spring.taybct.tool.scheduling.job.IScheduledTaskJob;
import io.github.mangocrisp.spring.taybct.tool.scheduling.prop.ScheduledTaskBean;
import org.apache.ibatis.annotations.Param;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 任务提供者
 *
 * @author xijieyin <br> 2022/10/31 17:00
 * @since 1.1.0
 */
public interface ITaskSupplier {

    /**
     * 初始化定时任务Map
     * key :任务key
     * value : 执行接口实现
     */
    default Map<String, IScheduledTaskJob> configTaskJob() {
        return Collections.emptyMap();
    }

    /**
     * 根据key 获取 任务信息
     */
    default ScheduledTaskBean getByKey(@Param("taskKey") String taskKey) {
        return null;
    }

    /**
     * 获取程序初始化需要自启的任务信息
     */
    default List<ScheduledTaskBean> getAllNeedStartTask() {
        return Collections.emptyList();
    }

    /**
     * 获取所有任务
     */
    default List<ScheduledTaskBean> getAllTask() {
        return Collections.emptyList();
    }

}
