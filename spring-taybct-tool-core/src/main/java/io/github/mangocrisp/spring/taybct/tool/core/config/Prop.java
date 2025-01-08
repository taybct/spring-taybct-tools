package io.github.mangocrisp.spring.taybct.tool.core.config;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置属性，以及一些数据类型的转换方法<br>
 * 这个类是要搭配 {@link PropConfig} 来使用，{@link PropConfig#getConfig()} 会去遍历所有配置
 * ，然后去根据 key 去获取对应的值
 *
 * @author xijieyin <br> 2022/8/5 17:46
 * @since 1.0.0
 */
@Data
public class Prop implements Serializable {

    @Serial
    private static final long serialVersionUID = 6996312726474138376L;

    /**
     * 键值配置
     */
    Map<String, Object> config = new ConcurrentHashMap<>();


    /**
     * 获取到配置
     *
     * @param key 根据 key 获取值
     * @return Object
     * @author xijieyin <br> 2022/8/5 18:13
     * @since 1.0.0
     */
    public Object getConfig(String key) {
        // getConfig() 会被重写
        return getConfig().get(key);
    }

}
