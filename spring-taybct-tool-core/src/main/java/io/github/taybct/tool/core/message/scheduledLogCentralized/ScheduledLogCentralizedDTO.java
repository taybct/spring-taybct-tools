package io.github.taybct.tool.core.message.scheduledLogCentralized;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.github.taybct.tool.core.constant.DateConstants;
import io.github.taybct.tool.core.message.Message;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
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
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledLogCentralizedDTO implements Message{

    @Serial
    private static final long serialVersionUID = -5529128318737325029L;

    /**
     * 生成日志时间
     */
    @DateTimeFormat(pattern = DateConstants.format.YYYY_MM_DD_HH_mm_ss)
    @JsonFormat(pattern = DateConstants.format.YYYY_MM_DD_HH_mm_ss)
    private LocalDateTime successTime;

    /**
     * 本次运行id
     */
    private String runId;

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 是否成功
     */
    private String status;

    /**
     * 日志内容
     */
    private String log;

    /**
     * 插入时间
     */
    @DateTimeFormat(pattern = DateConstants.format.YYYY_MM_DD_HH_mm_ss)
    @JsonFormat(pattern = DateConstants.format.YYYY_MM_DD_HH_mm_ss)
    private LocalDateTime insertTime;

    /**
     * 父级调用runId
     */
    private String parentRunId;

    /**
     * 执行耗时（毫秒）
     */
    private Long executionTime;

    @Override
    @JsonIgnore
    public String getPayload() {
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteMapNullValue);
    }

    @Override
    public boolean getRealTime() {
        return true;
    }
}
