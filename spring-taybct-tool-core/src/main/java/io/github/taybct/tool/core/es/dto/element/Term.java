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
 * 相当于 = 查询
 *
 * @author xijieyin <br> 2023/1/10 21:21
 */
@Data
@Accessors(chain = true)
@Schema(description = "相当于 = 查询")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Term implements Serializable {
    @Serial
    private static final long serialVersionUID = -1714492441948612773L;
    /**
     * 字段
     */
    @NotNull
    @Schema(description = "字段")
    private String field;
    /**
     * 值，这里的值，可以直接用字符串也没问题，转好就行了
     */
    @NotNull
    @Schema(description = "值，这里的值，可以直接用字符串也没问题，转好就行了\n")
    private String value;
}
