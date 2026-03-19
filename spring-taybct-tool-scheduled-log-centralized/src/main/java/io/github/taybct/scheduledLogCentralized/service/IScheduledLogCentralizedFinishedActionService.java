package io.github.taybct.scheduledLogCentralized.service;

import io.github.taybct.scheduledLogCentralized.enums.ScheduledLogCentralizedStatusEnum;
import io.github.taybct.scheduledLogCentralized.service.entity.ScheduledLogCentralized;

import java.util.Collection;

public interface IScheduledLogCentralizedFinishedActionService {
    /**
     * 插入的消息如果是调用成功需要做什么
     */
    default void afterSuccessed(ScheduledLogCentralized scheduledLogCentralized){
        if (isSuccess(scheduledLogCentralized)) {

        }
    }
    /**
     * 插入的消息如果是调用成功需要做什么
     */
    default void afterSuccessedCollections(Collection<ScheduledLogCentralized> scheduledLogCentralizeds){
        if (isSuccess(scheduledLogCentralizeds)) {

        }
    }

    /**
     * //插入的消息如果是调用失败需要做什么
     * @param scheduledLogCentralized
     */
    default void afterFailed(ScheduledLogCentralized scheduledLogCentralized) {
        if (isFail(scheduledLogCentralized)) {

        }
    }

    /**
     * //插入的消息如果是调用失败需要做什么
     * @param scheduledLogCentralizeds
     */
    default void afterFailedCollections(Collection<ScheduledLogCentralized> scheduledLogCentralizeds) {
        if (isFail(scheduledLogCentralizeds)) {

        }
    }


    default boolean isSuccess(ScheduledLogCentralized log){
        return ScheduledLogCentralizedStatusEnum.SUCCESS.equals(log.getStatus());
    }
    default boolean isSuccess(Collection<ScheduledLogCentralized> logs){
        return logs.stream().anyMatch(item -> {
            return ScheduledLogCentralizedStatusEnum.SUCCESS.equals(item.getStatus());
        });
    }
    default boolean isFail(ScheduledLogCentralized log){
        return ScheduledLogCentralizedStatusEnum.FAILED.equals(log.getStatus());
    }
    default boolean isFail(Collection<ScheduledLogCentralized> logs){
        return logs.stream().anyMatch(item -> {
            return ScheduledLogCentralizedStatusEnum.FAILED.equals(item.getStatus());
        });
    }
}
