package io.github.taybct.tool.cloud.dubbo.serializer;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.dubbo.common.serialize.support.SerializationOptimizer;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 注册被序列化类
 *
 * <br>
 *
 * <a href="https://cn.dubbo.apache.org/zh-cn/overview/mannual/java-sdk/advanced-features-and-usage/performance/serialization/">dubbo 官方文档</a>
 * <p>
 * 要让Kryo和FST完全发挥出高性能，最好将那些需要被序列化的类注册到dubbo系统中
 * </p>
 *
 * @author XiJieYin <br> 2024/5/16 16:02
 */
public class KryoSerializationOptimizerImpl implements SerializationOptimizer {

    public Collection<Class<?>> getSerializableClasses() {
        Set<Class<?>> classes = new LinkedHashSet<>();
        classes.add(JSONObject.class);
        classes.add(JSONArray.class);
        // TODO 可以继续添加很多类型
        return classes;
    }
}
