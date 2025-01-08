package io.github.mangocrisp.spring.taybct.tool.core.poi.easypoi.support;

import cn.afterturn.easypoi.entity.BaseTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * <pre>
 * Excel 导出模板的字段
 * </pre>
 *
 * @author XiJieYin
 * @since 2024/10/9 19:03
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Excel 导出模板的字段")
public class ExportTemplateField implements Serializable {

    @Serial
    private static final long serialVersionUID = 1593493556010638811L;

    /**
     * 字段，如果是MAP导出,这个是map的key
     */
    @Schema(description = "字段，如果是MAP导出,这个是map的key")
    protected String key;
    /**
     * 字段描述
     */
    @Schema(description = "字段描述")
    protected String name;
    /**
     * 对应type
     */
    @Schema(description = "对应type")
    private int type = BaseTypeConstants.STRING_TYPE;
    /**
     * 字段值
     */
    @Schema(description = "字段值")
    private Object value;
    /**
     * 字段排序
     */
    @Schema(description = "字段排序")
    private int orderNum = 0;
    /**
     * 列宽
     */
    @Schema(description = "列宽")
    private double width = 25d;
    /**
     * 标题分组
     */
    @Schema(description = "标题分组")
    private String groupName;
    /**
     * 纵向合并相同单元格
     */
    @Schema(description = "纵向合并相同单元格")
    private boolean mergeSame = true;
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
     * 是否支持换行
     */
    @Schema(description = "是否支持换行")
    private boolean wrap = true;
    /**
     * 后缀
     */
    @Schema(description = "后缀")
    private String suffix;
    /**
     * 是否统计
     */
    @Schema(description = "是否统计")
    private boolean statistics;
    /**
     * 数字格式
     */
    @Schema(description = "数字格式")
    private String numFormat;
    /**
     * 是否隐藏列
     */
    @Schema(description = "是否隐藏列")
    private boolean columnHidden;
    /**
     * 枚举导出属性字段
     */
    @Schema(description = "枚举导出属性字段")
    private String enumExportField;
    /**
     * 脱敏规则
     */
    @Schema(description = "脱敏规则")
    private String desensitizationRule;

    /**
     * 数据库格式
     */
    @Schema(description = "数据库格式")
    private String databaseFormat;
    /**
     * 导出日期格式
     */
    @Schema(description = "导出日期格式")
    private String format;
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
    /**
     * set/get方法
     */
    @Schema(description = "set/get方法")
    private Method method;
    /**
     * 这个是不是超链接,如果是需要实现接口返回对象
     */
    @Schema(description = "这个是不是超链接,如果是需要实现接口返回对象")
    private boolean hyperlink;
    /**
     * 固定的列
     */
    @Schema(description = "固定的列")
    private Integer fixedIndex;
    /**
     * 时区
     */
    @Schema(description = "时区")
    private String timezone;
    /**
     * 是否插入下拉
     */
    @Schema(description = "是否插入下拉")
    private boolean addressList;
}
