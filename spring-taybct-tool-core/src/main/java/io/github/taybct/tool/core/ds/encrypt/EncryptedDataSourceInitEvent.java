package io.github.taybct.tool.core.ds.encrypt;

import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.event.DataSourceInitEvent;
import io.github.taybct.tool.core.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 多数据源解密事件
 */
@RequiredArgsConstructor
@Slf4j
@AutoConfiguration("dataSourceInitEvent")
@ConditionalOnClass(DataSourceInitEvent.class)
@EnableConfigurationProperties(EncryptedDataSourceProperties.class)
public class EncryptedDataSourceInitEvent implements DataSourceInitEvent {
    /**
     * 加密正则
     */
    private static final Pattern ENC_PATTERN = Pattern.compile("^ENC\\((.*)\\)$");

    final EncryptedDataSourceProperties properties;

    private final Map<Class<? extends Function<String, String>>, Function<String, String>> createdDecoder = new HashMap<>();

    @Override
    public void beforeCreate(DataSourceProperty dataSourceProperty) {
        String poolName = dataSourceProperty.getPoolName();
        if (!properties.isEnabled() || StringUtil.isBlank(poolName)) {
            return;
        }
        if (properties.isUrl()) {
            dataSourceProperty.setUrl(decrypt(poolName, dataSourceProperty.getUrl()));
        }
        if (properties.isUsername()) {
            dataSourceProperty.setUsername(decrypt(poolName, dataSourceProperty.getUsername()));
        }
        if (properties.isPassword()) {
            dataSourceProperty.setPassword(decrypt(poolName, dataSourceProperty.getPassword()));
        }
        if (properties.isDriverClassName()) {
            dataSourceProperty.setDriverClassName(decrypt(poolName, dataSourceProperty.getDriverClassName()));
        }
    }

    /**
     * 根据不同的数据湖获取他们的解码器
     *
     * @param poolName 数据源名称
     * @return 解码器
     */
    private Function<String, String> getDecoder(String poolName) {
        Class<? extends Function<String, String>> clazz = properties.getDecryptFunctions().get(poolName);
        if (clazz == null) {
            clazz = properties.getDecryptFunction();
        }
        Function<String, String> decoder;
        if ((decoder = createdDecoder.get(clazz)) != null) {
            return decoder;
        }
        try {
            Constructor<? extends Function<String, String>> declaredConstructor = clazz.getDeclaredConstructor();
            createdDecoder.put(clazz, (decoder = declaredConstructor.newInstance()));
            return decoder;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 InstantiationException e) {
            log.error("Cannot find the decoder, decrypt error,and than will use the original value.", e);
        }
        return s -> s;
    }

    /**
     * 字符串解密
     */
    private String decrypt(String poolName, String cipherText) {
        if (StringUtil.isNotBlank(cipherText)) {
            Matcher matcher = ENC_PATTERN.matcher(cipherText);
            if (matcher.find()) {
                try {
                    return getDecoder(poolName).apply(matcher.group(1));
                } catch (Exception e) {
                    log.error("DynamicDataSourceProperties.decrypt error ", e);
                }
            }
        }
        return cipherText;
    }

    @Override
    public void afterCreate(DataSource dataSource) {
        // 创建完之后，做什么处理什么操作
    }
}
