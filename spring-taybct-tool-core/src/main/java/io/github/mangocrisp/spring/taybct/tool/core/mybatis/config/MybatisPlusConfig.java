package io.github.mangocrisp.spring.taybct.tool.core.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import io.github.mangocrisp.spring.taybct.tool.core.bean.ISecurityUtil;
import io.github.mangocrisp.spring.taybct.tool.core.config.DataScopeCondition;
import io.github.mangocrisp.spring.taybct.tool.core.config.DataScopeCustom;
import io.github.mangocrisp.spring.taybct.tool.core.ds.DBHelper;
import io.github.mangocrisp.spring.taybct.tool.core.interceptor.RecordHistoryMethodInterceptor;
import io.github.mangocrisp.spring.taybct.tool.core.mybatis.handle.*;
import io.github.mangocrisp.spring.taybct.tool.core.mybatis.interceptor.MyBatisExtraParamsInterceptor;
import io.github.mangocrisp.spring.taybct.tool.core.mybatis.prop.TenantSupplierProperties;
import io.github.mangocrisp.spring.taybct.tool.core.mybatis.util.DataScopeUtil;
import io.github.mangocrisp.spring.taybct.tool.core.mybatis.util.JDBCFieldUtil;
import io.github.mangocrisp.spring.taybct.tool.core.service.IDBOperateHistoryService;
import io.github.mangocrisp.spring.taybct.tool.core.service.impl.DBOperateHistoryServiceJdbcImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * MyBatis-plus 配置类
 *
 * @author xijieyin <br> 2022/8/4 17:49
 * @since 1.0.0
 */
@AutoConfiguration
// 指定要扫描的Mapper类的包的路径
@MapperScan({"io.github.mangocrisp.spring.taybct.**.mapper"})
@ConditionalOnClass({GlobalConfig.class, JdbcTemplate.class})
@EnableConfigurationProperties({DataScopeProperties.class, TenantSupplierProperties.class})
@Slf4j
public class MybatisPlusConfig {

    /**
     * 安全工具，用来获取用户信息
     */
    @Resource
    private ISecurityUtil securityUtil;

    @Bean
    @ConditionalOnMissingBean
    public ITenantSupplier tenantSupplier() {
        return new ITenantSupplier() {
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public TenantLineHandler tenantLineHandler(ITenantSupplier tenantSupplier) {
        return new DefaultTenantLineHandler(tenantSupplier);
    }

    /**
     * 新增多租户插件配置<br>
     * 一缓和二缓遵循mybatis的规则,需要设置 MybatisConfiguration#useDeprecatedExecutor = false 避免缓存万一出现问题
     *
     * @return MybatisPlusInterceptor
     * @author xijieyin <br> 2022/8/4 17:55
     * @since 1.0.0
     */
    @Bean
    @Order(0)
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(TenantLineHandler tenantLineHandler
            , ITenantSupplier tenantSupplier) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        if (tenantSupplier.getEnable()) {
            // 租户插件
            interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(tenantLineHandler));
        }

        // 如果用了分页插件注意先 add TenantLineInnerInterceptor 再 add PaginationInnerInterceptor

        // 这里如果不指定 DbType，mybatis plus 会自动去找方言，这很人性
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        // sql性能规范
        // 这个插件与 逻辑删除冲突，一定要求逻辑删除的字段也加上索引。。。
        //interceptor.addInnerInterceptor(new IllegalSQLInnerInterceptor());
        // 防止全表更新与删除
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }

    /**
     * 因为 MyBatis 的拦截器是先入后出，所以自定义的 {@linkplain org.apache.ibatis.plugin.Interceptor Interceptor}
     * 要比 MyBatisPlus 的后注入 spring 容器
     * <br>，因为 MyBatisPlus 在做拦截的时候需要截取原 SQL 里面的占位做处理，如果要添加额外参数的话，MyBatisPlus 处理的时候会报错说找不到对应的参数
     * ，所以我们要先把数据设置进去
     *
     * @return {@linkplain org.apache.ibatis.plugin.Interceptor Interceptor}
     */
    @Bean
    @Order(1)
    public MyBatisExtraParamsInterceptor myBatisExtraParamsInterceptor() {
        return new MyBatisExtraParamsInterceptor();
    }

    /**
     * 数据库言配置<br>
     * 这个需要你在 mapper.xml 里面使用 _databaseId 来区分数据库
     *
     * @return DatabaseIdProvider
     * @author xijieyin <br> 2022/8/4 17:56
     * @since 1.0.0
     */
    @Bean
    @ConditionalOnMissingBean
    public DatabaseIdProvider databaseIdProvider() {
        VendorDatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
        Properties properties = new Properties();
        properties.put("Oracle", "oracle");
        properties.put("MySQL", "mysql");
        properties.setProperty("PostgreSQL", "postgresql");
        properties.setProperty("DB2", "db2");
        properties.setProperty("SQL Server", "sqlserver");
        databaseIdProvider.setProperties(properties);
        return databaseIdProvider;
    }

