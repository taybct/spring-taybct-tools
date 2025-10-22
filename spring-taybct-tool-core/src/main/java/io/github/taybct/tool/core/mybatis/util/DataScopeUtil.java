package io.github.taybct.tool.core.mybatis.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import io.github.taybct.tool.core.bean.ILoginUser;
import io.github.taybct.tool.core.enums.DataScopeFilterType;
import io.github.taybct.tool.core.enums.DataScopeGetNotDealType;
import io.github.taybct.tool.core.enums.DataScopeType;
import io.github.taybct.tool.core.mybatis.config.DataScopeProperties;
import io.github.taybct.tool.core.mybatis.interceptor.DataScopeData;
import io.github.taybct.tool.core.util.SpringUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 数据权限工具类，用于生成权限过滤条件
 *
 * @author XiJieYin <br> 2023/7/6 15:25
 */
@Slf4j
public class DataScopeUtil {

    /**
     * 当前登录的用户
     */
    private Supplier<ILoginUser> userSupplier;

    /**
     * 配置全局参数
     */
    private DataScopeProperties properties;

    /**
     * 一对一权限的模板(按用户过滤)
     */
    private static final String singleConditionUserTemplate =
            " select 1 from {userScopeTable} user_scope_table_alias " +
                    " left join {deptTable} dept_table_s_alias on user_scope_table_alias.{userScopeDeptId} = dept_table_s_alias.{deptId} " +
                    " where user_scope_table_alias.{userScopeField} = {userId} ";
    /**
     * 一对一权限(扩展)的模板(按用户过滤)
     */
    public static final String singleExpConditionUserTemplate =
            " select 1 from {userScopeTable} user_scope_table_alias " +
                    " left join {extensionScopeTable} exp_table_s_alias on user_scope_table_alias.{userScopeDeptId} = exp_table_s_alias.{extensionScopeField} " +
                    " left join {deptTable} dept_table_s_alias on exp_table_s_alias.{extensionScopeDeptId} = dept_table_s_alias.{deptId} " +
                    " where user_scope_table_alias.{userScopeField} = {userId} ";

    /**
     * 一对一权限的模板(按角色过滤)
     */
    private static final String singleConditionRoleTemplate =
            " select 1 from {roleScopeTable} role_scope_table_alias " +
                    " LEFT JOIN {deptTable} dept_table_s_alias on role_scope_table_alias.{roleScopeDeptId} = dept_table_s_alias.{deptId} " +
                    " where exists (select 1 from {roleTable} role_table_alias where role_table_alias.{roleCode} in ({roleCodeArray}) " +
                    " and role_scope_table_alias.{roleScopeField} = role_table_alias.{roleField}) ";
    /**
     * 一对一权限(扩展)的模板(按角色过滤)
     */
    public static final String singleExpConditionRoleTemplate =
            " select 1 from {roleScopeTable} role_scope_table_alias " +
                    " left join {extensionScopeTable} exp_table_s_alias on role_scope_table_alias.{roleScopeDeptId} = exp_table_s_alias.{extensionScopeField} " +
                    " LEFT JOIN {deptTable} dept_table_s_alias on exp_table_s_alias.{extensionScopeDeptId} = dept_table_s_alias.{deptId} " +
                    " where exists (select 1 from {roleTable} role_table_alias where role_table_alias.{roleCode} in ({roleCodeArray}) " +
                    " and role_scope_table_alias.{roleScopeField} = role_table_alias.{roleField}) ";
    /**
     * 按逻辑删除的模板
     */
    private static final String singleConditionTemplateLogic = " and dept_table_s_alias.{logicField} = {notDeletedStatus} ";

    /**
     * 按状态过滤的模板
     */
    public static final String singleConditionTempStatusFilter = " and dept_table_s_alias.{statusField} = {enableStatus} ";

    /**
     * 数据库类型是 pgsql 时使用
     */
    private static final String singleConditionUserTemplatePGSQL =
            " and ( dept_table_s_alias.{deptId} = dept_table_m_alias.{deptId} %s %s) ";

    /**
     * IC（include children）
     */
    private static final String singleConditionUserTemplatePGSQLIC =
            " or CAST(dept_table_s_alias.{deptId} as VARCHAR) = ANY(STRING_TO_ARRAY(dept_table_m_alias.{deptPidAll}, ',')) ";

    /**
     * IP（include parents）
     */
    private static final String singleConditionUserTemplatePGSQLIP =
            "OR CAST(dept_table_m_alias.{deptId} AS VARCHAR) = ANY(STRING_TO_ARRAY(dept_table_s_alias.{deptPidAll}, ','))";

