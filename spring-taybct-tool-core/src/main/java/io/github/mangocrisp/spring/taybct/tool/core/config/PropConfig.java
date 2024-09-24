package io.github.mangocrisp.spring.taybct.tool.core.config;

import io.github.mangocrisp.spring.taybct.tool.core.constant.PropertiesPrefixConstants;
import io.github.mangocrisp.spring.taybct.tool.core.util.ObjectUtil;
import javax.annotation.Resource;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义参数配置
 * <p>
 * 这个配置类主要是为了配置一些自定义的配置，就是一些自定义的参数这些，为了不到处乱配置，可以配置在一起
 * </p>
 *
 * @author xijieyin <br> 2022/8/5 17:44
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = PropertiesPrefixConstants.PROP_CONFIG)
public class PropConfig extends Prop {

    private static final long serialVersionUID = -7013390887870550846L;
    @Resource
    private Environment environment;

    /**
     * 重写这个方法，让获取数据的时候，从当前的 application name 命名空间（优先级高）获取数据
     * 例如：全局配置 name = aaa，命名空间配置 name = bbb，那么 name 最后获取到的结果是 bbb
     *
     * @return 合并后的数据
     */
    @Override
    public Map<String, Object> getConfig() {
        Map<String, Object> config = super.getConfig();
        if (!appConfig().equals(this)) {
            config.putAll(appConfig().getConfig());
        }
        return config;
    }

    /**
     * 有命名空间的配置
     */
    @Setter
    @Getter
    private Map<String, Prop> apps = new ConcurrentHashMap<>();

    /**
     * 获取配置
     *
     * @param key 配置键
     */
    public Object get(String key) {
        Prop prop = appConfig();
        return prop.getConfig().get(key);
    }

    /**
     * 通过 application name 获取模块模块
     *
     * @return 模块名
     */
    public Prop appConfig() {
        Prop props = appConfig(environment.getProperty("spring.application.name"));
        return ObjectUtil.isNotEmpty(props) ? props : this;
    }

    /**
     * 指定模块名来获取对应的配置
     *
     * @param appName 模块名
     */
    public Prop appConfig(String appName) {
        return apps.get(appName);
    }

}
