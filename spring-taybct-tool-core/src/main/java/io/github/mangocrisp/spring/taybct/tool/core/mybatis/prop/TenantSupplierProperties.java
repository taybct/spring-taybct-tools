package io.github.mangocrisp.spring.taybct.tool.core.mybatis.prop;

import io.github.mangocrisp.spring.taybct.tool.core.constant.PropertiesPrefixConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Set;

/**
 * 租户配置
 *
 * @author XiJieYin <br> 2024/5/16 9:24
 */
@ConfigurationProperties(PropertiesPrefixConstants.TENANT)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TenantSupplierProperties {

    /**
     * 租户表
     */
    private Set<String> tenantTables = Collections.emptySet();
    /**
     * 租户 id 字段
     */
    private String tenantIdColumn = "tenant_id";
    /**
     * 是否开启租户模式
     *
     * @since 1.0.2
     */
    private Boolean enable = false;

}
