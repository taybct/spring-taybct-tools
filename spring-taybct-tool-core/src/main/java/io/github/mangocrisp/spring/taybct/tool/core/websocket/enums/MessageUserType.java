package io.github.mangocrisp.spring.taybct.tool.core.websocket.enums;

/**
 * <pre>
 * 消息用户类型
 * </pre>
 *
 * @param code        类型 code
 * @param intCode     类型 code
 * @param description 描述
 * @author XiJieYin
 * @since 2025/6/13 15:36
 */
public record MessageUserType(String code, int intCode, String description) {

    /**
     * 用户
     */
    public static final MessageUserType USER = new MessageUserType("USER", 1, "用户");
    /**
     * 群组
     */
    public static final MessageUserType GROUP = new MessageUserType("GROUP", 0, "群组");

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        MessageUserType that = (MessageUserType) obj;
        return this.intCode() == that.intCode;
    }

    @Override
    public int hashCode() {
        return this.code().hashCode();
    }
}
