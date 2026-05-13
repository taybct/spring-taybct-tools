package io.github.taybct.tool.scheduledlogcentralized.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.github.taybct.tool.core.constant.DateConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 定时任务控制台日志管理
 * </p>
 *
 * @author xieaixi
 * @since 2025-12-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="定时任务控制台日志管理")
@TableName("scheduled_log_centralized")
public class ScheduledLogCentralized implements Serializable {

    private static final long serialVersionUID = -2710074899213437355L;
    
    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 生成日志时间
     */
    @Schema(description = "生成日志时间")
    @DateTimeFormat(pattern = DateConstants.format.YYYY_MM_DD_HH_mm_ss)
    @JsonFormat(pattern = DateConstants.format.YYYY_MM_DD_HH_mm_ss)
    private LocalDateTime successTime;

    /**
     * 本次运行id
     */
    @Schema(description = "本次运行id")
    private String runId;

    /**
     * 模块名称
     */
    @Schema(description = "模块名称")
    private String moduleName;

    /**
     * 方法名称
     */
    @Schema(description = "方法名称")
    private String methodName;

    /**
     * 是否成功
     */
    @Schema(description = "是否成功")
    private String status;

    /**
     * 日志内容
     */
    @Schema(description = "日志内容")
    private String log;

    /**
     * 插入时间
     */
    @Schema(description = "插入时间")
    @DateTimeFormat(pattern = DateConstants.format.YYYY_MM_DD_HH_mm_ss)
    @JsonFormat(pattern = DateConstants.format.YYYY_MM_DD_HH_mm_ss)
    private LocalDateTime insertTime;

    /**
     * 父级调用runId
     */
    @Schema(description = "父级调用runId")
    private String parentRunId;

    /**
     * 执行耗时（毫秒）
     */
    @Schema(description = "执行耗时（毫秒）")
    private Long executionTime;
}
