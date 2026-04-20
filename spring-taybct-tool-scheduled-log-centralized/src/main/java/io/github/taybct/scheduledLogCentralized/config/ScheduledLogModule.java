package io.github.taybct.scheduledLogCentralized.config;

import lombok.Getter;

@Getter
public enum ScheduledLogModule {
    /**
     * 全部通过模式
     */
    ALL,
    /**
     * 白名单模式
     */
    WHITE_LIST,
    /**
     * 黑名单模式
     */
    BLACK_LIST,
    /**
     * 不通过模式
     */
    NONE,
    ;
}
