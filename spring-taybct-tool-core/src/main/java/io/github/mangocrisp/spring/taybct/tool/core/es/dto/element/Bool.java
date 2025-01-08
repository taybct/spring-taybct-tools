package io.github.mangocrisp.spring.taybct.tool.core.es.dto.element;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.function.Supplier;

/**
 * 相关于 sql 里面的 () ，里面的所有的条件在一起为 true
 *
 * @author xijieyin <br> 2023/1/10 21:15
 */
@Data
@Accessors(chain = true)
@Schema(description = "相关于 sql 里面的 () ，里面的所有的条件在一起为 true")
@AllArgsConstructor
@NoArgsConstructor
public class Bool implements Serializable {

    @Serial
    private static final long serialVersionUID = 5105588961726458942L;
    /**
     * and 查询
     */
    @Schema(description = "and 查询")
    private AndOrNot must;
    /**
     * or 查询
     */
    @Schema(description = "or 查询")
    private AndOrNot should;
    /**
     * and not 查询 这里是 and not 查询，里面包含所有的组成 not 的条件，例如
     * {@code name != 'xxx' and age != 18}
     */
    @Schema(description = "and not 查询 这里是 and not 查询，里面包含所有的组成 not 的条件，例如 name != 'xxx' and age != 18")
    private AndOrNot mustNot;

    public Bool must(Supplier<AndOrNot> mustSupplier) {
        if (this.must != null) {
            throw new RuntimeException("一个 bool 里面只能有一个 must");
        }
        this.must = mustSupplier.get();
        return this;
    }

    public Bool should(Supplier<AndOrNot> shouldSupplier) {
        if (this.should != null) {
            throw new RuntimeException("一个 bool 里面只能有一个 should");
        }
        this.should = shouldSupplier.get();
        return this;
    }

    public Bool mustNot(Supplier<AndOrNot> mustNotSupplier) {
        if (this.mustNot != null) {
            throw new RuntimeException("一个 bool 里面只能有一个 must_not");
        }
        this.mustNot = mustNotSupplier.get();
        return this;
    }
}
