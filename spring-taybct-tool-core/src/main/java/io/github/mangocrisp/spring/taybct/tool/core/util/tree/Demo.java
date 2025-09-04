package io.github.mangocrisp.spring.taybct.tool.core.util.tree;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.util.RandomUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.CollectionSortUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author XiJieYin
 * @since 2025/4/23 09:16
 */
public class Demo {
    public void main(String[] args) {
        List<BaseTree> list = new ArrayList<>();
        List<TreeNode<Integer>> list1 = new ArrayList<>();
//        list.add(new BaseTree(1, "0", 0, null, "label", null, BaseTree.class, new ArrayList<>()));
        for (int i = 1; i < 999999; i++) {
            int sort = RandomUtil.randomInt(1, 100);
            list.add(new BaseTree(1, i + "", sort, (i - 1) / 2 + "", "label", null, BaseTree.class, new ArrayList<>()));
            list1.add(new TreeNode<>(i, (i - 1) / 2, i + "", sort));
        }
//        list.add(new BaseTree(1, "1", 0, "0", "label", null, BaseTree.class, new ArrayList<>()));
//        list.add(new BaseTree(1, "3", 2, "1", "label", null, BaseTree.class, new ArrayList<>()));
//        list.add(new BaseTree(1, "2", 1, "1", "label", null, BaseTree.class, new ArrayList<>()));
//        list.add(new BaseTree(1, "5", 4, "2", "label", null, BaseTree.class, new ArrayList<>()));
//        list.add(new BaseTree(1, "4", 3, "2", "label", null, BaseTree.class, new ArrayList<>()));
//        list.add(new BaseTree(1, "7", 6, "3", "label", null, BaseTree.class, new ArrayList<>()));
//        list.add(new BaseTree(1, "6", 5, "3", "label", null, BaseTree.class, new ArrayList<>()));
//        list.add(new BaseTree(1, "9", 8, "4", "label", null, BaseTree.class, new ArrayList<>()));
//        list.add(new BaseTree(1, "8", 7, "4", "label", null, BaseTree.class, new ArrayList<>()));
//        list.add(new BaseTree(1, "11", 10, "5", "label", null, BaseTree.class, new ArrayList<>()));
//        list.add(new BaseTree(1, "10", 9, "5", "label", null, BaseTree.class, new ArrayList<>()));
//        list.add(new BaseTree(1, "12", 11, "6", "label", null, BaseTree.class, new ArrayList<>()));
        long l = System.currentTimeMillis();
        // 不排序
//        List<BaseTree> tree = TreeUtil.tree(list);
        // 硬比较，也就是比较数字大小
//        List<BaseTree> tree = TreeUtil.tree(list, Comparator.comparingInt(t-> Convert.toInt(t.getSort())));
        // 可以比较中文，按拼音比较
        List<BaseTree> tree = TreeUtil.tree(list, CollectionSortUtil.comparingAny(BaseTree::getLabel, true));
        System.out.println("TreeUtil.tree：" + (System.currentTimeMillis() - l));

//        l = System.currentTimeMillis();
//        List<BaseTree> baseTrees = TreeUtil.genTree(list, true);
//        System.out.println("TreeUtil.genTree：" + (System.currentTimeMillis() - l));

        l = System.currentTimeMillis();
        List<Tree<Integer>> build = cn.hutool.core.lang.tree.TreeUtil.build(list1);
        System.out.println("cn.hutool.core.lang.tree.TreeUtil.build：" + (System.currentTimeMillis() - l));
        System.out.println();
    }
}