    /**
     * 数据库类型是 MySQL 时使用
     */
    private static final String singleConditionUserTemplateMySQL =
            " and ( dept_table_s_alias.{deptId} = dept_table_m_alias.{deptId} %s %s)";
    /**
     * include children
     */
    private static final String singleConditionUserTemplateMySQLIC =
            " or FIND_IN_SET(dept_table_s_alias.{deptId},dept_table_m_alias.{deptPidAll}) ";
    /**
     * include parents
     */
    private static final String singleConditionUserTemplateMySQLIP =
            " or FIND_IN_SET(dept_table_m_alias.{deptId},dept_table_s_alias.{deptPidAll}) ";

    /**
     * 数据库类型是 ORACLE 时使用
     */
    private static final String singleConditionUserTemplateORACLE =
            " and ( dept_table_s_alias.{deptId} = dept_table_m_alias.{deptId} %s %s)";
    /**
     * include children
     */
    private static final String singleConditionUserTemplateORACLEIC =
            " or INSTR(dept_table_m_alias.{deptPidAll}, TO_CHAR(dept_table_s_alias.{deptId}))>0 ";
    /**
     * include parents
     */
    private static final String singleConditionUserTemplateORACLEIP =
            " or INSTR(dept_table_s_alias.{deptPidAll}, TO_CHAR(dept_table_m_alias.{deptId}))>0 ";

    /**
     * 数据库类型是 pgsql 时使用
     */
    private static final String singleConditionRoleTemplatePGSQL =
            " and (role_scope_table_alias.dept_id = dept_table_m_alias.{deptId} %s %s )";
    /**
     * include children
     */
    private static final String singleConditionRoleTemplatePGSQLIC =
            "or CAST(role_scope_table_alias.dept_id as VARCHAR) = ANY(STRING_TO_ARRAY(dept_table_m_alias.{deptPidAll}, ','))";
    /**
     * include parents
     */
    private static final String singleConditionRoleTemplatePGSQLIP =
            "OR CAST(dept_table_m_alias.{deptId} AS VARCHAR) = ANY(STRING_TO_ARRAY(dept_table_s_alias.{deptPidAll}, ','))";

    /**
     * 数据库类型是 MySQL 时使用
     */
    private static final String singleConditionRoleTemplateMySQL =
            " and (role_scope_table_alias.dept_id = dept_table_m_alias.{deptId} %s %s)";
    /**
     * include children
     */
    private static final String singleConditionRoleTemplateMySQLIC =
            "or FIND_IN_SET(role_scope_table_alias.dept_id,dept_table_m_alias.{deptPidAll}) ";
    /**
     * include parents
     */
    private static final String singleConditionRoleTemplateMySQLIP =
            " or FIND_IN_SET(dept_table_m_alias.{deptId},dept_table_s_alias.{deptPidAll}) ";

    /**
     * 数据库类型是 ORACLE 时使用
     */
    private static final String singleConditionRoleTemplateORACLE =
            " and (role_scope_table_alias.dept_id = dept_table_m_alias.{deptId} %s %s)";
    /**
     * include children
     */
    private static final String singleConditionRoleTemplateORACLEIC =
            "or INSTR(dept_table_m_alias.{deptPidAll}, TO_CHAR(role_scope_table_alias.dept_id))>0) ";
    /**
     * include parents
     */
    private static final String singleConditionRoleTemplateORACLEIP =
            " or INSTR(dept_table_s_alias.{deptPidAll}, TO_CHAR(dept_table_m_alias.{deptId}))>0 ";

    /**
     * 一对一权限的模板
     */
    private static final String singleConditionSqlTemplate = " select 1 from {deptTable} dept_table_m_alias " +
            " where dept_table_m_alias.{deptId} = {alias}.{field} and( %s %s ) ";

    /**
     * 一对一权限(扩展)的模板
     */
    public static final String singleExpConditionSqlTemplate = " select 1 from {extensionScopeTable} exp_table_m_alias " +
            " left join {deptTable} dept_table_m_alias on exp_table_m_alias.{extensionScopeDeptId} = dept_table_m_alias.{deptId} " +
            " where exp_table_m_alias.{extensionScopeField} = {alias}.{field} and( %s %s ) ";

    /**
     * 一对多权限的模板
     */
    private static final String multiConditionSqlTemplate = " select 1 from {multiTable} multi_table_alias " +
            " left join {deptTable} dept_table_m_alias on multi_table_alias.{multiDeptId} = dept_table_m_alias.{deptId} " +
            " where multi_table_alias.{multiFiled} = {alias}.{field} and( %s %s ) ";

