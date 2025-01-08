package io.github.mangocrisp.spring.taybct.tool.security.config;

import io.github.mangocrisp.spring.taybct.tool.core.bean.ILoginUser;
import io.github.mangocrisp.spring.taybct.tool.core.bean.ISecurityUtil;
import io.github.mangocrisp.spring.taybct.tool.core.constant.CacheConstants;
import io.github.mangocrisp.spring.taybct.tool.core.constant.ISysParamsObtainService;
import io.github.mangocrisp.spring.taybct.tool.core.mybatis.handle.ITenantSupplier;
import io.github.mangocrisp.spring.taybct.tool.core.mybatis.prop.TenantSupplierProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * 如果没有配置特定的租户配置，就会使用这个基于安全模块的租户获取方式，从用户的登录信息中去获取租户信息
 *
 * @author xijieyin <br> 2022/8/4 18:01
 * @since 1.0.0
 */
@Data
@RequiredArgsConstructor
public class SecurityTenantSupplier implements ITenantSupplier {

    private static final Logger log = LoggerFactory.getLogger(SecurityTenantSupplier.class);
    final ISecurityUtil securityUtil;

    final ISysParamsObtainService sysParamsObtainService;

    final TenantSupplierProperties tenantSupplierProperties;

    @Override
    public String getTenantId() {
        try {
            ILoginUser loginUser = securityUtil.getLoginUser();
            return loginUser != null
                    ? (loginUser.getTenantId() != null ? loginUser.getTenantId() : getDefaultTenantId())
                    : getDefaultTenantId();
        } catch (Exception e) {
            log.trace("如果没有获取到登录用户就给默认的租户", e);
            return getDefaultTenantId();
        }
    }

    public String getDefaultTenantId() {
        return sysParamsObtainService.get(CacheConstants.Params.TENANT_ID);
    }

    @Override
    public Set<String> getTenantTables() {
        return tenantSupplierProperties.getTenantTables();
    }

    @Override
    public String getTenantIdColumn() {
        return tenantSupplierProperties.getTenantIdColumn();
    }

    @Override
    public Boolean getEnable() {
        return tenantSupplierProperties.getEnable();
    }

}
