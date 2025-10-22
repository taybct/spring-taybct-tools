package io.github.taybct.tool.core.support;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONObject;
import org.postgresql.util.PGobject;

import java.io.Serial;

/**
 * <pre>
 * 转换成 JSONObject
 * </pre>
 *
 * @author XiJieYin
 * @since 2025/5/28 15:47
 */
public class ToJSONObjectSerializer extends ObjectSerializer<JSONObject> {

    public static final ToJSONObjectSerializer instance = new ToJSONObjectSerializer();
    @Serial
    private static final long serialVersionUID = 2336542629402174543L;

    public ToJSONObjectSerializer() {
        super(Object.class);
    }

    public ToJSONObjectSerializer(Class<?> handledType) {
        super(handledType);
    }

    @Override
    public JSONObject valueConvert(Object o) {
        if (ObjectUtil.isEmpty(o)) {
            return null;
        }
        if (o instanceof PGobject pGobject) {
            if (pGobject.getType().equalsIgnoreCase("json")) {
                return JSONObject.parseObject(pGobject.getValue());
            }
        }
        return JSONObject.parse(Convert.toStr(o));
    }
}
