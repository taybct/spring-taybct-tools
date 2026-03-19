package io.github.taybct.scheduledLogCentralized.dto;

import java.io.Serializable;

public class ScheduledRunSnapshot implements Serializable {

    private final String runId;
    private final String module;
    private final String method;
    private final String parentRunId;

    public ScheduledRunSnapshot(String runId, String module, String method) {
        this.runId = runId;
        this.module = module;
        this.method = method;
        this.parentRunId = null;
    }
    public ScheduledRunSnapshot(String runId, String module, String method,String parentRunId) {
        this.runId = runId;
        this.module = module;
        this.method = method;
        this.parentRunId = parentRunId;
    }

    public String getRunId() { return runId; }
    public String getModule() { return module; }
    public String getMethod() { return method; }
    public String getParentRunId() { return parentRunId; }
}