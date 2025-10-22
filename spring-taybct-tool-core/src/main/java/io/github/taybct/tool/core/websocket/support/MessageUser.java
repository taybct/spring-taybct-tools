package io.github.taybct.tool.core.websocket.support;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.github.taybct.tool.core.websocket.enums.MessageUserType;

import java.io.Serializable;

/**
 * <pre>
 * 消息用户
 * </pre>
 *
 * @param messageUserType 用户类型
 * @param userId          用户 id
 * @param sessionId       session id
 * @author XiJieYin
 * @since 2025/6/13 15:36
 */
public record MessageUser(MessageUserType messageUserType, @JsonSerialize(using = ToStringSerializer.class) Long userId,
                          String sessionId) implements Serializable {

}