    @Bean
    @ConditionalOnMissingBean(name = "dataScopeCondition")
    public DataScopeCondition dataScopeCondition() {
        return loginUser -> true;
    }

    @Bean
    @ConditionalOnMissingBean(name = "dataScopeCustom")
    public DataScopeCustom dataScopeCustom() {
        return dataScope -> null;
    }

    @Bean
    public DataScopeUtil dataScopeUtil(DataScopeProperties properties) {
        return DataScopeUtil.init(properties, securityUtil::getLoginUser);
    }

    /**
     * 添加用户信息处理
     *
     * @param myBatisExtraParamsInterceptor 额外拦截器
     * @return MyBatisExtraParamsHandle
     */
    @Bean
    public MyBatisExtraParamsHandle myBatisExtraParamsHandle(DataScopeProperties properties
            , @Qualifier(value = "dataScopeCondition") DataScopeCondition dataScopeCondition
            , @Qualifier(value = "dataScopeCustom") DataScopeCustom dataScopeCustom
            , MyBatisExtraParamsInterceptor myBatisExtraParamsInterceptor
            , Environment env) {
        MyBatisExtraParamsHandle handle = new MyBatisExtraParamsHandle() {
            @Override
            public Map<String, Object> apply(MappedStatement mappedStatement, Map<String, Object> stringObjectMap) {
                try {
                    stringObjectMap.put("_login_user_", securityUtil.getLoginUser().getPayload());
                } catch (Exception e) {
                    log.trace("获取用户失败！", e);
                }
                try (Connection connection = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection()) {
                    DbType dbType = DBHelper.getDbType(connection, DbType.POSTGRE_SQL);
                    stringObjectMap.put("_db_type_db_", dbType.getDb());
                } catch (Exception e) {
                    log.trace("设置数据源类型失败！", e);
                }
                return stringObjectMap;
            }
        };
        // 添加用户信息
        myBatisExtraParamsInterceptor.addHandler(handle);
        // 添加数据权限拦截
        DataScopeExtraParamsHandle dataScopeInterceptor = new DataScopeExtraParamsHandle(securityUtil::getLoginUser
                , properties
                , dataScopeCondition
                , dataScopeCustom);
        myBatisExtraParamsInterceptor.addHandler(dataScopeInterceptor);
        myBatisExtraParamsInterceptor.addHandler(new UniqueDeleteLogicExtraHandle(env));
        return handle;
    }

    @Bean
    @ConditionalOnMissingBean
    public IJDBCFieldMapper jdbcFieldMapper() {
        return new IJDBCFieldMapper() {
            @Override
            public ConcurrentHashMap<Class<?>, BiFunction<DbType, Object, String>> j2d() {
                // 类型判断
                ConcurrentHashMap<Class<?>, BiFunction<DbType, Object, String>> typeMap = new ConcurrentHashMap<>();
                typeMap.put(String.class, JDBCFieldUtil.J2D::varchar);
                typeMap.put(Long.class, JDBCFieldUtil.J2D::number);
                typeMap.put(Integer.class, JDBCFieldUtil.J2D::number);
                typeMap.put(Double.class, JDBCFieldUtil.J2D::number);
                typeMap.put(Float.class, JDBCFieldUtil.J2D::number);
                typeMap.put(Date.class, JDBCFieldUtil.J2D::dateTime);
                typeMap.put(LocalDateTime.class, JDBCFieldUtil.J2D::dateTime);
                typeMap.put(LocalDate.class, JDBCFieldUtil.J2D::dateTime);
                return typeMap;
            }
        };
    }

    ;

    @Bean
    @ConditionalOnMissingBean(DeleteLogicExtraHandle.class)
    public DeleteLogicExtraHandle deleteLogicExtraHandle(MyBatisExtraParamsInterceptor myBatisExtraParamsInterceptor, IJDBCFieldMapper jdbcFieldMapper) {
        DeleteLogicExtraHandle deleteLogicExtraHandle = new DeleteLogicExtraHandle();
        // TODO 类型还需要再继续添加
        deleteLogicExtraHandle.getAssembleFieldValueMap().putAll(jdbcFieldMapper.j2d());
        myBatisExtraParamsInterceptor.addHandler(deleteLogicExtraHandle);
        return deleteLogicExtraHandle;
    }

    @Bean
    @ConditionalOnMissingBean(IDBOperateHistoryService.class)
    public IDBOperateHistoryService historyService(@Nullable JdbcTemplate jdbcTemplate) {
        return new DBOperateHistoryServiceJdbcImpl(jdbcTemplate);
    }

    @Bean
    public DefaultPointcutAdvisor recordHistoryPointcutAdvisor(IDBOperateHistoryService historyService) {
        RecordHistoryMethodInterceptor methodInterceptor = new RecordHistoryMethodInterceptor(historyService, securityUtil);
        // 匹配一个切点，这里使用注解
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("@annotation(io.github.mangocrisp.spring.taybct.tool.core.annotation.RecordHistory)");
        // 增强
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(pointcut);
        // 增强的方法
        advisor.setAdvice(methodInterceptor);
        return advisor;
    }
}
