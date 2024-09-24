package io.github.mangocrisp.spring.taybct.tool.core.mybatis.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import io.github.mangocrisp.spring.taybct.tool.core.constant.DateConstants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * JDBC 操作数据库字段工具类
 */
public class JDBCFieldUtil {
    /**
     * Java 字段转换 Database 字段
     */
    public static class J2D {
        /**
         * 字符串类型
         *
         * @param dbType 数据库类型
         * @param o      值
         * @return 结果
         */
        public static String varchar(DbType dbType, Object o) {
            if (!(o instanceof CharSequence)) {
                return "null";
            }
            return String.format("'%s'", o);
        }

        /**
         * 数字类型
         *
         * @param dbType 数据库类型
         * @param o      值
         * @return 结果
         */
        public static String number(DbType dbType, Object o) {
            if (!(o instanceof Number)) {
                return "null";
            }
            return String.format("%s", o);
        }

        /**
         * 日期类型
         *
         * @param dbType 数据库类型
         * @param o      值
         * @return 结果
         */
        public static String dateTime(DbType dbType, Object o) {
            if (o instanceof Date) {
                Date v = (Date) o;
                switch (dbType) {
                    case POSTGRE_SQL:
                        return String.format("'%s'::timestamp", DateUtil.format(v, DateConstants.format.YYYY_MM_DD_HH_mm_ss + ".SSS"));
                    case ORACLE:
                        return String.format("to_date('%s', 'yyyy-MM-dd HH24:mm:ss.SSS')", DateUtil.format(v, DateConstants.format.YYYY_MM_DD_HH_mm_ss + ".SSS"));
                    default:
                        return String.format("'%s'", DateUtil.format(v, DateConstants.format.YYYY_MM_DD_HH_mm_ss + ".SSS"));
                }
            }
            if (o instanceof LocalDateTime) {
                LocalDateTime v = (LocalDateTime) o;
                switch (dbType) {
                    case POSTGRE_SQL:
                        return String.format("'%s'::timestamp", DateUtil.format(v, DateConstants.format.YYYY_MM_DD_HH_mm_ss + ".SSS"));
                    case ORACLE:
                        return String.format("to_date('%s', 'yyyy-MM-dd HH24:mm:ss.SSS')", DateUtil.format(v, DateConstants.format.YYYY_MM_DD_HH_mm_ss + ".SSS"));
                    default:
                        return String.format("'%s'", DateUtil.format(v, DateConstants.format.YYYY_MM_DD_HH_mm_ss + ".SSS"));
                }
            }
            if (o instanceof LocalDate) {
                LocalDate v = (LocalDate) o;
                switch (dbType) {
                    case POSTGRE_SQL:
                        return String.format("'%s'::date", LocalDateTimeUtil.format(v, DateConstants.format.YYYY_MM_DD));
                    case ORACLE:
                        return String.format("to_date('%s', 'yyyy-MM-dd HH24:mm:ss.SSS')", LocalDateTimeUtil.format(v, DateConstants.format.YYYY_MM_DD));
                    default:
                        return String.format("'%s'", LocalDateTimeUtil.format(v, DateConstants.format.YYYY_MM_DD));
                }
            }
            return "null";
        }

    }

}
