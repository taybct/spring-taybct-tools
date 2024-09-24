package io.github.mangocrisp.spring.taybct.tool.core.mybatis.interceptor;

import com.baomidou.mybatisplus.annotation.DbType;
import io.github.mangocrisp.spring.taybct.tool.core.annotation.DataScope;
import io.github.mangocrisp.spring.taybct.tool.core.config.DataScopeCondition;
import io.github.mangocrisp.spring.taybct.tool.core.config.DataScopeCustom;
import io.github.mangocrisp.spring.taybct.tool.core.config.DefaultDataScopeCondition;
import io.github.mangocrisp.spring.taybct.tool.core.config.DefaultDataScopeCustom;
import io.github.mangocrisp.spring.taybct.tool.core.enums.DataScopeFilterType;
import io.github.mangocrisp.spring.taybct.tool.core.enums.DataScopeGetNotDealType;
import io.github.mangocrisp.spring.taybct.tool.core.enums.DataScopeType;
import lombok.*;

/**
 * @author XiJieYin <br> 2023/6/21 14:19
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataScopeData {

    /**
     * 用户权限表，例如，数据权限体系是使用部门，那第，这个表就是用户和部门的关联表，例如：sys_user_dept
     */
    private String userScopeTable = "";

    /**
     * 权限字段，按什么过滤权限，也就是 用户权限表里面的某个字段来关联权限，例如，sys_user_dept 表里面的 user_id，是按用户来关联过滤数据权限
     */
    private String userScopeField = "";

    /**
     * 用户权限表用于关联部门 表的字段名，例如：sys_user_dept 表里面的 dept_id
     */
    private String userScopeDeptId = "";


    /**
     * 角色权限表，按角色过滤权限的权限关联表，例如：sys_role_dept
     */
    private String roleScopeTable = "";

    /**
     * 角色权限表权限字段，按什么过滤权限，也就是 角色权限表里面的某个字段来关联权限，例如，sys_role_dept 表里面的 role_id，是按用户来关联过滤数据权限
     */
    private String roleScopeField = "";

    /**
     * 角色权限表用于关联部门 表的字段名，例如：sys_role_dept 表里面的 dept_id
     */
    private String roleScopeDeptId = "";

    /**
     * 角色表，因为一个用户可能是有多个角色的，所以，需要按角色 code 的集合来过滤，就得查询角色表，例如：sys_role
     */
    private String roleTable = "";

    /**
     * 角色表的主键，用来关联，例如：id
     */
    private String roleField = "";

    /**
     * 角色 code，例如：code
     */
    private String roleCode = "";

    /**
     * 部门表 指定部门信息表，这个主要是为了获取上下级关系，例如：sys_dept
     */
    private String deptTable = "";

    /**
     * 部门 表用来关联权限表的主键字段，例如：id
     */
    private String deptId = "";

    /**
     * 部门 表的用于存储所有父级部门的id的字段，例如：pid_all
     */
    private String deptPidAll = "";

    /**
     * 如果是一对多的数据权限，这里需要指定需要过滤的表用于关联权限的关联表名，例如：sys_user_dept ，用来关联用户有多个部门
     * <br> type 为 {@linkplain DataScopeType#MULTI MULTI} 时必填
     */
    private String multiTable = "";

    /**
     * 一对多的数据权限的关联表用于关联需要过滤表的字段，例如 sys_user_dept 表里面的 user_id
     * <br> type 为 {@linkplain DataScopeType#MULTI MULTI} 时必填
     */
    private String multiFiled = "";

    /**
     * 一对多的数据权限的关联表用于关联部门 表的字段，例如 sys_user_dept 表里面的 dept_id
     * <br> type 为 {@linkplain DataScopeType#MULTI MULTI} 时必填
     */
    private String multiDeptId = "";

    /**
     * 需要过滤权限的表的别名，查询的时候需要关联查询，所以这里要指定一个表的别名，例如：要查询用户表就可以用 su 表示
     */
    private String alias;

    /**
     * 需要过滤权限的表的关联字段,例如：用户表的 id
     */
    private String field;

    /**
     * 如果是自定义 mapper.xml 写 sql ，这里要加入过滤数据权限条件的 sql 的字段，可以把这个 sql 加入到自定义的 sql where 条件里面，但是，注意，一般是使用一个
     * {@code and exists(${sql})} 关联起来
     */
    private String sqlField = "";

    /**
     * 按用户，还是按角色过滤，或者取合集过滤，这里默认按用户过滤
     *
     * @see DataScopeFilterType
     */
    private DataScopeFilterType filterType = DataScopeFilterType.USER;

    /**
     * 数据权限类型
     *
     * @see DataScopeType
     */
    private DataScopeType type = DataScopeType.SINGLE;

    /**
     * 部门表 是否是逻辑删除的
     */
    private boolean logicDelete = false;

    /**
     * 逻辑删除字段
     */
    private String logicField = "";

    /**
     * 逻辑删除的未删除状态
     */
    private String notDeletedStatus = "";

    /**
     * 数据库类型，目前仅支持 {@linkplain DbType#POSTGRE_SQL POSTGRE_SQL} | {@linkplain DbType#MYSQL MYSQL} | {@linkplain DbType#ORACLE ORACLE}
     */
    private DbType dbtype = DbType.POSTGRE_SQL;

    /**
     * 是否包含查询权限子集
     */
    private boolean includeChildren = true;

    /**
     * 是否包含查询权限父级，即，把父级也一起返回
     */
    private boolean includeParents = false;

    /**
     * 判断是否要进行数据权限过滤
     */
    private Class<? extends DataScopeCondition> dataScopeCondition = DefaultDataScopeCondition.class;

    /**
     * 自定义过滤规则
     */
    private Class<? extends DataScopeCustom> custom = DefaultDataScopeCustom.class;

    /**
     * 如果查询不到数据权限，或者不存在数据权限关联的时候的处理方式，一般只出现在一对多关联的情况下，比如，可能存在如下的数据关联条件：
     * <br>
     * 这分数据，即可以是按部门来分配权限，也可以单独指定到某个人，那这样，就有可能部门字段是空的，因为他只单独指定到了用户，可以不指定部门
     * <br>
     * 默认不允许，也就是必须要存在数据权限的配置才给通过
     */
    private DataScopeGetNotDealType notExistDealType = DataScopeGetNotDealType.FORBID;

    /**
     * 是否扩展
     */
    private boolean extensible;

    /**
     * 扩展数据范围表
     */
    private String extensionScopeTable;

    /**
     * 扩展数据范围表的主键（用来关联是哪个部门需要扩展范围）
     */
    private String extensionScopeField;

    /**
     * 扩展数据范围表的关联扩展的部门的 id
     */
    private String extensionScopeDeptId;

    /**
     * 是否按状态过滤
     */
    private boolean statusFilter;

    /**
     * 状态字段
     */
    private String statusField;

    /**
     * 组织机构启用状态，默认启动状态是 1
     */
    private String enableStatus;

    /**
     * 根据注解直接生成权限数据
     *
     * @param dataScope 注解
     */
    public DataScopeData(DataScope dataScope) {
        this.userScopeTable = dataScope.userScopeTable();
        this.userScopeField = dataScope.userScopeField();
        this.userScopeDeptId = dataScope.userScopeDeptId();
        this.roleScopeTable = dataScope.roleScopeTable();
        this.roleScopeField = dataScope.roleScopeField();
        this.roleScopeDeptId = dataScope.roleScopeDeptId();
        this.roleTable = dataScope.roleTable();
        this.roleField = dataScope.roleField();
        this.roleCode = dataScope.roleCode();
        this.deptTable = dataScope.deptTable();
        this.deptId = dataScope.deptId();
        this.deptPidAll = dataScope.deptPidAll();
        this.multiTable = dataScope.multiTable();
        this.multiFiled = dataScope.multiFiled();
        this.multiDeptId = dataScope.multiDeptId();
        this.alias = dataScope.alias();
        this.field = dataScope.field();
        this.sqlField = dataScope.sqlField();
        this.filterType = dataScope.filterType();
        this.type = dataScope.type();
        this.logicDelete = dataScope.logicDelete();
        this.logicField = dataScope.logicField();
        this.notDeletedStatus = dataScope.notDeletedStatus();
        this.dbtype = dataScope.dbtype();
        this.includeChildren = dataScope.includeChildren();
        this.includeParents = dataScope.includeParents();
        this.dataScopeCondition = dataScope.dataScopeCondition();
        this.custom = dataScope.custom();
        this.notExistDealType = dataScope.notExistDealType();

        this.extensible = dataScope.extensible();
        this.extensionScopeTable = dataScope.extensionScopeTable();
        this.extensionScopeField = dataScope.extensionScopeField();
        this.extensionScopeDeptId = dataScope.extensionScopeDeptId();
        this.statusFilter = dataScope.statusFilter();
        this.statusField = dataScope.statusField();
        this.enableStatus = dataScope.enableStatus();
    }

    public DataScopeData setUserScopeTable(String userScopeTable) {
        this.userScopeTable = userScopeTable;
        return this;
    }

    public DataScopeData setUserScopeField(String userScopeField) {
        this.userScopeField = userScopeField;
        return this;
    }

    public DataScopeData setUserScopeDeptId(String userScopeDeptId) {
        this.userScopeDeptId = userScopeDeptId;
        return this;
    }

    public DataScopeData setRoleScopeTable(String roleScopeTable) {
        this.roleScopeTable = roleScopeTable;
        return this;
    }

    public DataScopeData setRoleScopeField(String roleScopeField) {
        this.roleScopeField = roleScopeField;
        return this;
    }

    public DataScopeData setRoleScopeDeptId(String roleScopeDeptId) {
        this.roleScopeDeptId = roleScopeDeptId;
        return this;
    }

    public DataScopeData setRoleTable(String roleTable) {
        this.roleTable = roleTable;
        return this;
    }

    public DataScopeData setRoleField(String roleField) {
        this.roleField = roleField;
        return this;
    }

    public DataScopeData setRoleCode(String roleCode) {
        this.roleCode = roleCode;
        return this;
    }

    public DataScopeData setDeptTable(String deptTable) {
        this.deptTable = deptTable;
        return this;
    }

    public DataScopeData setDeptId(String deptId) {
        this.deptId = deptId;
        return this;
    }

    public DataScopeData setDeptPidAll(String deptPidAll) {
        this.deptPidAll = deptPidAll;
        return this;
    }

    public DataScopeData setMultiTable(String multiTable) {
        this.multiTable = multiTable;
        return this;
    }

    public DataScopeData setMultiFiled(String multiFiled) {
        this.multiFiled = multiFiled;
        return this;
    }

    public DataScopeData setMultiDeptId(String multiDeptId) {
        this.multiDeptId = multiDeptId;
        return this;
    }

    public DataScopeData setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public DataScopeData setField(String field) {
        this.field = field;
        return this;
    }

    public DataScopeData setSqlField(String sqlField) {
        this.sqlField = sqlField;
        return this;
    }

    public DataScopeData setFilterType(DataScopeFilterType filterType) {
        this.filterType = filterType;
        return this;
    }

    public DataScopeData setType(DataScopeType type) {
        this.type = type;
        return this;
    }

    public DataScopeData setLogicDelete(boolean logicDelete) {
        this.logicDelete = logicDelete;
        return this;
    }

    public DataScopeData setLogicField(String logicField) {
        this.logicField = logicField;
        return this;
    }

    public DataScopeData setNotDeletedStatus(String notDeletedStatus) {
        this.notDeletedStatus = notDeletedStatus;
        return this;
    }

    public DataScopeData setDbtype(DbType dbtype) {
        this.dbtype = dbtype;
        return this;
    }

    public DataScopeData setIncludeChildren(boolean includeChildren) {
        this.includeChildren = includeChildren;
        return this;
    }

    public DataScopeData setIncludeParents(boolean includeParents) {
        this.includeParents = includeParents;
        return this;
    }

    public DataScopeData setDataScopeCondition(Class<? extends DataScopeCondition> dataScopeCondition) {
        this.dataScopeCondition = dataScopeCondition;
        return this;
    }

    public DataScopeData setCustom(Class<? extends DataScopeCustom> custom) {
        this.custom = custom;
        return this;
    }

    public DataScopeData setNotExistDealType(DataScopeGetNotDealType notExistDealType) {
        this.notExistDealType = notExistDealType;
        return this;
    }
}
