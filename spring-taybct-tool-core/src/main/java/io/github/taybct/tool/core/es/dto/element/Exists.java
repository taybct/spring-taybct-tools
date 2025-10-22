package io.github.taybct.tool.core.es.dto.element;

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
 * 相当于 is not null
 *
 * @author xijieyin <br> 2023/1/10 21:21
 */
@Data
@Accessors(chain = true)
@Schema(description = "相当于 is not null 查询")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Exists implements Serializable {

    @Serial
    private static final long serialVersionUID = 4524808125861291059L;
    /**
     * 字段
     */
    @NotNull
    @Schema(description = "字段")
    private String field;

}
