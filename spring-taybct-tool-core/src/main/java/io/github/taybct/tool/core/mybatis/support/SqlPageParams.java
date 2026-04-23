package io.github.taybct.tool.core.mybatis.support;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.github.taybct.tool.core.util.BeanUtil;
import io.github.taybct.tool.core.util.ObjectUtil;
import io.github.taybct.tool.core.util.StringUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
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
@Slf4j
@Accessors(chain = true)
public class SqlPageParams implements ISqlPageParams, Serializable {
    @Serial
    private static final long serialVersionUID = -5669238366260294233L;
    /**
     * 分页页码
     */
    @Schema(description = "分页页码", example = "0")
    @JsonSerialize(using = ToStringSerializer.class)
    @Builder.Default
    private Long pageNum = 0L;
    /**
     * 分页大小
     */
    @Schema(description = "分页大小", example = "10")
    @JsonSerialize(using = ToStringSerializer.class)
    @Builder.Default
    private Long pageSize = 10L;
    /**
     * 是否查询总数
     */
    @Schema(description = "是否查询总数", example = "true")
    @Builder.Default
    private Boolean countTotal = true;
    /**
     * 按哪些字段排序 逗号隔开字段，空格隔开 asc（正序） 或者 desc（倒序）
     */
    @Schema(description = "按哪些字段排序 逗号隔开字段，空格隔开 asc（正序） 或者 desc（倒序）", example = "id asc,updateTime desc")
    private String pageOrder;

    /**
     * 按哪些字段排序 asc（正序） 或者 desc（倒序），这个是个集合
     */
    @Schema(description = "按哪些字段排序 asc（正序） 或者 desc（倒序），这个是个集合", example = "sort=id asc&sort=updateTime desc")
    private LinkedHashSet<String> sort;

    /**
     * 被允许排序的字段类，也就是返回结果，一般 MyBatis 返回的结果实体类字段会和数据库字段一致，所以这里可以指定返回结果实体类，如果不完全一直，可以使用 allowedSort 做补充
     *
     * @see SqlPageParams#allowedSort
     */
    private Class<?> allowedSortClass;

    /**
     * 被允许排序的字段，防止 sql 注入，如果不想一个一个写，也可以直接指定返回结果的类
     *
     * @see SqlPageParams#allowedSort
     */
    private final LinkedHashSet<String> allowedSortFields = new LinkedHashSet<>();

