package io.github.mangocrisp.spring.taybct.tool.core.es.dto.element;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 查询逻辑条件 and | ro | not
 *
 * @author XiJieYin <br> 2023/1/31 15:28
 */
@Data
@Schema(description = "相关于 sql 里面的 and | ro | not 查询")
public class AndOrNot implements Serializable {

    private static final long serialVersionUID = -1557719420785275466L;
    /**
     * 布尔查询
     */
    @Schema(description = "or 查询")
    private Set<Bool> bool;
    /**
     * = 查询
     */
    @Schema(description = "= 查询")
    private Set<Term> term;
    /**
     * in 查询
     */
    @Schema(description = "in 查询")
    private Set<Terms> terms;
    /**
     * 区间查询
     */
    @Schema(description = "区间查询")
    private Set<Range> range;
    /**
     * like 模糊查询
     */
    @Schema(description = "like 模糊查询")
    private Set<Wildcard> wildcard;
    /**
     * is not null 查询
     */
    @Schema(description = "is not null 查询")
    private Set<Exists> exists;

    /**
     * 添加 bool 条件
     *
     * @param bool 条件
     * @return 当前逻辑
     */
    public AndOrNot addBool(Bool bool) {
        if (this.bool == null) {
            this.bool = new LinkedHashSet<>();
        }
        this.bool.add(bool);
        return this;
    }

    /**
     * 添加 term 条件
     *
     * @param term 条件
     * @return 当前逻辑
     */
    public AndOrNot addTerm(Term term) {
        if (this.term == null) {
            this.term = new LinkedHashSet<>();
        }
        this.term.add(term);
        return this;
    }

    /**
     * 添加 terms 条件
     *
     * @param terms 条件
     * @return 当前逻辑
     */
    public AndOrNot addTerms(Terms terms) {
        if (this.terms == null) {
            this.terms = new LinkedHashSet<>();
        }
        this.terms.add(terms);
        return this;
    }

    /**
     * 添加 range 条件
     *
     * @param range 条件
     * @return 当前逻辑
     */
    public AndOrNot addRange(Range range) {
        if (this.range == null) {
            this.range = new LinkedHashSet<>();
        }
        this.range.add(range);
        return this;
    }

    /**
     * 添加 wildcard 条件
     *
     * @param wildcard 条件
     * @return 当前逻辑
     */
    public AndOrNot addWildcard(Wildcard wildcard) {
        if (this.wildcard == null) {
            this.wildcard = new LinkedHashSet<>();
        }
        this.wildcard.add(wildcard);
        return this;
    }

    /**
     * 添加 is not null 条件
     *
     * @param exists 条件
     * @return 当前查询
     */
    public AndOrNot addExists(Exists exists) {
        if (this.exists == null) {
            this.exists = new LinkedHashSet<>();
        }
        this.exists.add(exists);
        return this;
    }
}