    /**
     * 一对多权限(扩展)的模板
     */
    public static final String multiExpConditionSqlTemplate = " select 1 from {multiTable} multi_table_alias " +
            " left join {extensionScopeTable} exp_table_m_alias on multi_table_alias.{multiDeptId} = exp_table_m_alias.{extensionScopeField} " +
            " left join {deptTable} dept_table_m_alias on exp_table_m_alias.{extensionScopeDeptId} = dept_table_m_alias.{deptId} " +
            " where multi_table_alias.{multiFiled} = {alias}.{field} and( %s %s ) ";

    /**
     * 如果权限过滤并不是只按部门过滤，意味着，可以部门关联是空的情况，这里就要判断该如何过滤
     */
    private static final String notExistDealSingleSqlTemplate = " or {alias}.{field} is null";

    /**
     * 如果权限过滤并不是只按部门过滤，意味着，可以部门关联是空的情况，这里就要判断该如何过滤
     */
    private static final String notExistDealMultiSqlTemplate = " or multi_table_alias.{multiDeptId} is null";

    /**
     * 逻辑删除
     */
    private static final String conditionSqlTemplateLogic = " and dept_table_m_alias.{logicField} = {notDeletedStatus} ";

    /**
     * 按状态过滤的模板
     */
    public static final String conditionSqlTemplateStatusFilter = " and dept_table_m_alias.{statusField} = {enableStatus} ";

    private DataScopeUtil() {
    }

    public DataScopeUtil(DataScopeProperties properties, Supplier<ILoginUser> userSupplier) {
        this.properties = properties;
        this.userSupplier = userSupplier;
    }

    /**
     * 初始化
     *
     * @param properties   全局配置
     * @param userSupplier 因为是需要用户相关的数据权限，所以这里需要提供用户
     * @return DataScopeUtil
     */
    public static DataScopeUtil init(DataScopeProperties properties, Supplier<ILoginUser> userSupplier) {
        return new DataScopeUtil(properties, userSupplier);
    }

    public String getConditionSql(DataScopeData dataScope, DbType dbType) {
        StringBuilder conditionSql = new StringBuilder();

        StringBuilder singleConditionUser = new StringBuilder();
        StringBuilder singleConditionRole = new StringBuilder();
        singleConditionUser.append(dataScope.isExtensible() ? singleExpConditionUserTemplate : singleConditionUserTemplate);
        singleConditionRole.append(dataScope.isExtensible() ? singleExpConditionRoleTemplate : singleConditionRoleTemplate);
        if (dataScope.isLogicDelete()) {
            singleConditionUser.append(singleConditionTemplateLogic);
            singleConditionRole.append(singleConditionTemplateLogic);
        }
        if (dataScope.isStatusFilter()) {
            singleConditionUser.append(singleConditionTempStatusFilter);
            singleConditionRole.append(singleConditionTempStatusFilter);
        }
        String includeChildrenU = "", includeParentsU = "", includeChildrenR = "", includeParentsR = "";
        switch (dbType) {
            case POSTGRE_SQL:
                if (dataScope.isIncludeChildren()) {
                    includeChildrenU = singleConditionUserTemplatePGSQLIC;
                    includeChildrenR = singleConditionRoleTemplatePGSQLIC;
                }
                if (dataScope.isIncludeParents()) {
                    includeParentsU = singleConditionUserTemplatePGSQLIP;
                    includeParentsR = singleConditionRoleTemplatePGSQLIP;
                }
                singleConditionUser.append(String.format(singleConditionUserTemplatePGSQL, includeChildrenU, includeParentsU));
                singleConditionRole.append(String.format(singleConditionRoleTemplatePGSQL, includeChildrenR, includeParentsR));
                break;
            case MYSQL:
                if (dataScope.isIncludeChildren()) {
                    includeChildrenU = singleConditionUserTemplateMySQLIC;
                    includeChildrenR = singleConditionRoleTemplateMySQLIC;
                }
                if (dataScope.isIncludeParents()) {
                    includeParentsU = singleConditionUserTemplateMySQLIP;
                    includeParentsR = singleConditionRoleTemplateMySQLIP;
                }
                singleConditionUser.append(String.format(singleConditionUserTemplateMySQL, includeChildrenU, includeParentsU));
                singleConditionRole.append(String.format(singleConditionRoleTemplateMySQL, includeChildrenR, includeParentsR));
                break;
            case ORACLE:
                if (dataScope.isIncludeChildren()) {
                    includeChildrenU = singleConditionUserTemplateORACLEIC;
                    includeChildrenR = singleConditionRoleTemplateORACLEIC;
                }
                if (dataScope.isIncludeParents()) {
                    includeParentsU = singleConditionUserTemplateORACLEIP;
                    includeParentsR = singleConditionRoleTemplateORACLEIP;
                }
                singleConditionUser.append(String.format(singleConditionUserTemplateORACLE, includeChildrenU, includeParentsU));
                singleConditionRole.append(String.format(singleConditionRoleTemplateORACLE, includeChildrenR, includeParentsR));
                break;
        }
//
//        jdbcTemplate.getDataSource().getConnection().getMetaData().getDatabaseProductName();
        StringBuilder sb = new StringBuilder();
        if (dataScope.getFilterType().equals(DataScopeFilterType.USER)) {
            // 如果是按用户过滤
            sb.append(String.format(" exists(%s) ", singleConditionUser));
        } else if (dataScope.getFilterType().equals(DataScopeFilterType.ROLE)) {
            // 如果是按角色过滤
            sb.append(String.format(" exists(%s) ", singleConditionRole));
        } else if (dataScope.getFilterType().equals(DataScopeFilterType.BOTH)) {
            sb.append(String.format(" exists(%s) ", singleConditionUser));
            sb.append(String.format(" or exists(%s) ", singleConditionRole));
        }

        if (dataScope.getType().equals(DataScopeType.SINGLE)) {
            conditionSql.append(String.format(dataScope.isExtensible() ? singleExpConditionSqlTemplate : singleConditionSqlTemplate
                    , sb
                    , dataScope.getNotExistDealType().equals(DataScopeGetNotDealType.ALLOW) ? notExistDealSingleSqlTemplate : ""));
        } else {
            conditionSql.append(String.format(dataScope.isExtensible() ? multiExpConditionSqlTemplate : multiConditionSqlTemplate
                    , sb
                    , dataScope.getNotExistDealType().equals(DataScopeGetNotDealType.ALLOW) ? notExistDealMultiSqlTemplate : ""));
        }
        if (dataScope.isLogicDelete()) {
            conditionSql.append(conditionSqlTemplateLogic);
        }
        if (dataScope.isStatusFilter()) {
            conditionSql.append(conditionSqlTemplateStatusFilter);
        }

        return replaceQueryCondition(replacePlaceholder(conditionSql.toString(), dataScope));
    }

