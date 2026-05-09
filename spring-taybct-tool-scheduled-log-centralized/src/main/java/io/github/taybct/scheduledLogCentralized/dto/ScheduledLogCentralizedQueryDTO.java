package io.github.taybct.scheduledLogCentralized.dto;

import io.github.taybct.scheduledLogCentralized.entity.ScheduledLogCentralized;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 定时任务日志管理查询参数<br>
 * api_log
 *
 * @author xijieyin <br> 2022/8/5 9:47
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "定时任务日志管理查询参数")
public class ScheduledLogCentralizedQueryDTO extends ScheduledLogCentralized {
    @Serial
    private static final long serialVersionUID = -507794178290101364L;

    /**
     * 生成日志时间-开始时间
     */
    @Schema(description = "生成日志时间-开始时间")
    private LocalDateTime successTimeStartTime;
    /**
     * 生成日志时间-结束时间
     */
    @Schema(description = "生成日志时间-结束时间")
    private LocalDateTime successTimeEndTime;

}
