package io.github.mangocrisp.spring.taybct.tool.core.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.github.mangocrisp.spring.taybct.tool.core.annotation.TableFieldDefault;
import io.github.mangocrisp.spring.taybct.tool.core.constant.DateConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 历史表
 *
 * @author XiJieYin <br> 2023/1/29 15:14
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "历史记录")
public class HistoryEntity implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 2135595980986647658L;

    /**
     * 主键id
     */
    @Schema(description = "主键")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 创建人
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "创建人")
    private String createUser;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = DateConstants.format.YYYY_MM_DD_HH_mm_ss)
    @JsonFormat(pattern = DateConstants.format.YYYY_MM_DD_HH_mm_ss)
    @TableFieldDefault(isTimeNow = true)
    private LocalDateTime createTime;
    /**
     * 表名
     */
    @Schema(description = "表名")
    private String tableName;
    /**
     * 主键
     */
    @Schema(description = "主键")
    private String primaryKey;
    /**
     * 历史记录（json）
     */
    @Schema(description = "历史记录（json）")
    private String jsonData;
    /**
     * 操作类型（修改2，删除3）
     */
    @Schema(description = "操作类型（修改2，删除3）")
    private Integer operateType;
    /**
     * 主键
     */
    @Schema(description = "主键")
    private Serializable primaryValue;

}