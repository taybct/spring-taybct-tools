package io.github.taybct.tool.core.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class MdcAbsentFilter extends Filter<ILoggingEvent> {

    private String key;

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public FilterReply decide(ILoggingEvent event) {

        if (key == null) {
            return FilterReply.NEUTRAL;
        }

        String val = event.getMDCPropertyMap().get(key);

        return val == null ? FilterReply.ACCEPT : FilterReply.DENY;
    }
}