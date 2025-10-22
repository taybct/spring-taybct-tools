package io.github.taybct.tool.security.config;

import io.github.taybct.tool.core.bean.ISecurityUtil;
import io.github.taybct.tool.core.constant.ISysParamsObtainService;
import io.github.taybct.tool.core.mybatis.handle.ITenantSupplier;
import io.github.taybct.tool.core.mybatis.prop.TenantSupplierProperties;
import io.github.taybct.tool.security.util.DubboSecurityUtil;
import io.github.taybct.tool.security.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 安全配置
 *
 * @author xijieyin
 */
@AutoConfiguration
public class TokenSecurityConfig {

    /**
     * 如果有 dubbo 的调用，就使用  dubbo 的获取方式，来获取登录用户信息
     *
     * @param sysParamsObtainService 系统参数获取 Service
     * @return ISecurityUtil
     */
    @Bean(name = "securityUtil")
    @ConditionalOnClass(name = "org.apache.dubbo.rpc.RpcContext")
    public ISecurityUtil dubboSecurityUtil(ISysParamsObtainService sysParamsObtainService) {
        return new DubboSecurityUtil(sysParamsObtainService);
    }

    /**
     * 安全工具类
     *
     * @param sysParamsObtainService 系统参数获取 Service
     * @return ISecurityUtil
     */
    @Bean(name = "securityUtil")
    @ConditionalOnMissingClass("org.apache.dubbo.rpc.RpcContext")
    public ISecurityUtil securityUtil(ISysParamsObtainService sysParamsObtainService) {
        return new SecurityUtil(sysParamsObtainService);
    }

    @Bean
    public ITenantSupplier tenantSupplier(@Qualifier("securityUtil") ISecurityUtil securityUtil
            , ISysParamsObtainService sysParamsObtainService
            , @Nullable TenantSupplierProperties tenantSupplierProperties) {
        return new SecurityTenantSupplier(securityUtil, sysParamsObtainService, tenantSupplierProperties);
    }

    /**
     * 密码编码器
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
