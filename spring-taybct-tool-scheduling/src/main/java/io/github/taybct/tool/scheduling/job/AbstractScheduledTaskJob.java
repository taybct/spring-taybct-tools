package io.github.taybct.tool.scheduling.job;

import com.alibaba.fastjson2.JSONObject;
import io.github.taybct.tool.core.enums.OperateStatus;
import io.github.taybct.tool.core.util.StringUtil;
import io.github.taybct.tool.scheduling.prop.ScheduledTaskBean;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * 任务调度抽象实现
 *
 * @author xijieyin <br> 2022/11/1 14:47
 * @since 1.1.0
 */
@Slf4j
public abstract class AbstractScheduledTaskJob implements IScheduledTaskJob {

    @Getter
    protected ScheduledTaskBean taskConfig;

    @Override
    public void config(ScheduledTaskBean taskBean) {
        this.taskConfig = taskBean;
    }

    /**
     * 日记记录开始时间
     */
    protected LocalDateTime startTime;
    /**
     * 日志记录停止时间
     */
    protected LocalDateTime stopTime;
    /**
     * 日志记录器
     */
    protected Consumer<JSONObject> logRecorder;
    /**
     * 停止状态
     */
    private boolean stopFlag = false;

    /**
     * 日志记录收集器
     */
    protected Consumer<JSONObject> getLogRecorder() {
        return null;
    }

    /**
     * 这个 run spring scheduling 会调用
     */
    @Override
    public void run() {
        if (getLogRecorder() != null) {
            startRecord(getLogRecorder());
        }
        try {
            log.debug(">>>>>> 任务 [{}] 开始 >>>>>> ", this.taskConfig.getDescription());
            this.run(this.taskConfig.getParams());
            log.debug(">>>>>> 任务 [{}] 结束 >>>>>> ", this.taskConfig.getDescription());
            if (getLogRecorder() != null) {
                stopRecord(OperateStatus.SUCCESS.intCode(), String.format("任务 [%s] 执行完毕", this.taskConfig.getDescription()));
            }
        } catch (Exception e) {
            log.error(">>>>>> 任务 [{}] 异常 >>>>>> ", this.taskConfig.getDescription());
            log.error("\r\n", e);
            if (getLogRecorder() != null) {
                stopRecord(OperateStatus.FAILED.intCode(), String.format("任务 [%s] 执行完毕", this.taskConfig.getDescription()), e.getMessage());
            }
        }
    }

    /**
     * 开始记录日志
     *
     * @param logRecorder 日志记录者
     * @author xijieyin <br> 2022/11/1 17:41
     * @since 1.1.0
     */
    public void startRecord(Consumer<JSONObject> logRecorder) {
        this.startTime = LocalDateTime.now();
        log.debug(">>>>>> 日志记录开始 {}  >>>>>>", this.startTime);
        this.logRecorder = logRecorder;
        this.stopFlag = false;
    }

    /**
     * 结束记录日志
     *
     * @param status  状态
     * @param message 消息
     */
    public void stopRecord(Integer status, String message) {
        stopRecord(status, message, null);
    }

    /**
     * 结束记录日志
     *
     * @param status        状态
     * @param message       消息
     * @param exceptionInfo 异常信息
     */
    public void stopRecord(Integer status, String message, String exceptionInfo) {
        if (this.stopFlag) {
            return;
        }
        this.stopFlag = true;
        this.stopTime = LocalDateTime.now();
        log.debug(">>>>>> 日志记录结束 {}  >>>>>>", this.stopTime);
        JSONObject logRecord = new JSONObject();
        logRecord.put("taskKey", this.taskConfig.getTaskKey());
        logRecord.put("uniKey", this.taskConfig.getUniKey());
        logRecord.put("description", this.taskConfig.getDescription());
        logRecord.put("params", this.taskConfig.getParams());
        logRecord.put("status", status);
        logRecord.put("message", message);
        if (StringUtil.isNotEmpty(exceptionInfo)) {
            logRecord.put("exceptionInfo", exceptionInfo);
        }
        logRecord.put("startTime", this.startTime);
        logRecord.put("stopTime", this.stopTime);
        this.logRecorder.accept(logRecord);
    }

}
