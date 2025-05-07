package io.github.mangocrisp.spring.taybct.tool.core.util.tree;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSONObject;
import io.github.mangocrisp.spring.taybct.tool.core.util.CollectionSortUtil;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
         * {@link CollectionSortUtil#sortListByName(List, String)}<br>
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
         * {@link CollectionSortUtil#sortListByName(List, String, Boolean)}<br>
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
        List<T> getChildren();

        /**
         * 设置子级
         */
        void setChildren(List<T> children);

    }

    @Deprecated(since = "3.2.2", forRemoval = true)
    public static <T extends Tree<T>> List<T> genTree(List<T> list) {
        return genTree(list, "0", false);
    }

    @Deprecated(since = "3.2.2", forRemoval = true)
    public static <T extends Tree<T>> List<T> genTree(List<T> list, Serializable parentId) {
        return genTree(list, parentId, false);
    }

    @Deprecated(since = "3.2.2", forRemoval = true)
    public static <T extends Tree<T>> List<T> genTree(List<T> list, boolean includeTopParent) {
        return genTree(list, "0", includeTopParent);
    }

    /**
     * 生成树
     *
     * @param list             集合
     * @param parentId         从哪个父级开始往下生子子集
     * @param includeTopParent 是否包含顶级父级，如果不包含，会有多个根目录
     * @return {@code List<T>}
     * @author xijieyin <br> 2022/8/5 19:12
     * @since 1.0.0
     */
    @Deprecated(since = "3.2.2", forRemoval = true)
    public static <T extends Tree<T>> List<T> genTree(List<T> list, Serializable parentId, boolean includeTopParent) {
        List<T> result = new ArrayList<>();
        // 先获取到第一个，如果一个都没有就直接返回空了
        Optional<T> first = list.stream().findFirst();
        if (first.isEmpty()) {
            return result;
        }
        if (parentId == null) parentId = first.get().defaultParentId();
        // 先找到所有的顶级节点
        Serializable finalParentLevel = parentId;
        // 先排序
        list = CollectionSortUtil.sortListByName(list, first.get().getSortField(), first.get().sortAsc());
        // 然后在所有的集合里面找父级
        list.forEach(t -> {
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
        List<T> finalSet = list;
        result.forEach(p -> Iteration(p, finalSet));
        return result;
    }

    @Deprecated(since = "3.2.2", forRemoval = true)
    private static <T extends Tree<T>> void Iteration(T p, List<T> set) {
        for (T c : set) {
            if (Objects.equals(p.getId(), c.getParentId())) {
                Iteration(c, set);
                if (p.getChildren() == null) {
                    p.setChildren(new ArrayList<>());
                }
                p.getChildren().add(c);
            }
        }
        // 这里可以做排序
        p.setChildren(CollectionSortUtil.sortListByName(p.getChildren(), p.getSortField(), p.sortAsc()));
    }

    /**
     * 生成树结构, 如果对顺序没有要求，不排序可以增加生成树结构的速度（不排序）
     *
     * @param list           数据
     * @param excludeNodeIds 需要排除的节点的 id
     * @return 树结构
     */
    public static <T extends Tree<T>> List<T> tree(List<T> list, Serializable... excludeNodeIds) {
        return tree(list, null, new HashSet<>(Arrays.asList(excludeNodeIds)));
    }

    /**
     * 生成树结构, 如果对顺序没有要求，不排序可以增加生成树结构的速度
     *
     * @param list           数据
     * @param sort           排序规则
     * @param excludeNodeIds 需要排除的节点的 id
     * @return 树结构
     */
    public static <T extends Tree<T>> List<T> tree(List<T> list, Comparator<? super T> sort, Serializable... excludeNodeIds) {
        return tree(list, sort, new HashSet<>(Arrays.asList(excludeNodeIds)));
    }

    /**
     * 生成树结构, 如果对顺序没有要求，不排序可以增加生成树结构的速度(不排序)
     *
     * @param list             数据
     * @param excludeNodeIdSet 需要排除的节点的 id
     * @return 树结构
     */
    public static <T extends Tree<T>> List<T> tree(List<T> list, Set<Serializable> excludeNodeIdSet) {
        return tree(list, null, excludeNodeIdSet);
    }

    /**
     * 生成树结构, 如果对顺序没有要求，不排序可以增加生成树结构的速度
     *
     * @param list             数据
     * @param sort             排序规则
     * @param excludeNodeIdSet 需要排除的节点的 id
     * @return 树结构
     */
    public static <T extends Tree<T>> List<T> tree(List<T> list, Comparator<? super T> sort, Set<Serializable> excludeNodeIdSet) {
        if (CollectionUtil.isEmpty(list)) {
            return new ArrayList<>();
        }
        // 先给所有的数据找到自己的位置
        Map<Serializable, Integer> indexMap = new ConcurrentHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            if (excludeNodeIdSet.contains(list.get(i).getId())) {
                continue;
            }
            indexMap.put(list.get(i).getId(), i);
        }
        list.forEach(i -> {
            if (excludeNodeIdSet.contains(i.getId())) {
                return;
            }
            if (i.getParentId() != null) {
                // 如果有父级就往父级里面添加
                Integer index = indexMap.get(i.getParentId());
                if (index != null) {
                    T j = list.get(index);
                    if (j.getChildren() == null) {
                        j.setChildren(new ArrayList<>());
                    }
                    j.getChildren().add(i);
                }
            }
        });
        List<T> tree = new ArrayList<>();
        list.forEach(i -> {
            if (excludeNodeIdSet.contains(i.getId())) {
                return;
            }
            if (sort != null && CollectionUtil.isNotEmpty(i.getChildren())) {
                i.getChildren().sort(sort);
            }
            if (i.getParentId() == null || indexMap.get(i.getParentId()) == null) {
                // 如果找父级不到了就往 tree 里面放在第一级
                tree.add(i);
            }
        });
        // 顶级也需要排序
        if (sort != null && CollectionUtil.isNotEmpty(tree)) {
            tree.sort(sort);
        }
        return tree;
    }

}
