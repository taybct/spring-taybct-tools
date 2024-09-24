package io.github.mangocrisp.spring.taybct.tool.core.es.dto.element;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Set;

/**
 * 相当于 in 查询
 *
 * @author xijieyin <br> 2023/1/10 21:24
 */
@Data
@Schema(description = "相当于 in 查询")
@AllArgsConstructor
@NoArgsConstructor
public class Terms implements Serializable {
    private static final long serialVersionUID = 2311644868334621326L;
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
    Set<?> values;

    public Terms setField(String field) {
        this.field = field;
        return this;
    }

    public Terms setValues(Set<?> values) {
        this.values = values;
        return this;
    }
}
