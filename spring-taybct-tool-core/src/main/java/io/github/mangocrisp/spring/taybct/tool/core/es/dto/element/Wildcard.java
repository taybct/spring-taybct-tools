package io.github.mangocrisp.spring.taybct.tool.core.es.dto.element;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 相当于 like 模糊查询
 *
 * @author xijieyin <br> 2023/1/10 21:29
 */
@Data
@Schema(description = "相当于 like 模糊查询")
@AllArgsConstructor
@NoArgsConstructor
public class Wildcard implements Serializable {

    private static final long serialVersionUID = -7348424334223611703L;
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

    public Wildcard setField(String field) {
        this.field = field;
        return this;
    }

    public Wildcard setValue(String value) {
        this.value = value;
        return this;
    }
}
