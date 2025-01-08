package io.github.mangocrisp.spring.taybct.tool.core.poi.easypoi.util;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * <pre>
 * 需要导出的数据库字段
 * </pre>
 *
 * @author XiJieYin
 * @since 2024/10/11 17:45
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "需要导出的数据库字段")
@Builder
public class DBField implements Serializable {

    @Serial
    private static final long serialVersionUID = 5815399030994726402L;

    /**
     * 字段，如果是MAP导出,这个是map的key
     */
    @Schema(description = "字段，如果是MAP导出,这个是map的key")
    private String key;
    /**
     * 字段描述
     */
    @Schema(description = "字段描述")
    private String name;
    /**
     * 字段默认值
     */
    @Schema(description = "字段默认值")
    private Object defaultValue;
    /**
     * 是属于哪个表（标题分组）
     */
    @Schema(description = "是属于哪个表（标题分组）")
    private String groupName;
    /**
     * 字段排序
     */
    @Schema(description = "字段排序")
    private String orderNum = "0";
    /**
     * 格式
     */
    @Schema(description = "格式")
    private String format;
    /**
     * 列宽
     */
    @Schema(description = "列宽")
    private double width = 25d;
    /**
     * 是否需要合并
     */
    @Schema(description = "是否需要合并")
    private boolean needMerge = true;
    /**
     * 单元格纵向合并
     */
    @Schema(description = "单元格纵向合并")
    private boolean mergeVertical = true;
    /**
     * 替换规则，a_1,b_2
     */
    @Schema(description = "替换规则，a_1,b_2")
    private String[] replace;
    /**
     * 字典名称
     */
    @Schema(description = "字典名称")
    private String dict;

}
