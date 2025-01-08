package io.github.mangocrisp.spring.taybct.tool.core.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * SQL 查询参数
 *
 * @author xijieyin <br> 2022/8/5 18:43
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SQL 查询参数")
public class SqlQueryParams implements Serializable {
    @Serial
    private static final long serialVersionUID = 810998982004714052L;
    /**
     * 分页页码
     */
    @Schema(description = "分页页码", example = "0")
    private Long pageNum = 0L;
    /**
     * 分页大小
     */
    @Schema(description = "分页大小", example = "10")
    private Long pageSize = 10L;
    /**
     * 按哪些字段排序 逗号隔开字段，空格隔开 asc（正序） 或者 desc（倒序）
     */
    @Schema(description = "按哪些字段排序 逗号隔开字段，空格隔开 asc（正序） 或者 desc（倒序）", example = "id asc,updated desc")
    private String pageOrder;
    /**
     * 数据库查询位置
     */
    @Schema(description = "数据库查询位置", hidden = true)
    private Long offset;

    public SqlQueryParams(Long pageNum, Long pageSize, String pageOrder) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pageOrder = pageOrder;
    }

    public Long getOffset() {
        long l;
        return (l = (this.getPageNum() - 1L)) < 0L ? 0L : l * this.getPageSize();
    }

    private void setOffset(Long offset) {
    }

}