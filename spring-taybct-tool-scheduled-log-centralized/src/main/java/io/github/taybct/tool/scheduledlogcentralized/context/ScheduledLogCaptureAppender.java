package io.github.taybct.tool.scheduledlogcentralized.context;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;
import io.github.taybct.tool.scheduledlogcentralized.config.ScheduledLogContant;
import io.github.taybct.tool.scheduledlogcentralized.util.ScheduledThreadLogBuffer;
import io.github.taybct.tool.core.constant.DateConstants;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScheduledLogCaptureAppender extends AppenderBase<ILoggingEvent> {


    @Override
    protected void append(ILoggingEvent event) {

        String runId = event.getMDCPropertyMap().get(ScheduledLogContant.MDC_RUN_ID);
        if (runId == null) {
            return;
        }
        String nowStr = DateTimeFormatter.ofPattern(DateConstants.format.YYYY_MM_DD_HH_mm_ss).format(LocalDateTime.now());
        StringBuilder sb = new StringBuilder();
        sb.append(nowStr).append("：").append("[").append(event.getLevel()).append("] ")
                .append(event.getFormattedMessage()).append("\n");

        IThrowableProxy tp = event.getThrowableProxy();
        if (tp != null) {
            appendThrowable(sb, tp);
        }

        ScheduledThreadLogBuffer.append(sb.toString());
    }

    private void appendThrowable(StringBuilder sb, IThrowableProxy tp) {
        sb.append(tp.getClassName()).append(": ")
                .append(tp.getMessage()).append("\n");

        for (StackTraceElementProxy p : tp.getStackTraceElementProxyArray()) {
            sb.append("\tat ").append(p.getSTEAsString()).append("\n");
        }

        if (tp.getCause() != null) {
            sb.append("Caused by:\n");
            appendThrowable(sb, tp.getCause());
        }
    }
}