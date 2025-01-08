package io.github.mangocrisp.spring.taybct.tool.core.es.dto.element;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 相当于 like 模糊查询
 *
 * @author xijieyin <br> 2023/1/10 21:29
 */
@Data
@Accessors(chain = true)
@Schema(description = "相当于 like 模糊查询")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Wildcard implements Serializable {

    @Serial
    private static final long serialVersionUID = -3823006481236934705L;

    /**
     * 字段
     */
    @NotNull
    @Schema(description = "字段")
    private String field;
    /**
     * 值，模糊查询，* 相当于 %
     */
    @NotNull
    @Schema(description = "值，模糊查询，* 相当于 %")
    private String value;
}
