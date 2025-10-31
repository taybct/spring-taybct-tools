package io.github.taybct.tool.core.message;

import java.io.Serializable;

/**
 * <pre>
 * 消息
 * </pre>
 *
 * @author XiJieYin
 * @since 2024/8/31 23:49
 */
public interface Message extends Serializable {

    /**
     * 是否是实时消息，消息不会缓存本地文件，会立刻发送
     *
     * @return true:是实时消息
     */
    default boolean realTime(){
        return false;
    }

    /**
     * 获取数据
     *
     * @return 消息数据
     */
    default String getPayload() {
        return null;
    }

}
