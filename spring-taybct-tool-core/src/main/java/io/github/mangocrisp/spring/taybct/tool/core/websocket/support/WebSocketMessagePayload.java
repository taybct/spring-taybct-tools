package io.github.mangocrisp.spring.taybct.tool.core.websocket.support;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;

/**
 * <pre>
 * websocket消息
 * </pre>
 *
 * @author XiJieYin
 * @since 2025/3/14 16:55
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(description = "websocket消息体")
@Builder
public class WebSocketMessagePayload implements Serializable {
    @Serial
    private static final long serialVersionUID = -8550725582200274152L;
    /**
     * 消息 id
     */
    @Schema(description = "消息 id")
    private String messageId = IdUtil.randomUUID();
    /**
     * 发送人
     */
    @Schema(description = "发送人")
    private MessageUser fromUser;
    /**
     * 接收人，可以是多人，如果不传则发送给所有人
     */
    @Schema(description = "接收人")
    private LinkedHashSet<MessageUser> toUser;
    /**
     * 发送时间
     */
    @Schema(description = "发送时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime = LocalDateTime.now();
    /**
     * 消息主题
     */
    @Schema(description = "消息主题")
    private String topic;
    /**
     * 消息标题
     */
    @Schema(description = "消息标题")
    private String title;
    /**
     * 消息内容
     */
    @Schema(description = "消息内容")
    private Object content;
    /**
     * 二进制数据，如果指定了这个值，就只发送二进制数据，不发送消息内容
     */
    @Schema(description = "二进制数据")
    @JsonIgnore
    private ByteBuffer byteBuffer;

}
