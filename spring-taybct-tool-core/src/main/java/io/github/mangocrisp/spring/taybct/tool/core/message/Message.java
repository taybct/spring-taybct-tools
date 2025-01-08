package io.github.mangocrisp.spring.taybct.tool.core.message;

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
     * 获取数据
     *
     * @return 消息数据
     */
    default String getPayload() {
        return null;
    }

}
