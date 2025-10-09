package io.github.mangocrisp.spring.taybct.tool.core.message;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * <pre>
 * 消息类型
 * </pre>
 *
 * @param messageClass 消息类型
 * @param prefix       生成临时文件的前缀
 * @param generateName 生成文件名
 * @param suffix       生成临时文件的后缀
 * @author XiJieYin
 * @since 2024/9/1 00:48
 */
public record MessageType(Class<? extends Message> messageClass, String prefix, Supplier<String> generateName,
                          String suffix) implements Serializable {

    /**
     * 比较是否是这个类型的
     *
     * @param messageClass 消息类型
     * @return 是否
     */
    public boolean supports(Class<? extends Message> messageClass) {
        return this.messageClass().equals(messageClass);
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
        return this.messageClass().equals(that.messageClass())
                && this.prefix().equals(that.prefix())
                && this.suffix().equals(that.suffix());
    }

    @Override
    public int hashCode() {
        return this.messageClass().hashCode();
    }
}