    /**
     * 替换模板里面的点位符
     *
     * @param template  模板
     * @param dataScope 注解
     * @return string
     */
    public String replacePlaceholder(String template, DataScopeData dataScope) {
        return template.replaceAll("[{]userScopeTable[}]", StrUtil.emptyToDefault(dataScope.getUserScopeTable(), properties.getUserScopeTable()))
                .replaceAll("[{]userScopeField[}]", StrUtil.emptyToDefault(dataScope.getUserScopeField(), properties.getUserScopeField()))
                .replaceAll("[{]userScopeDeptId[}]", StrUtil.emptyToDefault(dataScope.getUserScopeDeptId(), properties.getUserScopeDeptId()))
                .replaceAll("[{]roleScopeTable[}]", StrUtil.emptyToDefault(dataScope.getRoleScopeTable(), properties.getRoleScopeTable()))
                .replaceAll("[{]roleScopeField[}]", StrUtil.emptyToDefault(dataScope.getRoleScopeField(), properties.getRoleScopeField()))
                .replaceAll("[{]roleScopeDeptId[}]", StrUtil.emptyToDefault(dataScope.getRoleScopeDeptId(), properties.getRoleScopeDeptId()))
                .replaceAll("[{]roleTable[}]", StrUtil.emptyToDefault(dataScope.getRoleTable(), properties.getRoleTable()))
                .replaceAll("[{]roleField[}]", StrUtil.emptyToDefault(dataScope.getRoleField(), properties.getRoleField()))
                .replaceAll("[{]roleCode[}]", StrUtil.emptyToDefault(dataScope.getRoleCode(), properties.getRoleCode()))
                .replaceAll("[{]deptTable[}]", StrUtil.emptyToDefault(dataScope.getDeptTable(), properties.getDeptTable()))
                .replaceAll("[{]deptId[}]", StrUtil.emptyToDefault(dataScope.getDeptId(), properties.getDeptId()))
                .replaceAll("[{]deptPidAll[}]", StrUtil.emptyToDefault(dataScope.getDeptPidAll(), properties.getDeptPidAll()))
                .replaceAll("[{]multiTable[}]", StrUtil.emptyToDefault(dataScope.getMultiTable(), properties.getMultiTable()))
                .replaceAll("[{]multiFiled[}]", StrUtil.emptyToDefault(dataScope.getMultiFiled(), properties.getMultiFiled()))
                .replaceAll("[{]multiDeptId[}]", StrUtil.emptyToDefault(dataScope.getMultiDeptId(), properties.getMultiDeptId()))
                .replaceAll("[{]alias[}]", dataScope.getAlias())
                .replaceAll("[{]field[}]", dataScope.getField())
                .replaceAll("[{]logicField[}]", StrUtil.emptyToDefault(dataScope.getLogicField(), properties.getLogicField()))
                .replaceAll("[{]notDeletedStatus[}]", StrUtil.emptyToDefault(dataScope.getNotDeletedStatus(), properties.getNotDeletedStatus()))
                .replaceAll("[{]extensionScopeTable[}]", StrUtil.emptyToDefault(dataScope.getExtensionScopeTable(), properties.getExtensionScopeTable()))
                .replaceAll("[{]extensionScopeField[}]", StrUtil.emptyToDefault(dataScope.getExtensionScopeField(), properties.getExtensionScopeField()))
                .replaceAll("[{]extensionScopeDeptId[}]", StrUtil.emptyToDefault(dataScope.getExtensionScopeDeptId(), properties.getExtensionScopeDeptId()))
                .replaceAll("[{]statusField[}]", StrUtil.emptyToDefault(dataScope.getStatusField(), properties.getStatusField()))
                .replaceAll("[{]enableStatus[}]", StrUtil.emptyToDefault(dataScope.getEnableStatus(), properties.getEnableStatus()))
                ;
    }

