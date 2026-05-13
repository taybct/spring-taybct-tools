package io.github.taybct.tool.scheduledlogcentralized.util;

import io.github.taybct.tool.scheduledlogcentralized.enums.ScheduledLogCentralizedStatusEnum;
import io.github.taybct.tool.core.constant.DateConstants;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ScheduledErrorHolder {

    private static final ThreadLocal<Throwable> ERROR = new ThreadLocal<>();

    public static void record(Throwable t) {
        ERROR.set(t);
    }

    public static Throwable getAndClear() {
        Throwable t = ERROR.get();
        ERROR.remove();
        return t;
    }
    public static String getStatus() {
        Throwable t = ERROR.get();
        return t == null ? ScheduledLogCentralizedStatusEnum.SUCCESS : ScheduledLogCentralizedStatusEnum.FAILED;
    }

    public static String getThrowableString(Throwable t) {
        String str = null;
        if (t != null) {
            String nowStr = DateTimeFormatter.ofPattern(DateConstants.format.YYYY_MM_DD_HH_mm_ss).format(LocalDateTime.now());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.flush();
            StringBuffer sb = sw.getBuffer(); // StringBuffer
            str = nowStr + "：" + sb.toString();
        }
        return str;
    }
}