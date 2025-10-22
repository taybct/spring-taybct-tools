package io.github.taybct.tool.core.mybatis.handle;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;

/**
 * 默认的多租户处理器
 *
 * @author xijieyin <br> 2022/8/4 17:58
 * @since 1.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultTenantLineHandler implements TenantLineHandler {

    final ITenantSupplier tenantSupplier;

    /**
     * 获取租户 ID 值表达式，只支持单个 ID 值
     * <p>
     *
     * @return 租户 ID 值表达式
     */
    @Override
    public Expression getTenantId() {
        return new StringValue(tenantSupplier.getTenantId());
    }

    /**
     * 获取租户字段名
     * <p>
     * 默认字段名叫: tenant_id
     *
     * @return 租户字段名
     */
    @Override
    public String getTenantIdColumn() {
        return tenantSupplier.getTenantIdColumn();
    }

    /**
     * 根据表名判断是否忽略拼接多租户条件
     * <p>
     * 默认都要进行解析并拼接多租户条件
     *
     * @param tableName 表名
     * @return 是否忽略, true:表示忽略，false:需要解析并拼接多租户条件
     */
    @Override
    public boolean ignoreTable(String tableName) {
        // 没有找到匹配的，说明是需要忽略的表，也就不是租户表
        return tenantSupplier.getTenantTables().stream().noneMatch(tableName::equalsIgnoreCase);
    }
}
