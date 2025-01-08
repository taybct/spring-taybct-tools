package io.github.mangocrisp.spring.taybct.tool.core.mybatis.constant;

import com.baomidou.mybatisplus.core.toolkit.StringPool;

import java.io.Serializable;

/**
 * <pre>
 * 常量
 * </pre>
 *
 * @author XiJieYin
 * @since 2024/10/20 14:58
 */
public interface Constants extends StringPool, Serializable {
    /**
     * asc
     */
    String ASC = "asc";
    /**
     * desc
     */
    String DESC = "desc";
    /**
     * mybatis 可选项
     */
    String MYBATIS_OPTIONAL = "mo";
    /**
     * 条件设置的位置 —— 基础（主表查询条件）
     */
    String CONDITION_PLACE_BASIC = "basic";

}
