package io.github.mangocrisp.spring.taybct.tool.core.bean.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.mangocrisp.spring.taybct.tool.core.bean.BaseEntity;
import io.github.mangocrisp.spring.taybct.tool.core.bean.ISecurityUtil;
import io.github.mangocrisp.spring.taybct.tool.core.constant.ISysParamsObtainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 基础接口类，继承自 MyBatis-plus 的 {@link ServiceImpl}，主要是为了在做新增，修改等操作的时候
 * 加入登录用户的信息，这样知道是谁在什么时候操作了什么。<br>
 * <p>
 * 然后第二个就是，分页的操作等
 *
 * @author xijieyin <br> 2022/8/5 13:55
 * @see ServiceImpl
 * @since 1.0.0
 */
@Slf4j
@SuppressWarnings("unchecked")
public abstract class BaseServiceImpl<M extends BaseMapper<T>, T extends BaseEntity>
        extends ServiceImpl<M, T>
        implements IBaseService<T> {

    @Autowired
    protected ISecurityUtil securityUtil;
    @Autowired
    private ISysParamsObtainService sysParamsObtainService;

    public ISecurityUtil getSecurityUtil() {
        return securityUtil == null ? () -> null : securityUtil;
    }

    /**
     * 保存新的对象，为了确保是新增，这里 id 得先清空
     *
     * @param entity 实体
     * @return 是否操作成功
     */
    public boolean saveNew(T entity) {
        entity.setId(null);
        return save(entity);
    }

    /**
     * 批量新增，为了确保是新增，这里 id 得先清空
     *
     * @param entityList 实体类集合
     * @return 是否操作成功
     */
    public boolean saveNewBatch(Collection<T> entityList) {
        entityList.forEach(entity -> entity.setId(null));
        return saveBatch(entityList);
    }

    /**
     * 这里是去获取 {@code <M>} 的类型，要继承一下是因为 MyBatis 里面也有个方法，他获取到的 {@code <M>}
     * 不是我们要的
     *
     * @return {@code Class<M>}
     * @author xijieyin <br> 2022/8/5 14:21
     * @since 1.0.0
     */
    @Override
    public Class<M> getMapperClass() {
        return (Class<M>) ReflectionKit.getSuperClassGenericType(this.getClass(), BaseServiceImpl.class, 0);
    }

    /**
     * 这里是去获取 {@code <T>} 的类型
     *
     * @return {@code Class<T>}
     * @author xijieyin <br> 2022/8/5 14:25
     * @since 1.0.0
     */
    @Override
    public  Class<T> getEntityClass() {
        return (Class<T>) ReflectionKit.getSuperClassGenericType(this.getClass(), BaseServiceImpl.class, 1);
    }

    /**
     * 检查有没有权限，返回权限列表
     *
     * @return {@code Set<String>}
     * @author xijieyin <br> 2022/8/5 14:36
     * @since 1.0.0
     */
    public Set<String> checkAuthorities() {
        return securityUtil.getLoginUser().checkAuthorities();
    }

    /**
     * 检查有没有 ROOT 权限
     *
     * @return int
     * @author xijieyin <br> 2022/8/5 14:36
     * @since 1.0.0
     */
    public int checkRoot() {
        return securityUtil.getLoginUser().checkRoot();
    }

    /**
     * 权限过滤，只查询出用户拥有的角色的权限
     *
     * @param params 请求参数
     * @author xijieyin <br> 2022/9/21 13:49
     * @since 1.0.4
     */
    public void authoritiesFilter(Map<String, Object> params) {
        Set<String> authorities = checkAuthorities();
        int isRoot = checkRoot();
        params.put("isRoot", isRoot);
        if (isRoot == 0) {
            params.put("authorities", authorities);
        }
    }
}
