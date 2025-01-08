package io.github.mangocrisp.spring.taybct.tool.core.es.dto;

import io.github.mangocrisp.spring.taybct.tool.core.es.dto.element.Bool;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashSet;

/**
 * ES 查询数据传输对象
 *
 * @author xijieyin <br> 2022/8/5 22:22
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "ES 查询数据传输对象")
public class ESQueryDTO<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = -4309672008590713770L;
    /**
     * 查询对象
     */
    @Schema(description = "需要查询的对象")
    private T object;
    /**
     * 第几页
     */
    @Schema(description = "第几页，从 0 开始", example = "0")
    private Integer number = 0;
    /**
     * 页面大小
     */
    @Schema(description = "页面大小", example = "10")
    private Integer size = 10;
    /**
     * 排序字段
     */
    @Schema(description = "排序字段")
    private LinkedHashSet<ESQuerySort> fieldSort;
    /**
     * 这里可以自定义条件查询，可以自己去 kibana 去试，想要达到的最终效果
     */
    private Bool bool;

}
