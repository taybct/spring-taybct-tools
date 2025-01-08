package io.github.mangocrisp.spring.taybct.tool.core.mybatisUtil;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 基本包装类
 *
 * @author jopson
 * @since 2020/10/23 10:49 上午
 */
public abstract class BaseEntityWrapper<E, V> {

    public BaseEntityWrapper() {
    }

    public abstract V entityVO(E entity);

    public List<V> listVO(List<E> list) {
        return (List) list.stream().map(this::entityVO).collect(Collectors.toList());
    }

    public IPage<V> pageVO(IPage<E> pages) {
        List<V> records = this.listVO(pages.getRecords());
        IPage<V> pageVo = new Page(pages.getCurrent(), pages.getSize(), pages.getTotal());
        pageVo.setRecords(records);
        return pageVo;
    }
}
