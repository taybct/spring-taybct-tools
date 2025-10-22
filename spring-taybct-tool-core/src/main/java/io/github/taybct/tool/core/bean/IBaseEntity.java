package io.github.taybct.tool.core.bean;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 基础类接口，这里提取一下抽象，因为不能确定业务会使用什么类型做主键，这里让开发人员可以自己定义主键应该是什么类型的
 *
 * @author XiJieYin
 * @since 2.1.7
 */
public interface IBaseEntity<P extends Serializable> extends Serializable {

    /**
     * 获取主键id
     *
     * @return 主键 id
     */
    P getId();

    /**
     * 设置主键id
     *
     * @param id 主键 id
     */
    void setId(P id);

    /**
     * 获取创建人
     *
     * @return 创建人
     */
    P getCreateUser();

    /**
     * 设置创建人
     *
     * @param createUser 创建人
     */
    default void setCreateUser(P createUser) {

    }

    /**
     * 设置创建人
     *
     * @param createUser 创建人
     */
    default void setCreateUser(Object createUser) {

    }

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    LocalDateTime getCreateTime();

    /**
     * 设置创建时间
     *
     * @param createTime 创建时间
     */
    void setCreateTime(LocalDateTime createTime);

    /**
     * 获取更新人
     *
     * @return 更新人
     */
    P getUpdateUser();

    /**
     * 设置更新人
     *
     * @param updateUser 更新人
     */
    default void setUpdateUser(P updateUser) {

    }

    /**
     * 设置更新人
     *
     * @param updateUser 更新人
     */
    default void setUpdateUser(Object updateUser) {

    }

    /**
     * 获取更新时间
     *
     * @return 更新时间
     */
    LocalDateTime getUpdateTime();

    /**
     * 设置更新时间
     *
     * @param updateTime 更新时间
     */
    void setUpdateTime(LocalDateTime updateTime);

    /**
     * 获取扩展
     *
     * @return 扩展
     */
    Map<String, Object> getExpansion();

    /**
     * 设置扩展
     *
     * @param expansion 扩展
     */
    void setExpansion(Map<String, Object> expansion);

}
