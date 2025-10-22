package io.github.taybct.tool.core.support;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializerBase;
import org.postgresql.util.PGobject;

import java.io.Serial;

/**
 * <pre>
 * PGobject 转换成字符串
 * </pre>
 *
 * @author XiJieYin
 * @since 2025/5/16 10:26
 */
@JacksonStdImpl
public class JSONToStringSerializer extends ToStringSerializerBase {

    @Serial
    private static final long serialVersionUID = -9071946361931312356L;

    public static final JSONToStringSerializer instance = new JSONToStringSerializer();

    public JSONToStringSerializer() {
        super(Object.class);
    }

    public JSONToStringSerializer(Class<?> handledType) {
        super(handledType);
    }

    @Override
    public String valueToString(Object o) {
        if (ObjectUtil.isEmpty(o)) {
            return null;
        }
        if (o instanceof PGobject pGobject) {
            if (pGobject.getType().equalsIgnoreCase("json")) {
                return pGobject.getValue();
            }
        }
        return Convert.toStr(o, null);
    }
}
