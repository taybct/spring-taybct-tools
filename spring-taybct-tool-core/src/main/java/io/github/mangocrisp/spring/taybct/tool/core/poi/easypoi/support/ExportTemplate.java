package io.github.mangocrisp.spring.taybct.tool.core.poi.easypoi.support;

import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import com.alibaba.fastjson2.JSONObject;
import io.github.mangocrisp.spring.taybct.tool.core.mybatis.support.SqlPageParams;
import io.github.mangocrisp.spring.taybct.tool.core.poi.easypoi.constants.EasyPOIConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * Excel 导出模板
 * </pre>
 *
 * @author XiJieYin
 * @since 2024/10/9 19:03
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Excel 导出模板")
public class ExportTemplate<P extends Serializable> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1593493556010638811L;

    /**
     * 表名
     */
    @Schema(description = "表名")
    private String fileName;
    /**
     * sheet 名
     */
    @Schema(description = "sheet 名")
    private String sheetName = EasyPOIConstant.DEFAULT_SHEET;
    /**
     * excel 类型
     */
    @Schema(description = "excel 类型")
    private ExcelType excelType = ExcelType.XSSF;
    /**
     * 行高
     */
    @Schema(description = "行高")
    private double height = 20;
    /**
     * 纵向合并相同单元格开始行
     */
    @Schema(description = "纵向合并相同单元格开始行", defaultValue = "2")
    private Integer mergeSameStartRow = 2;
    /**
     * 模板字段数组
     */
    @Schema(description = "模板字段数组")
    private List<ExportTemplateField> exportTemplateField = new ArrayList<>();
    /**
     * 查询条件
     */
    @Schema(description = "查询条件")
    private P params;

    /**
     * 获取查询条件 JSON 对象
     */
    @Schema(hidden = true)
    public JSONObject getParamsJSONObject() {
        return JSONObject.from(this.params);
    }

    /**
     * 分页参数
     */
    @Schema(description = "分页参数")
    private SqlPageParams sqlPageParams;

}
