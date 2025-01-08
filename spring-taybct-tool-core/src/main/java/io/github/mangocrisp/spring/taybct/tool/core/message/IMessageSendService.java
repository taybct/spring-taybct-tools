package io.github.mangocrisp.spring.taybct.tool.core.message;

/**
 * 消息 日志发送 Service
 *
 * @author xijieyin <br> 2023/2/23 下午5:02
 */
public interface IMessageSendService {

    /**
     * 添加处理器
     *
     * @param handler 处理器
     */
    default void addHandler(IMessageSendHandler handler) {
    }

    /**
     * 发送消息
     *
     * @param message 消息体
     */
    void send(Message message);

}
