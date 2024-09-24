package io.github.mangocrisp.spring.taybct.tool.core.es.dto.element;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 相当于 = 查询
 *
 * @author xijieyin <br> 2023/1/10 21:21
 */
@Data
@Schema(description = "相当于 = 查询")
@AllArgsConstructor
@NoArgsConstructor
public class Term implements Serializable {
    private static final long serialVersionUID = -1685289668121073954L;
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
    private Object value;

    public Term setField(String field) {
        this.field = field;
        return this;
    }

    public Term setValue(Object value) {
        this.value = value;
        return this;
    }
}
