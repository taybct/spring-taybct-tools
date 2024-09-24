package io.github.mangocrisp.spring.taybct.tool.core.es.dto.element;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 区间查询 between，但是也不会是，里面有很多条件，或以随意组合
 * 大于，大于等于，小于，小于等于
 *
 * @author xijieyin <br> 2023/1/10 22:25
 */
@Data
@Schema(description = "区间查询 between，但是也不会是，里面有很多条件，或以随意组合 大于，大于等于，小于，小于等于")
@AllArgsConstructor
@NoArgsConstructor
public class Range implements Serializable {
    private static final long serialVersionUID = -7746651911397393035L;
    /**
     * 字段
     */
    @NotNull
    @Schema(description = "字段")
    private String field;
    /**
     * 小于
     */
    @Schema(description = "小于")
    private Object lt;
    /**
     * 小于等于
     */
    @Schema(description = "小于等于")
    private Object lte;
    /**
     * 大于
     */
    @Schema(description = "大于")
    private Object gt;
    /**
     * 大于等于
     */
    @Schema(description = "大于等于")
    private Object gte;
    /**
     * 时区 +08:00，这个主要是用于比较时间的时候用
     */
    @Schema(description = "时区 +08:00，这个主要是用于比较时间的时候用")
    private String timeZone;

    public Range setField(String field) {
        this.field = field;
        return this;
    }

    public Range setLt(Object lt) {
        this.lt = lt;
        return this;
    }

    public Range setLte(Object lte) {
        this.lte = lte;
        return this;
    }

    public Range setGt(Object gt) {
        this.gt = gt;
        return this;
    }

    public Range setGte(Object gte) {
        this.gte = gte;
        return this;
    }

    public Range setTimeZone(String timeZone) {
        this.timeZone = timeZone;
        return this;
    }
}
