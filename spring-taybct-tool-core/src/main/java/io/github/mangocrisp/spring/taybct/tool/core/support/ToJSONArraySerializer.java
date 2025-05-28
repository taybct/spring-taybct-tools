package io.github.mangocrisp.spring.taybct.tool.core.support;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONArray;
import org.postgresql.util.PGobject;

import java.io.Serial;

/**
 * <pre>
 * 转换成 JSONArray
 * </pre>
 *
 * @author XiJieYin
 * @since 2025/5/28 15:47
 */
public class ToJSONArraySerializer extends ObjectSerializer<JSONArray> {

    public static final ToJSONArraySerializer instance = new ToJSONArraySerializer();

    @Serial
    private static final long serialVersionUID = 327046210918926431L;

    public ToJSONArraySerializer() {
        super(Object.class);
    }

    public ToJSONArraySerializer(Class<?> handledType) {
        super(handledType);
    }

    @Override
    public JSONArray valueConvert(Object o) {
        if (ObjectUtil.isEmpty(o)) {
            return null;
        }
        if (o instanceof PGobject pGobject) {
            if (pGobject.getType().equalsIgnoreCase("json")) {
                return JSONArray.parseArray(pGobject.getValue());
            }
        }
        return JSONArray.parse(Convert.toStr(o));
    }
}
