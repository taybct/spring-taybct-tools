package io.github.mangocrisp.spring.taybct.tool.core.message;

import cn.hutool.core.lang.UUID;
import io.github.mangocrisp.spring.taybct.tool.core.message.apilog.ApiLogDTO;

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
    MessageType API_LOG = new MessageType(ApiLogDTO.class, "API_LOG-", () -> UUID.fastUUID().toString(true), ".json");

}
