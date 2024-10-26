package io.github.mangocrisp.spring.taybct.tool.core.poi.easypoi.support;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * <pre>
 * 参数
 * </pre>
 *
 * @author XiJieYin
 * @since 2024/10/9 17:34
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "excel 操作参数")
public class ExcelParamsDTO implements Serializable {

    
    private static final long serialVersionUID = 5229459259657346485L;

    /**
     * 表名
     */
    @Schema(description = "表名", defaultValue = "sheet1")
    private String sheetName = "sheet1";
    /**
     * 传入的信息行
     */
    @Schema(description = "传入的信息行")
    private String infoRows;
    /**
     * 是否读取多行数据
     */
    @Schema(description = "是否读取多行数据", defaultValue = "true")
    private Boolean multiRows = true;
    /**
     * 是否合并表头数据
     */
    @Schema(description = "是否合并表头数据", defaultValue = "false")
    private Boolean mergeInfoRows = false;
    /**
     * 标题行
     */
    @Schema(description = "标题行", defaultValue = "0")
    private Integer titleRows = 0;
    /**
     * 头部行
     */
    @Schema(description = "头部行", defaultValue = "1")
    private Integer headerRows = 1;
    /**
     * 模型类型
     */
    @Schema(description = "模型类型")
    private String modelType;
}
