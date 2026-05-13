package io.github.taybct.tool.scheduledlogcentralized.util;

import java.util.ArrayList;
import java.util.List;

public final class ScheduledThreadLogBuffer {

    private static final ThreadLocal<List<String>> BUFFER =
            ThreadLocal.withInitial(ArrayList::new);

    public static void append(String log) {
        BUFFER.get().add(log);
    }

    public static List<String> getAndClear() {
        List<String> copy = new ArrayList<>(BUFFER.get());
        BUFFER.remove();
        return copy;
    }
}
