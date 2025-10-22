package io.github.taybct.tool.core.es.dto.element;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * 查询逻辑条件 and | ro | not
 *
 * @author XiJieYin <br> 2023/1/31 15:28
 */
@Data
@Accessors(chain = true)
@Schema(description = "相关于 sql 里面的 and | ro | not 查询")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AndOrNot implements Serializable {

    @Serial
    private static final long serialVersionUID = 7847373101999885810L;

    /**
     * 布尔查询
     */
    @Schema(description = "or 查询")
    @Singular("addBool")
    private Set<Bool> bool;
    /**
     * = 查询
     */
    @Schema(description = "= 查询")
    @Singular("addTerm")
    private Set<Term> term;
    /**
     * in 查询
     */
    @Schema(description = "in 查询")
    @Singular("addTerms")
    private Set<Terms> terms;
    /**
     * 区间查询
     */
    @Schema(description = "区间查询")
    @Singular("addRange")
    private Set<Range> range;
    /**
     * like 模糊查询
     */
    @Schema(description = "like 模糊查询")
    @Singular("addWildcard")
    private Set<Wildcard> wildcard;
    /**
     * is not null 查询
     */
    @Schema(description = "is not null 查询")
    @Singular("addExists")
    private Set<Exists> exists;
}