    public SqlPageParams(Long pageNum, Long pageSize, String pageOrder) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pageOrder = pageOrder;
    }

    private void setAllowedSortFields(LinkedHashSet<String> allowedSortFields) {
        // 什么事也不做
    }

    private void setAllowedSortClass(Class<?> allowedSortClass) {
        // 什么事也不做
    }

    /**
     * 被允许排序的字段类，也就是返回结果，一般 MyBatis 返回的结果实体类字段会和数据库字段一致，所以这里可以指定返回结果实体类，如果不完全一致，注意，类型最好是对应数据库映射类，也就是说有被 MyBatis 字段注解的映射字段
     *
     * @see TableField
     * @see TableId
     * @see SqlPageParams#allowedSort
     */
    public SqlPageParams allowedSort(Class<?> clazz) {
        this.allowedSortClass = clazz;
        return this;
    }

    /**
     * 按哪些字段排序 asc（正序） 或者 desc（倒序），这个是个集合
     */
    public SqlPageParams allowedSort(LinkedHashSet<String> allowedSortFields) {
        this.allowedSortFields.addAll(allowedSortFields);
        return this;
    }

    public LinkedHashSet<String> getAllowedSortFields() {
        Class<?> sortClass = getAllowedSortClass();
        if (sortClass != null) {
            allowedSortFields.addAll(getAllFields(sortClass));
        }
        return allowedSortFields;
    }

    List<String> getAllFields(Class<?> type) {
        return BeanUtil.getAllFields(type, true).stream()
                .map(field -> {
                    if (field.isAnnotationPresent(TableField.class)) {
                        TableField annotation = field.getAnnotation(TableField.class);
                        if (StringUtil.isNotEmpty(annotation.value()) && annotation.exist()) {
                            return annotation.value();
                        }
                    }
                    if (field.isAnnotationPresent(TableId.class)) {
                        TableId annotation = field.getAnnotation(TableId.class);
                        if (StringUtil.isNotEmpty(annotation.value())) {
                            return annotation.value();
                        }
                    }
                    return field.getName();
                })
                .toList();
    }

    /**
     * 清空允许排序字段
     */
    public SqlPageParams clearAllowedSortFields() {
        allowedSortFields.clear();
        return this;
    }

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

    /**
     * 安全的排序（防止 sql 注入）
     *
     * @param orderStr 排序字符串
     * @return 去除注入字符之后的字符串
     */
    @Deprecated(since = "3.5.3")
    public String safePageOrder(String orderStr) {
        // do nothing
        return null;
    }

    /**
     * 转换安全的排序字符串
     *
     * @param stream 流
     * @return 安全字符串
     */
    public LinkedHashSet<String> safeOrderBySql(Stream<String> stream) {
        LinkedHashSet<String> sortFields = getAllowedSortFields();
        if (CollectionUtil.isNotEmpty(sortFields)) {
            // 都转成下划线去比较
            sortFields.forEach(StringUtil::humpToUnderline);
            return stream
                    .filter(StringUtil::isNotBlank)
                    .map(StringUtil::humpToUnderline)
                    .filter(fieldGroup -> {
                        String[] field = fieldGroup.split(ORDER_SEPARATE);
                        if (field.length > 1) {
                            if (!PAGE_SORT_ASC.equalsIgnoreCase(field[1]) && !PAGE_SORT_DESC.equalsIgnoreCase(field[1])) {
                                return false;
                            }
                        }
                        if (field.length > 2){
                            // 一般是 field + " " + asc/desc
                            return false;
                        }
                        return sortFields.contains(field[0]);
                    })
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        // 把 ;,(,) 替换掉，防止 sql 注入
        // 把结果丢到一个 Set 里面去去重
        return stream
                .peek(s -> {
                    if (s.contains(";") || s.contains("(") || s.contains(")") || s.contains("=")) {
                        throw new RuntimeException("分页参数错误");
                    }
                })
                .map(s -> s.replaceAll(";", "")
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
    @Override
    public String getPageOrder() {
        if (StringUtil.isNotBlank(this.pageOrder)) {
            this.sort = safeOrderBySql(Arrays.stream(this.pageOrder.split(FIELD_SEPARATE)));
        }
        if (CollectionUtil.isNotEmpty(this.sort)) {
            this.pageOrder = CollectionUtil.join(safeOrderBySql(this.sort.stream()), FIELD_SEPARATE);
        }
        return this.pageOrder;
    }

    public LinkedHashSet<String> getSort() {
        if (CollectionUtil.isNotEmpty(this.sort)) {
            this.pageOrder = CollectionUtil.join(safeOrderBySql(this.sort.stream()), FIELD_SEPARATE);
        }
        if (StringUtil.isNotBlank(this.pageOrder)) {
            this.sort = safeOrderBySql(Arrays.stream(this.pageOrder.split(FIELD_SEPARATE)));
        }
        return this.sort;
    }

    /**
     * 创建一个空的 SqlPageParams
     *
     * @return SqlPageParams
     */
    public static SqlPageParams of() {
        return new SqlPageParams();
    }

    /**
     * <pre>
     *     兼容原 Map&lt;String, Object&gt; 参数转换成 SqlPageParams
     * </pre>
     *
     * @param params 参数
     * @return SqlPageParams
     */
    public static SqlPageParams of(Map<String, Object> params) {
        SqlPageParams sqlPageParams = new SqlPageParams();
        if (params == null) {
            return sqlPageParams;
        }
        long pageNum = Convert.toLong(params.get(PAGE_NUM), 0L);
        long pageSize = Convert.toLong(params.get(PAGE_SIZE), 10L);
        boolean countTotal = Convert.toBool(params.get(COUNT_TOTAL), true);
        String pageOrder = Convert.toStr(params.get(ORDER_BY_COLUMN), null);
        return sqlPageParams.setPageNum(pageNum)
                .setPageSize(pageSize)
                .setCountTotal(countTotal)
                .setPageOrder(pageOrder);
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
