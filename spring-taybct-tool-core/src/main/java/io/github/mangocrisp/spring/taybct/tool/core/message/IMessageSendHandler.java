package io.github.mangocrisp.spring.taybct.tool.core.message;

/**
 * 现在可以支持任意能够发送消息的工具
 * <br>
 * 发送记录日志处理
 *
 * @author xijieyin <br> 2022/8/5 20:21
 * @since 1.0.0
 */
public interface IMessageSendHandler {

    /**
     * 支持的消息类型
     *
     * @return 消息研
     */
    MessageType getMessageType();

    /**
     * 发送消息
     *
     * @param message 消息
     * @return 是否发送成功
     */
    default boolean send(String message) {
        return false;
    }

}
