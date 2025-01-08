package io.github.mangocrisp.spring.taybct.tool.core.es.dto.element;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * 相当于 in 查询
 *
 * @author xijieyin <br> 2023/1/10 21:24
 */
@Data
@Accessors(chain = true)
@Schema(description = "相当于 in 查询")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Terms implements Serializable {
    @Serial
    private static final long serialVersionUID = -7204697394413206892L;
    /**
     * 字段
     */
    @NotNull
    @Schema(description = "字段")
    String field;
    /**
     * 值，集合
     */
    @NotNull
    @Schema(description = "值，集合")
    @Singular("addValues")
    Set<String> values;

}
