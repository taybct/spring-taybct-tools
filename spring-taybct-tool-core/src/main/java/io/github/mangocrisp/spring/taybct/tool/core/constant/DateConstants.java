package io.github.mangocrisp.spring.taybct.tool.core.constant;

import org.apache.commons.lang3.time.TimeZones;

import java.util.TimeZone;

/**
 * 日期常量
 *
 * @author xijieyin <br> 2022/8/5 18:17
 * @since 1.0.0
 */
public class DateConstants {

    /**
     * 日期格式常量
     */
    public interface format {
        String YYYY_MM_DD = "yyyy-MM-dd";
        String HH_mm_ss = "HH:mm:ss";
        String YYYY_MM_DD_HH_mm_ss = YYYY_MM_DD + " " + HH_mm_ss;
        String YYYYMMDD = "yyyyMMdd";
        String HHmmss = "HHmmss";
        String YYYYMMDDHHmmss = YYYYMMDD + HHmmss;
    }

    /**
     * 时区
     */
    public interface Zone {
        String CHINA_ID = TimeZones.GMT_ID + "+8:00";
        /**
         * 中国时区
         */
        TimeZone CHINA = TimeZone.getTimeZone(CHINA_ID);
    }


}
