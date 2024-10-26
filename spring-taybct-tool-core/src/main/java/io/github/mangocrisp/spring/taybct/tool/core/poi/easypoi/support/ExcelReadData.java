package io.github.mangocrisp.spring.taybct.tool.core.poi.easypoi.support;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * <pre>
 * excel 读取结果
 * </pre>
 *
 * @author XiJieYin
 * @since 2024/10/9 17:42
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "excel 操作参数")
public class ExcelReadData implements Serializable {

    
    private static final long serialVersionUID = -3337514421493141938L;

    /**
     * 拿到的一行的单元格
     */
    @Schema(description = "拿到的一行的单元格")
    private JSONObject infoRowCells = new JSONObject();
    /**
     * 数据行
     */
    @Schema(description = "数据行")
    private JSONArray dataRows = new JSONArray();

}
