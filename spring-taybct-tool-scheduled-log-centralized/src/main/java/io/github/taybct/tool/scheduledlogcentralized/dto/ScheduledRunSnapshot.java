package io.github.taybct.tool.scheduledlogcentralized.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;


public class ScheduledRunSnapshot implements Serializable {

    @Serial
    private static final long serialVersionUID = -482039441844078296L;

    private final String runId;
    private final String module;
    private final String method;
    private final String parentRunId;
    private final LocalDateTime startTime;

    public ScheduledRunSnapshot(String runId, String module, String method) {
        this.runId = runId;
        this.module = module;
        this.method = method;
        this.parentRunId = null;
        this.startTime =  LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
    }
    public ScheduledRunSnapshot(String runId, String module, String method,String parentRunId) {
        this.runId = runId;
        this.module = module;
        this.method = method;
        this.parentRunId = parentRunId;
        this.startTime =  LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
    }

    public String getRunId() { return runId; }
    public String getModule() { return module; }
    public String getMethod() { return method; }
    public String getParentRunId() { return parentRunId; }
    public LocalDateTime getStartTime() { return startTime; }
}