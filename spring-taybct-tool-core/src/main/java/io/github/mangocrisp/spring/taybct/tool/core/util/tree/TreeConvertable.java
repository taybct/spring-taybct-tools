package io.github.mangocrisp.spring.taybct.tool.core.util.tree;

/**
 * 可以转换成转型的
 *
 * @author xijieyin <br> 2022/8/5 19:09
 * @since 1.0.0
 */
public interface TreeConvertable<T> {

    /**
     * 转换成树
     *
     * @param bean 转入对象
     */
    default BaseTree convertToTree(T bean) {
        return new BaseTree();
    }

}
