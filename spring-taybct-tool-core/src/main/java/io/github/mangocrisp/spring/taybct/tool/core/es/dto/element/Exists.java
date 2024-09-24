package io.github.mangocrisp.spring.taybct.tool.core.es.dto.element;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 相当于 is not null
 *
 * @author xijieyin <br> 2023/1/10 21:21
 */
@Data
@Schema(description = "相当于 is not null 查询")
@AllArgsConstructor
@NoArgsConstructor
public class Exists implements Serializable {

    private static final long serialVersionUID = -6737761959689170245L;
    /**
     * 字段
     */
    @NotNull
    @Schema(description = "字段")
    private String field;

    public Exists setField(String field) {
        this.field = field;
        return this;
    }

}
