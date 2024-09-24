package io.github.mangocrisp.spring.taybct.tool.core.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * <pre>
 * 消息类型
 * </pre>
 *
 * @author XiJieYin
 * @since 2024/9/1 00:48
 */
@AllArgsConstructor
@Getter
public final class MessageType implements Serializable {

    private static final long serialVersionUID = -5127349829385831365L;

    /**
     * 消息类型
     */
    private Class<? extends Message> messageClass;
    /**
     * 生成临时文件的前缀
     */
    private String prefix;
    /**
     * 生成文件名
     */
    private Supplier<String> generateName;
    /**
     * 生成临时文件的后缀
     */
    private String suffix;

    /**
     * 比较是否是这个类型的
     *
     * @param messageClass 消息类型
     * @return 是否
     */
    public boolean supports(Class<? extends Message> messageClass) {
        return this.getMessageClass().equals(messageClass);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        MessageType that = (MessageType) obj;
        return this.getMessageClass().equals(that.getMessageClass())
                && this.getPrefix().equals(that.getPrefix())
                && this.getSuffix().equals(that.getSuffix());
    }

    @Override
    public int hashCode() {
        return this.getMessageClass().hashCode();
    }
}
