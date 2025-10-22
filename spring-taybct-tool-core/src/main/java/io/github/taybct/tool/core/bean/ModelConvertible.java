package io.github.taybct.tool.core.bean;

import io.github.taybct.tool.core.util.BeanUtil;
import io.swagger.v3.oas.annotations.Hidden;

/**
 * 可转换的数据模型，对于一些前端往后端接口传数据的时候，为了不想直接把实体类的字段全都给出来，让前端传进来影响脏了数据，
 * 可以使用数据模型来接收数据然后转换成想要的实体类{@code <T>}
 * <br>
 * 如果要临时存储转换之后的对象，可以在实现类里面添加一个属性字段 {@code private T convertedBean;}
 * <br>
 * 如果不希望被 swagger 等文档检查到 可以加上 {@code @Hidden} 注解
 *
 * @author XiJieYin <br> 2024/3/20 9:31
 */
public interface ModelConvertible<T> {

    /**
     * 转换之后的对象，这个方便一般用于返回转换之后的对象，如果是操作前端传过来的对象，请先调用 bean() 方法，防止被攻击篡改数据
     *
     * @return 对象
     */
    @Hidden
    default T getConvertedBean() {
        return null;
    }

    /**
     * 设置存储转换之后的对象，方便可能比如在接口内对对象做了操作，数据变了之类的，这里可以把转换之后对对象临时存在内存里面
     *
     * @param bean 对象
     */
    default void setConvertedBean(T bean) {
    }

    /**
     * 获取到 bean 的类型
     *
     * @return bean 的类型
     */
    default Class<T> beanClass() {
        return BeanUtil.getInterfaceT(this, ModelConvertible.class, 0);
    }

    /**
     * 转换成数据库需要操作的对象
     *
     * @param ignoreProperties 忽略不转换不需要的字段
     * @return 转换成功的实体对象
     */
    default T bean(String... ignoreProperties) {
        T bean;
        if ((bean = getConvertedBean()) != null) {
            return bean;
        }
        setConvertedBean(bean = BeanUtil.copyProperties(this, beanClass(), ignoreProperties));
        return bean;
    }

}
