package io.github.taybct.tool.core.annotation;

import com.baomidou.mybatisplus.annotation.DbType;
import io.github.taybct.tool.core.config.DataScopeCondition;
import io.github.taybct.tool.core.config.DataScopeCustom;
import io.github.taybct.tool.core.config.DefaultDataScopeCondition;
import io.github.taybct.tool.core.config.DefaultDataScopeCustom;
import io.github.taybct.tool.core.enums.DataScopeFilterType;
import io.github.taybct.tool.core.enums.DataScopeGetNotDealType;
import io.github.taybct.tool.core.enums.DataScopeType;

import java.lang.annotation.*;

/**
 * 数据权限范围
 *
 * @author XiJieYin <br> 2023/6/20 13:55
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited                          //允许子类继承
public @interface DataScope {

    /**
     * 用户权限表，例如，数据权限体系是使用部门，那第，这个表就是用户和部门的关联表，例如：sys_user_dept
     *
     * @return string
     */
    String userScopeTable() default "";

    /**
     * 权限字段，按什么过滤权限，也就是 用户权限表里面的某个字段来关联权限，例如，sys_user_dept 表里面的 user_id，是按用户来关联过滤数据权限
     *
     * @return string
     */
    String userScopeField() default "";

    /**
     * 用户权限表用于关联部门 表的字段名，例如：sys_user_dept 表里面的 dept_id
     *
     * @return string
     */
    String userScopeDeptId() default "";


    /**
     * 角色权限表，按角色过滤权限的权限关联表，例如：sys_role_dept
     *
     * @return string
     */
    String roleScopeTable() default "";

    /**
     * 角色权限表权限字段，按什么过滤权限，也就是 角色权限表里面的某个字段来关联权限，例如，sys_role_dept 表里面的 role_id，是按用户来关联过滤数据权限
     *
     * @return string
     */
    String roleScopeField() default "";

    /**
     * 角色权限表用于关联部门 表的字段名，例如：sys_role_dept 表里面的 dept_id
     *
     * @return string
     */
    String roleScopeDeptId() default "";

    /**
     * 角色表，因为一个用户可能是有多个角色的，所以，需要按角色 code 的集合来过滤，就得查询角色表，例如：sys_role
     *
     * @return string
     */
    String roleTable() default "";

    /**
     * 角色表的主键，用来关联，例如：id
     *
     * @return string
     */
    String roleField() default "";

    /**
     * 角色 code，例如：code
     *
     * @return string
     */
    String roleCode() default "";

    /**
     * 部门表 指定部门信息表，这个主要是为了获取上下级关系，例如：sys_dept
     *
     * @return string
     */
    String deptTable() default "";

    /**
     * 部门 表用来关联权限表的主键字段，例如：id
     *
     * @return string
     */
    String deptId() default "";

    /**
     * 部门 表的用于存储所有父级部门的id的字段，例如：pid_all
     *
     * @return string
     */
    String deptPidAll() default "";

    /**
     * 如果是一对多的数据权限，这里需要指定需要过滤的表用于关联权限的关联表名，例如：sys_user_dept ，用来关联用户有多个部门
     * <br> type 为 {@linkplain DataScopeType#MULTI MULTI} 时必填
     *
     * @return string
     */
    String multiTable() default "";

    /**
     * 一对多的数据权限的关联表用于关联需要过滤表的字段，例如 sys_user_dept 表里面的 user_id
     * <br> type 为 {@linkplain DataScopeType#MULTI MULTI} 时必填
     *
     * @return string
     */
    String multiFiled() default "";

    /**
     * 一对多的数据权限的关联表用于关联部门 表的字段，例如 sys_user_dept 表里面的 dept_id
     * <br> type 为 {@linkplain DataScopeType#MULTI MULTI} 时必填
     *
     * @return string
     */
    String multiDeptId() default "";

    /**
     * 需要过滤权限的表的别名，查询的时候需要关联查询，所以这里要指定一个表的别名，例如：要查询用户表就可以用 su 表示
     *
     * @return string
     */
    String alias();

    /**
     * <pre>
     * 需要过滤权限的表的关联字段，分两种情况：
     * 1. 单权限（一个用户只有一个部门）的时候就是用户表的 dept_id
     * 2. 多权限（一个用户有多个部门）的时候就是用户和部门关联表的时候用户表用于做关联的字段，例如用户表的 id
     * </pre>
     *
     * @return string
     */
    String field();

    /**
     * 如果是自定义 mapper.xml 写 sql ，这里要加入过滤数据权限条件的 sql 的字段，可以把这个 sql 加入到自定义的 sql where 条件里面，但是，注意，一般是使用一个
     * <pre>{@code <if test="_parameter.containsKey('_data_scope_') and _data_scope_ != null">
     * and exists(${_data_scope_})
     * </if> }
     * </pre>
     * 关联起来
     *
     * @return string
     */
    String sqlField() default "_data_scope_";

    /**
     * 按用户，还是按角色过滤，或者取合集过滤，这里默认按用户过滤
     *
     * @return DataScopeFilterType
     * @see DataScopeFilterType
     */
    DataScopeFilterType filterType() default DataScopeFilterType.USER;

    /**
     * 数据权限类型
     *
     * @return DataScopeType
     * @see DataScopeType
     */
    DataScopeType type() default DataScopeType.SINGLE;

    /**
     * 部门表 是否是逻辑删除的
     *
     * @return boolean
     */
    boolean logicDelete() default false;

    /**
     * 逻辑删除字段
     *
     * @return string
     */
    String logicField() default "";

    /**
     * 逻辑删除的未删除状态
     *
     * @return string
     */
    String notDeletedStatus() default "";

    /**
     * 数据库类型，目前仅支持 {@linkplain DbType#POSTGRE_SQL POSTGRE_SQL} | {@linkplain DbType#MYSQL MYSQL} | {@linkplain DbType#ORACLE ORACLE}
     *
     * @return DbType
     */
    DbType dbtype() default DbType.POSTGRE_SQL;

    /**
     * 是否包含查询权限子集
     *
     * @return boolean
     */
    boolean includeChildren() default true;

    /**
     * 是否包含查询权限父级，即，把父级也一起返回
     *
     * @return boolean
     */
    boolean includeParents() default false;

    /**
     * 判断是否要进行数据权限过滤
     *
     * @return boolean
     */
    Class<? extends DataScopeCondition> dataScopeCondition() default DefaultDataScopeCondition.class;

    /**
     * 自定义过滤规则
     *
     * @return string
     */
    Class<? extends DataScopeCustom> custom() default DefaultDataScopeCustom.class;

    /**
     * 如果查询不到数据权限，或者不存在数据权限关联的时候的处理方式，一般只出现在一对多关联的情况下，比如，可能存在如下的数据关联条件：
     * <br>
     * 这分数据，即可以是按部门来分配权限，也可以单独指定到某个人，那这样，就有可能部门字段是空的，因为他只单独指定到了用户，可以不指定部门
     * <br>
     * 默认不允许，也就是必须要存在数据权限的配置才给通过
     *
     * @return {@linkplain DataScopeGetNotDealType DataScopeGetNotDealType}
     */
    DataScopeGetNotDealType notExistDealType() default DataScopeGetNotDealType.FORBID;

    /**
     * 是否自动把过滤条件添加到 where 语句后面，默认添加到第一个 where 后面
     *
     * @return boolean
     */
    boolean auto() default true;

    /**
     * 是否扩展
     *
     * @return boolean
     */
    boolean extensible() default false;

    /**
     * 扩展数据范围表
     *
     * @return string
     */
    String extensionScopeTable() default "";

    /**
     * 扩展数据范围表的主键（用来关联是哪个部门需要扩展范围）
     *
     * @return string
     */
    String extensionScopeField() default "";

    /**
     * 扩展数据范围表的关联扩展的部门的 id
     *
     * @return string
     */
    String extensionScopeDeptId() default "";

    /**
     * 是否按状态过滤
     *
     * @return boolean
     */
    boolean statusFilter() default false;

    /**
     * 状态字段
     *
     * @return string
     */
    String statusField() default "";

    /**
     * 组织机构启用状态，默认启动状态是 1
     *
     * @return string
     */
    String enableStatus() default "1";
}
