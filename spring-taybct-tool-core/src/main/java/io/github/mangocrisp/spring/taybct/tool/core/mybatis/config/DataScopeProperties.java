package io.github.mangocrisp.spring.taybct.tool.core.mybatis.config;

import io.github.mangocrisp.spring.taybct.tool.core.constant.PropertiesPrefixConstants;
import io.github.mangocrisp.spring.taybct.tool.core.enums.DataScopeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

/**
 * 数据权限属性
 *
 * @author XiJieYin <br> 2023/6/21 17:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@ConfigurationProperties(prefix = PropertiesPrefixConstants.DATA_SCOPE)
public class DataScopeProperties implements Serializable {

    private static final long serialVersionUID = -3309426652790255013L;

    /**
     * 用户权限表，例如，数据权限体系是使用部门，那第，这个表就是用户和部门的关联表，例如：sys_user_dept
     */
    private String userScopeTable;

    /**
     * 权限字段，按什么过滤权限，也就是 用户权限表里面的某个字段来关联权限，例如，sys_user_dept 表里面的 user_id，是按用户来关联过滤数据权限
     */
    private String userScopeField;

    /**
     * 用户权限表用于关联部门 表的字段名，例如：sys_user_dept 表里面的 dept_id
     */
    private String userScopeDeptId;

    /**
     * 角色权限表，按角色过滤权限的权限关联表，例如：sys_role_dept
     */
    private String roleScopeTable;

    /**
     * 角色权限表权限字段，按什么过滤权限，也就是 角色权限表里面的某个字段来关联权限，例如，sys_role_dept 表里面的 role_id，是按用户来关联过滤数据权限
     */
    private String roleScopeField;

    /**
     * 角色权限表用于关联部门 表的字段名，例如：sys_role_dept 表里面的 dept_id
     */
    private String roleScopeDeptId;

    /**
     * 角色表，因为一个用户可能是有多个角色的，所以，需要按角色 code 的集合来过滤，就得查询角色表，例如：sys_role
     */
    private String roleTable;

    /**
     * 角色表的主键，用来关联，例如：id
     */
    private String roleField;

    /**
     * 角色 code，例如：code
     */
    private String roleCode;

    /**
     * 部门表 指定部门信息表，这个主要是为了获取上下级关系，例如：sys_dept
     */
    private String deptTable;

    /**
     * 部门 表用来关联权限表的主键字段，例如：id
     */
    private String deptId;

    /**
     * 部门 表的用于存储所有父级部门的id的字段，例如：pid_all
     */
    private String deptPidAll;

    /**
     * 如果是一对多的数据权限，这里需要指定需要过滤的表用于关联权限的关联表名，例如：sys_user_dept ，用来关联用户有多个部门
     * <br> type 为 {@linkplain DataScopeType#MULTI MULTI} 时必填
     */
    private String multiTable;

    /**
     * 一对多的数据权限的关联表用于关联需要过滤表的字段，例如 sys_user_dept 表里面的 user_id
     * <br> type 为 {@linkplain DataScopeType#MULTI MULTI} 时必填
     */
    private String multiFiled;

    /**
     * 一对多的数据权限的关联表用于关联部门 表的字段，例如 sys_user_dept 表里面的 dept_id
     * <br> type 为 {@linkplain DataScopeType#MULTI MULTI} 时必填
     */
    private String multiDeptId;

    /**
     * 部门表 是否是逻辑删除的
     */
    private boolean logicDelete;

    /**
     * 逻辑删除字段
     */
    private String logicField;

    /**
     * 逻辑删除的未删除状态
     */
    private String notDeletedStatus;

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
     * 状态字段
     */
    private String statusField;

    /**
     * 组织机构启用状态，默认启动状态是 1
     */
    private String enableStatus;
}
