package io.github.mangocrisp.spring.taybct.tool.scheduling.job;

import com.alibaba.fastjson2.JSONObject;
import io.github.mangocrisp.spring.taybct.tool.core.enums.OperateStatus;
import io.github.mangocrisp.spring.taybct.tool.core.util.StringUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 任务调度抽象实现
 *
 * @author xijieyin <br> 2022/11/1 14:47
 * @since 1.1.0
 */
@Slf4j
public abstract class AbstractScheduledTaskJob implements IScheduledTaskJob {

    /**
     * 任务 key
     */
    @Setter
    protected String key;
    /**
     * cron 表达式
     */
    @Setter
    protected String cron;

    /**
     * 任务描述
     */
    @Setter
    protected String description;

    /**
     * 初始化参数
     */
    @Setter
    protected Map<String, Object> params = new HashMap<>();
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
     * 这个 run 需要用户自己支写逻辑
     *
     * @param params 参数就是配置的参数
     */
    public abstract void run(Map<String, Object> params) throws Exception;

    /**
     * 这个 run spring scheduling 会调用
     */
    @Override
    public void run() {
        if (getLogRecorder() != null) {
            startRecord(getLogRecorder());
        }
        try {
            log.debug(">>>>>> 任务 [{}] 开始 >>>>>> ", this.description);
            run(this.params);
            log.debug(">>>>>> 任务 [{}] 结束 >>>>>> ", this.description);
            if (getLogRecorder() != null) {
                stopRecord(OperateStatus.SUCCESS.getIntCode(), String.format("任务 [%s] 执行完毕", this.description));
            }
        } catch (Exception e) {
            log.error(">>>>>> 任务 [{}] 异常 >>>>>> ", this.description);
            log.error("\r\n", e);
            if (getLogRecorder() != null) {
                stopRecord(OperateStatus.FAILED.getIntCode(), String.format("任务 [%s] 执行完毕", this.description), e.getMessage());
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
        logRecord.put("taskKey", this.key);
        logRecord.put("description", this.description);
        logRecord.put("params", this.params);
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
