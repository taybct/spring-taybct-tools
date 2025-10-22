package io.github.taybct.tool.core.mybatis.support;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.github.taybct.tool.core.util.ObjectUtil;
import io.github.taybct.tool.core.util.StringUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.taybct.tool.core.constant.PageRequestConstants.*;

/**
 * SQL 分页参数
 *
 * @author xijieyin
 * @since 3.1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SQL 分页参数")
@Builder
public class SqlPageParams implements Serializable {
    @Serial
    private static final long serialVersionUID = -5669238366260294233L;
    /**
     * 分页页码
     */
    @Schema(description = "分页页码", example = "0")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pageNum = 0L;
    /**
     * 分页大小
     */
    @Schema(description = "分页大小", example = "10")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pageSize = 10L;
    /**
     * 是否查询总数
     */
    @Schema(description = "是否查询总数", example = "true")
    private Boolean countTotal = true;
    /**
     * 按哪些字段排序 逗号隔开字段，空格隔开 asc（正序） 或者 desc（倒序）
     */
    @Schema(description = "按哪些字段排序 逗号隔开字段，空格隔开 asc（正序） 或者 desc（倒序）", example = "id asc,updateTime desc")
    private String pageOrder;

    @Schema(description = "按哪些字段排序 asc（正序） 或者 desc（倒序），这个是个集合", example = "sort=id asc&sort=updateTime desc")
    private LinkedHashSet<String> sort;

    /**
     * 数据库查询位置
     */
    @Schema(description = "数据库查询位置", hidden = true)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long offset;

    public Long getOffset() {
        if (ObjectUtil.isNotEmpty(this.offset) && this.offset >= 0) {
            return this.offset;
        }
        return (this.offset = (this.getPageNum() - 1L) < 0L ? 0L : (this.getPageNum() - 1L) * this.getPageSize());
    }

    public void setPageOrder(String pageOrder) {
        this.pageOrder = safePageOrder(pageOrder);
    }

    /**
     * 安全的排序（防止 sql 注入）
     *
     * @param orderStr 排序字符串
     * @return 去除注入字符之后的字符串
     */
    public String safePageOrder(String orderStr) {
        if (StringUtil.isBlank(orderStr)) {
            return null;
        }
        // 按逗号隔开
        return CollectionUtil.join(safeOrderBySql(Arrays.stream(orderStr.split(FIELD_SEPARATE))), FIELD_SEPARATE);
    }

    /**
     * 转换安全的排序字符串
     *
     * @param stream 流
     * @return 安全字符串
     */
    public LinkedHashSet<String> safeOrderBySql(Stream<String> stream) {
        // 把 ;,(,) 替换掉，防止 sql 注入
        // 把结果丢到一个 Set 里面去去重
        return stream.map(s -> s.replaceAll(";", "")
                        .replaceAll("\\(", "")
                        .replaceAll("\\)", ""))
                .map(StringUtil::humpToUnderline)
                .map(String::trim).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 获取排序字符串
     *
     * @return 排序字符串
     */
    public String getPageOrder() {
        if (StringUtil.isNotBlank(this.pageOrder)) {
            return this.pageOrder;
        }
        if (CollectionUtil.isNotEmpty(this.sort)) {
            this.pageOrder = CollectionUtil.join(safeOrderBySql(this.sort.stream()), FIELD_SEPARATE);
        }
        return this.pageOrder;
    }

    public void setSort(LinkedHashSet<String> sort) {
        if (CollectionUtil.isNotEmpty(sort)) {
            this.sort = safeOrderBySql(sort.stream());
        }
    }

    public LinkedHashSet<String> getSort() {
        if (CollectionUtil.isNotEmpty(this.sort)) {
            return this.sort;
        }
        if (StringUtil.isNotBlank(this.pageOrder)) {
            this.sort = safeOrderBySql(Arrays.stream(this.pageOrder.split(FIELD_SEPARATE)));
        }
        return this.sort;
    }

    /**
     * @param dto 实体类
     * @return {@code Wrapper<T>}
     * @author xijieyin
     * @since 3.1.0
     */
    public <T> Wrapper<T> genQueryWrapper(T dto) {
        // 排序字段逗号隔开了
        QueryWrapper<T> wrapper = new QueryWrapper<>(dto);
        // 排序
        Optional.ofNullable(getSort())
                // 然后按 " " 分开
                .ifPresent(sortArray -> sortArray.forEach(s -> {
                    // 空格隔开字段
                    String[] fieldOrder = s.split(ORDER_SEPARATE);
                    String field = fieldOrder[0];
                    if (fieldOrder.length > 1) {
                        String order = fieldOrder[1];
                        if (order.equalsIgnoreCase(PAGE_SORT_ASC)) {
                            wrapper.orderByAsc(field);
                        } else {
                            wrapper.orderByDesc(field);
                        }
                    } else {
                        // 默认正序
                        wrapper.orderByAsc(field);
                    }
                }));
        return wrapper;
    }

    /**
     * 根据传入的参数构建分页
     *
     * @return {@code Page<T>}
     * @author xijieyin
     * @since 3.1.0
     */
    public <T> Page<T> genPage() {
        Page<T> page = new Page<>();
        // 当前页码
        page.setCurrent(Optional.ofNullable(getPageNum()).orElse(page.getCurrent()));
        // 页面大小
        page.setSize(Optional.ofNullable(getPageSize()).orElse(page.getSize()));
        // 排序
        Optional.ofNullable(getSort())
                // 然后按 " " 分开
                .ifPresent(sortArray -> sortArray.forEach(s -> {
                    String[] sa = s.split(ORDER_SEPARATE);
                    if (sa.length > 1) {
                        page.orders().add(sa[1].equalsIgnoreCase(PAGE_SORT_ASC) ? OrderItem.asc(sa[0]) : OrderItem.desc(sa[0]));
                    } else {
                        page.orders().add(OrderItem.asc(sa[0]));
                    }
                }));
        return page;
    }
}
