package io.github.mangocrisp.spring.taybct.tool.core.util.tree;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSONObject;
import io.github.mangocrisp.spring.taybct.tool.core.util.CollectionSortUtil;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 树工具，依赖于集合排序工具
 *
 * @author xijieyin <br> 2022/8/5 19:10
 * @since 1.0.0
 */
public class TreeUtil {

    public interface Tree<T> extends Serializable {

        /**
         * 用来排序的字段，排序使用的是<br>
         * {@link CollectionSortUtil#sortListByName(LinkedHashSet, String)}<br>
         * 如果你要排序，这里默认是使用的 sort，但是建议你是自己再准备一个专门用来排序的字段
         *
         * @return String
         * @author xijieyin <br> 2022/8/5 19:15
         * @since 1.0.0
         */
        default String getSortField() {
            return "sort";
        }

        /**
         * 是否是正序排序,结合排序来使用，这个配置其实也就是<br>
         * {@link CollectionSortUtil#sortListByName(LinkedHashSet, String, Boolean)}<br>
         * 里面的排序字段，本来默认也是正序，如果想倒序排序，就返回一个 false
         *
         * @return Boolean
         * @author xijieyin <br> 2022/8/9 10:31
         * @since 1.0.0
         */
        default Boolean sortAsc() {
            return true;
        }

        /**
         * 默认顶级父级
         */
        default Serializable defaultParentId() {
            return 0;
        }

        /**
         * 获取节点 id
         */
        Serializable getId();

        /**
         * 获取父节点 id
         */
        Serializable getParentId();

        /**
         * 级别
         */
        default Integer getLevel() {
            return null;
        }

        /**
         * 显示标签
         */
        default String getLabel() {
            return null;
        }

        /**
         * 父 id
         */
        default Serializable getPid() {
            return getParentId();
        }

        /**
         * 详细信息
         */
        default JSONObject getDetail() {
            return null;
        }

        /**
         * 获取子级
         */
        LinkedHashSet<T> getChildren();

        /**
         * 设置子级
         */
        void setChildren(LinkedHashSet<T> children);

    }


    public static <T extends Tree<T>> LinkedHashSet<T> genTree(LinkedHashSet<T> set) {
        return genTree(set, "0", false);
    }

    public static <T extends Tree<T>> LinkedHashSet<T> genTree(LinkedHashSet<T> set, Serializable parentId) {
        return genTree(set, parentId, false);
    }

    public static <T extends Tree<T>> LinkedHashSet<T> genTree(LinkedHashSet<T> set, boolean includeTopParent) {
        return genTree(set, "0", includeTopParent);
    }

    /**
     * 生成树
     *
     * @param set              集合
     * @param parentId         从哪个父级开始往下生子子集
     * @param includeTopParent 是否包含顶级父级，如果不包含，会有多个根目录
     * @return {@code LinkedHashSet<T>}
     * @author xijieyin <br> 2022/8/5 19:12
     * @since 1.0.0
     */
    public static <T extends Tree<T>> LinkedHashSet<T> genTree(LinkedHashSet<T> set, Serializable parentId, boolean includeTopParent) {
        LinkedHashSet<T> result = new LinkedHashSet<>();
        // 先获取到第一个，如果一个都没有就直接返回空了
        Optional<T> first = set.stream().findFirst();
        if (first.isEmpty()) {
            return result;
        }
        if (parentId == null) parentId = first.get().defaultParentId();
        // 先找到所有的顶级节点
        Serializable finalParentLevel = parentId;
        // 先排序
        set = CollectionSortUtil.sortListByName(set, first.get().getSortField(), first.get().sortAsc());
        // 然后在所有的集合里面找父级
        set.forEach(t -> {
            if (includeTopParent) {
                if (Objects.equals(t.getId(), finalParentLevel)) {
                    result.add(t);
                }
            } else {
                if (Objects.equals(t.getParentId(), finalParentLevel)) {
                    result.add(t);
                }
            }
        });
        LinkedHashSet<T> finalSet = set;
        result.forEach(p -> Iteration(p, finalSet));
        return result;
    }

    private static <T extends Tree<T>> void Iteration(T p, LinkedHashSet<T> set) {
        for (T c : set) {
            if (Objects.equals(p.getId(), c.getParentId())) {
                Iteration(c, set);
                if (p.getChildren() == null) {
                    p.setChildren(new LinkedHashSet<>());
                }
                p.getChildren().add(c);
            }
        }
        // 这里可以做排序
        p.setChildren(CollectionSortUtil.sortListByName(p.getChildren(), p.getSortField(), p.sortAsc()));
    }


    /**
     * 排除根节点
     *
     * @param set         集合
     * @param topParentId 根节点的id
     * @return 树结构
     */
    public static <T extends Tree<T>> LinkedHashSet<T> excludeRootNode(LinkedHashSet<T> set, Long topParentId) {
        return excludeRootNode(set, topParentId, true);
    }

    /**
     * 排除根节点
     *
     * @param set         集合
     * @param topParentId 根节点的id
     * @param sort        排序
     * @return 树结构
     */
    public static <T extends Tree<T>> LinkedHashSet<T> excludeRootNode(LinkedHashSet<T> set, Long topParentId, boolean sort) {
        return tree(new LinkedHashSet<>(set.stream().filter(i -> !i.getId().equals(topParentId)).toList()), sort);
    }

    /**
     * 生成树结构
     *
     * @param set 数据
     * @return 树结构
     */
    public static <T extends Tree<T>> LinkedHashSet<T> tree(LinkedHashSet<T> set) {
        return tree(set, true);
    }

    /**
     * 生成树结构, 如果对顺序没有要求，不排序可以增加生成树结构的速度
     *
     * @param set  数据
     * @param sort 排序
     * @return 树结构
     */
    public static <T extends Tree<T>> LinkedHashSet<T> tree(LinkedHashSet<T> set, boolean sort) {
        if (CollectionUtil.isEmpty(set)) {
            return new LinkedHashSet<>();
        }
        // 拿到所有的 id
        Set<Serializable> idSet = set.stream().map(Tree::getId).collect(Collectors.toSet());
        LinkedHashSet<T> tree = new LinkedHashSet<>();
        set.forEach(i -> {
            if (idSet.contains(i.getParentId())) {
                set.forEach(j -> {
                    if (j.getId().equals(i.getParentId())) {
                        if (j.getChildren() == null) {
                            j.setChildren(new LinkedHashSet<>());
                        }
                        j.getChildren().add(i);
                    }
                });
            }
        });
        set.forEach(i -> {
            if (!idSet.contains(i.getParentId())) {
                if (sort && CollectionUtil.isNotEmpty(i.getChildren())) {
                    i.setChildren(sortChildren(i, i.getChildren()));
                }
                // 如果找父级不到了就往 tree 里面放在第一级
                tree.add(i);
            }
        });
        return tree;
    }

    /**
     * 给子集排序
     *
     * @param t   父级
     * @param set 子集
     * @return 排序后的子集
     */
    public static <T extends Tree<T>> LinkedHashSet<T> sortChildren(T t, LinkedHashSet<T> set) {
        set.forEach(i -> {
            if (CollectionUtil.isNotEmpty(i.getChildren())) {
                i.setChildren(sortChildren(i, i.getChildren()));
            }
        });
        // 这里可以做排序
        return CollectionSortUtil.sortListByName(set, t.getSortField(), t.sortAsc());
    }

}
