package io.github.mangocrisp.spring.taybct.tool.core.constant;

/**
 * 分页查询请求请求常量
 *
 * @author xijieyin <br> 2022/8/5 18:18
 * @since 1.0.0
 */
public interface PageRequestConstants {
    /**
     * 分页页码
     */
    String PAGE_NUM = "pageNum";
    /**
     * 每页大小
     */
    String PAGE_SIZE = "pageSize";
    /**
     * 按哪些字段排序 逗号隔开字段，空格隔开 asc（正序） 或者 desc（倒序） 例如: <br>
     * name asc,time desc,score,
     */
    String ORDER_BY_COLUMN = "pageOrder";
    /**
     * 字段与字段之间的分隔符
     */
    String FIELD_SEPARATE = ",";
    /**
     * 排序与字段之间的分隔符
     */
    String ORDER_SEPARATE = " ";
    /**
     * 排序asc
     */
    String PAGE_SORT_ASC = "asc";
    /**
     * 排序desc
     */
    String PAGE_SORT_DESC = "desc";
    /**
     * mybatis查询别名
     */
    String WRAPPER = "ew";
    /**
     * 默认页码
     */
    int DEFAULT_PAGE = 1;
    /**
     * 默认显示条数
     */
    int DEFAULT_LIMIT = 10;
}
