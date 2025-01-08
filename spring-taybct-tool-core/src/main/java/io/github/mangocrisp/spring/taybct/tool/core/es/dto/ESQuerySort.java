package io.github.mangocrisp.spring.taybct.tool.core.es.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * ES 查询排序数据传输对象
 *
 * @author xijieyin <br> 2022/8/5 22:22
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "ES 查询排序数据传输对象")
public class ESQuerySort implements Serializable {

    @Serial
    private static final long serialVersionUID = 1928377835030540004L;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段", example = "create_time")
    private String field;
    /**
     * 顺序，倒序 asc,desc
     */
    @Schema(description = "排序顺序", example = "asc")
    private String order;

}
