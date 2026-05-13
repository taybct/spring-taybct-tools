package io.github.taybct.tool.scheduledlogcentralized.collector;



import cn.hutool.core.collection.CollectionUtil;
import io.github.taybct.tool.scheduledlogcentralized.context.ScheduledRunContext;
import io.github.taybct.tool.scheduledlogcentralized.dto.ScheduledRunSnapshot;
import io.github.taybct.tool.scheduledlogcentralized.util.ScheduledErrorHolder;
import io.github.taybct.tool.scheduledlogcentralized.util.ScheduledThreadLogBuffer;
import io.github.taybct.tool.core.message.IMessageSendService;
import io.github.taybct.tool.core.message.scheduledLogCentralized.ScheduledLogCentralizedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ScheduledLogCollector {
    @Autowired(required = false)
    protected IMessageSendService messageSendService;


    public void collect(String runId) {
        if (!ScheduledRunContext.isLogEnabled()){
            return;
        }
        List<String> logs = ScheduledThreadLogBuffer.getAndClear();
        String status = ScheduledErrorHolder.getStatus();
        Throwable error = ScheduledErrorHolder.getAndClear();
        if (error != null) {
            logs.add(ScheduledErrorHolder.getThrowableString(error));
        }
        ScheduledRunSnapshot snapshot = ScheduledRunContext.snapshot();
        ScheduledLogCentralizedDTO scheduledLogCentralizedDto = new ScheduledLogCentralizedDTO();
        scheduledLogCentralizedDto.setSuccessTime(LocalDateTime.now());
        if (Objects.nonNull(snapshot)) {
            scheduledLogCentralizedDto.setRunId(snapshot.getRunId());
            scheduledLogCentralizedDto.setParentRunId(snapshot.getParentRunId());
            scheduledLogCentralizedDto.setModuleName(snapshot.getModule());
            scheduledLogCentralizedDto.setMethodName(snapshot.getMethod());
            LocalDateTime startTime = snapshot.getStartTime();
            long millis = Duration.between(startTime, LocalDateTime.now(ZoneId.of("Asia/Shanghai"))).toMillis();
            scheduledLogCentralizedDto.setExecutionTime(millis);
        }
        scheduledLogCentralizedDto.setStatus(status);
        //如果没有收到日志打印，设置null值
        String message = CollectionUtil.isEmpty(logs) ? null : String.join("", logs);
        scheduledLogCentralizedDto.setLog(message);
        //使用发送mq的方式
        messageSendService.send(scheduledLogCentralizedDto);
    }
}