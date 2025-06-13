package io.github.mangocrisp.spring.taybct.tool.core.websocket.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <pre>
 * 消息用户类型
 * </pre>
 *
 * @author XiJieYin
 * @since 2025/6/13 15:36
 */
@Getter
@AllArgsConstructor
public final class MessageUserType {

    /**
     * 用户
     */
    public static final MessageUserType USER = new MessageUserType("USER", 1, "用户");
    /**
     * 群组
     */
    public static final MessageUserType GROUP = new MessageUserType("GROUP", 0, "群组");

    /**
     * 类型 code
     */
    private final String code;
    /**
     * 类型 code
     */
    private final int intCode;
    /**
     * 描述
     */
    private final String description;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        MessageUserType that = (MessageUserType) obj;
        return this.getIntCode() == that.intCode;
    }

    @Override
    public int hashCode() {
        return this.getCode().hashCode();
    }
}
