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
 * 区间查询 between，但是也不会是，里面有很多条件，或以随意组合
 * 大于，大于等于，小于，小于等于
 *
 * @author xijieyin <br> 2023/1/10 22:25
 */
@Data
@Accessors(chain = true)
@Schema(description = "区间查询 between，但是也不会是，里面有很多条件，或以随意组合 大于，大于等于，小于，小于等于")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Range implements Serializable {
    @Serial
    private static final long serialVersionUID = 2178240557938058951L;

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
}
