package io.github.mangocrisp.spring.taybct.tool.core.mybatisUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.mangocrisp.spring.taybct.tool.core.constant.PageRequestConstants;
import org.springframework.beans.BeanUtils;

import java.util.Map;

/**
 * mybatisPlus分页扩展包装类
 *
 * @author jopson
 * @since 2020/9/21 11:20 上午
 */
public class Condition<T> {

    public static <T> IPage<T> getPage(Map<String, Object> entityMap) {

        int pageNumber = Integer.parseInt(entityMap.getOrDefault(PageRequestConstants.PAGE_NUM, PageRequestConstants.DEFAULT_PAGE).toString());
        int pageSize = Integer.parseInt(entityMap.getOrDefault(PageRequestConstants.PAGE_SIZE, PageRequestConstants.DEFAULT_LIMIT).toString());
        Page<T> page = new Page(pageNumber, pageSize);
        return page;
    }

    public static <T> QueryWrapper<T> getQueryWrapper(T entity) {
        return new QueryWrapper(entity);
    }

    public static <T> QueryWrapper<T> getQueryWrapper(Map<String, Object> query, Class<T> clazz) {
        // 移除多余的分页字段，避免查询报错
        query.remove(PageRequestConstants.PAGE_NUM);
        query.remove(PageRequestConstants.PAGE_SIZE);
        QueryWrapper<T> qw = new QueryWrapper();
        // 排序字段
        if (StringUtils.isNotBlank((String) query.get(PageRequestConstants.PAGE_SORT_ASC))) {
            qw.orderByAsc((String) query.get(PageRequestConstants.PAGE_SORT_ASC));
        }
        if (StringUtils.isNotBlank((String) query.get(PageRequestConstants.PAGE_SORT_DESC))) {
            qw.orderByDesc((String) query.get(PageRequestConstants.PAGE_SORT_DESC));
        }
        // 移除排序字段，避免查询报错
        query.remove(PageRequestConstants.PAGE_SORT_ASC);
        query.remove(PageRequestConstants.PAGE_SORT_DESC);
        qw.setEntity(BeanUtils.instantiateClass(clazz));
        SqlKeyword.buildCondition(query, qw);
        return qw;
    }
}
