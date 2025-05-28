package io.github.mangocrisp.spring.taybct.tool.core.support;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import cn.hutool.core.util.ObjectUtil;

import java.io.IOException;
import java.io.Serial;

/**
 * <pre>
 * 对象序列化
 * </pre>
 *
 * @author XiJieYin
 * @since 2025/5/28 15:31
 */
public abstract class ObjectSerializer<T>
        extends StdSerializer<Object> {

    @Serial
    private static final long serialVersionUID = 1348042087372944298L;

    protected ObjectSerializer(Class<?> t) {
        super(t, false);
    }

    @Override
    public boolean isEmpty(SerializerProvider prov, Object value) {
        return ObjectUtil.isEmpty(valueConvert(value));
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeObject(valueConvert(value));
    }

    public abstract T valueConvert(Object value);

}