    /**
     * 替换查询条件，如：用户id，角色 code 等
     *
     * @return string
     */
    public String replaceQueryCondition(String template) {
        ILoginUser loginUser = userSupplier.get();
        Set<String> roleCodeSet = loginUser.checkAuthorities();
        String roleCodeArray = CollectionUtil.join(roleCodeSet.stream().map(code -> "'" + code + "'").collect(Collectors.toSet()), ",");
        return template.replaceAll("[{]userId[}]", loginUser.getUserId().toString())
                .replaceAll("[{]roleCodeArray[}]", roleCodeArray);
    }

    /**
     * 获取数据源数据库类型
     *
     * @return DbType
     */
    @SneakyThrows
    public static DbType getDbType() {
        DataSource dataSource = SpringUtil.getBean(DataSource.class);
        DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
        return DbType.getDbType(metaData.getDatabaseProductName().toLowerCase());
    }

}


/*
示例的过滤查询
select * from sys_user su
where
su.is_deleted = 0
and su.id <> 1660854347478024193
and su.id <> 1
and EXISTS(
	select 1 from sys_user_dept sudu
	left join sys_dept sd on sd.is_deleted = 0 and sudu.dept_id = sd.id
	where sudu.user_id = su.id
	and (
		EXISTS (
			select 1 from sys_user_dept sud
			left join sys_dept sdi on sdi.is_deleted = 0 and sud.dept_id = sdi.id
			where sud.user_id = 1660854347478024193
			and ( sud.dept_id = sd.id or CAST(sud.dept_id as VARCHAR) = ANY(STRING_TO_ARRAY(sd.pid_all, ',')) )
		)
		or EXISTS(
			select 1 from sys_role_dept srd
			LEFT JOIN sys_dept sdi on sdi.is_deleted = 0 and srd.dept_id = sdi.ID
			where exists (select 1 from sys_role sr where sr.code = 'AAA' and srd.role_id = sr.id)
			and (srd.dept_id = sd.id or CAST(srd.dept_id as VARCHAR) = ANY(STRING_TO_ARRAY(sd.pid_all, ',')))
		)
	)
)

select * from sys_dept sd
where sd.is_deleted = 0
and (
	EXISTS (
		select 1 from sys_user_dept sud
		left join sys_dept sdi on sdi.is_deleted = 0 and sud.dept_id = sdi.id
		where sud.user_id = 1660854347478024193
		and ( sud.dept_id = sd.id or CAST(sud.dept_id as VARCHAR) = ANY(STRING_TO_ARRAY(sd.pid_all, ',')) )
	)
	or EXISTS(
		select 1 from sys_role_dept srd
		LEFT JOIN sys_dept sdi on sdi.is_deleted = 0 and srd.dept_id = sdi.ID
		where exists (select 1 from sys_role sr where sr.code = 'AAA' and srd.role_id = sr.id)
		and (srd.dept_id = sd.id or CAST(srd.dept_id as VARCHAR) = ANY(STRING_TO_ARRAY(sd.pid_all, ',')))
	)
)
 */