package io.github.taybct.tool.core.message;

import cn.hutool.core.lang.UUID;
import io.github.taybct.tool.core.message.apilog.ApiLogDTO;
import io.github.taybct.tool.core.message.scheduledLogCentralized.ScheduledLogCentralizedDTO;

/**
 * <pre>
 * 框架默认的消息类型
 * </pre>
 *
 * @author XiJieYin
 * @since 2024/9/1 22:02
 */
public interface DefaultMessageType {

    /**
     * 接口日志消息
     */
    MessageType API_LOG = new MessageType(ApiLogDTO.class, "API_LOG-", UUID.fastUUID().toString(true), ".json");
    /**
     * 定时任务日志管理
     */
    MessageType SCHEDULED_LOG_CENTRALIZED = new MessageType(ScheduledLogCentralizedDTO.class, "SCHEDULED_LOG_CENTRALIZED-", UUID.fastUUID().toString(true), ".json");

}
