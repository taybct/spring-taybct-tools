package io.github.taybct.tool.core.mybatisUtil;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;

/**
 * 根据传入字段判断_后面的规则，拼接查询sql
 *
 * @author jopson
 * @since 2020/9/21 11:38 上午
 */
@Slf4j
public class SqlKeyword {

    private static final String ALLOW_SUFFIX = "|_equal|_eq|_not_equal|_like|_like_left|_like_right|_like_multiple|_not_like|_gt|_lt|_gt_time|_lt_time|_ge|_le|_in|_date_gt|_date_equal_date_lt|_null|_not_null|_ignore|";

    private static final String EQUAL = "_equal";
    private static final String EQ = "_eq";
    private static final String NOT_EQUAL = "_not_equal";
    private static final String LIKE = "_like";
    private static final String LIKE_LEFT = "_like_left";
    private static final String LIKE_RIGHT = "_like_right";
    private static final String MULTIPLE = "_like_multiple";
    private static final String NOT_LIKE = "_not_like";
    private static final String GT = "_gt";
    private static final String LT = "_lt";
    private static final String GTTime = "_gt_time";
    private static final String LTTime = "_lt_time";
    private static final String GE = "_ge";
    private static final String LE = "_le";
    private static final String IN = "_in";
    private static final String DATE_GT = "_date_gt";
    private static final String DATE_EQUAL = "_date_equal";
    private static final String DATE_LT = "_date_lt";
    private static final String IS_NULL = "_null";
    private static final String NOT_NULL = "_not_null";
    private static final String IGNORE = "_ignore";

    // 默认字段
    private static final String DEFAULT_FIELD = "|id|isDeleted|createUser|createTime|updateUser|updateTime|expansion|";

    public SqlKeyword() {
    }

    public static void buildCondition(Map<String, Object> query, QueryWrapper<?> qw) {
        if (!StringUtils.isEmpty(query)) {
            Class<?> entityClass = qw.getEntity().getClass();
            query.forEach((k, v) -> {
                boolean isOk = false;
                try {
                    /** 非实体类参数不允许拼接条件 **/
                    if (k.indexOf("_") > 0) {
                        // 有后缀的，去掉后缀
                        String fieldName = k.substring(0, k.indexOf("_"));
                        // 不是默认字段 就得是实体类中的字段
                        if (!DEFAULT_FIELD.contains("|" + fieldName + "|")) {
                            entityClass.getDeclaredField(fieldName);
                        }
                        /** 非允许后缀不允拼接 **/
                        String fieldSuffix = k.substring(k.indexOf("_"));
                        if (ALLOW_SUFFIX.contains("|" + fieldSuffix + "|")) {
                            isOk = true;
                        }
                    } else {
                        // 是默认字段就直接返回true
                        if (DEFAULT_FIELD.contains("|" + k + "|")) {
                            isOk = true;
                        } else {
                            // 没有后缀的，不是默认字段 就得是实体类中的字段
                            entityClass.getDeclaredField(k);
                            isOk = true;
                        }
                    }
                } catch (Exception e) {
                    log.info("字段{}不存在", k);
                }

                if (isOk && !StringUtils.hasEmpty(new Object[]{k, v}) && !k.endsWith(IGNORE)) {
                    if (k.endsWith(EQUAL)) {
                        qw.eq(getColumn(k, EQUAL), v);
                    } else if (k.endsWith(EQ)) {
                        qw.eq(getColumn(k, EQ), v);
                    } else if (k.endsWith(NOT_EQUAL)) {
                        qw.ne(getColumn(k, NOT_EQUAL), v);
                    } else if (k.endsWith(NOT_LIKE)) {
                        qw.notLike(getColumn(k, NOT_LIKE), v);
                    } else if (k.endsWith(GT)) {
                        qw.gt(getColumn(k, GT), v);
                    } else if (k.endsWith(LT)) {
                        qw.lt(getColumn(k, LT), v);
                    } else if (k.endsWith(GE)) {
                        qw.ge(getColumn(k, GE), v);
                    } else if (k.endsWith(LE)) {
                        qw.le(getColumn(k, LE), v);
                    } else if (k.endsWith(IN)) {
                        // 这里暂时逗号隔空，后续可以增加类型区分
                        if (v != null) {
                            qw.in(getColumn(k, IN), v.toString().split(","));
                        }
                    } else if (k.endsWith(DATE_GT)) {
                        qw.gt(getColumn(k, DATE_GT), v);
                    } else if (k.endsWith(DATE_EQUAL)) {
                        qw.eq(getColumn(k, DATE_EQUAL), v);
                    } else if (k.endsWith(DATE_LT)) {
                        qw.lt(getColumn(k, DATE_LT), v);
                    } else if (k.endsWith(IS_NULL)) {
                        qw.isNull(getColumn(k, IS_NULL));
                    } else if (k.endsWith(NOT_NULL)) {
                        qw.isNotNull(getColumn(k, NOT_NULL));
                    } else if (k.endsWith(LIKE_LEFT)) {
                        qw.likeLeft(getColumn(k, LIKE_LEFT), v);
                    } else if ((k.endsWith(LIKE_RIGHT))) {
                        qw.likeRight(getColumn(k, LIKE_RIGHT), v);
                    } else if ((k.endsWith(MULTIPLE))) {
                        String column = getColumn(k, MULTIPLE);
                        String str = v.toString().replace(",", "|");
                        qw.apply(column + " ~* {0} ", str);
                    } else if ((k.endsWith(LIKE))) {
                        qw.like(getColumn(k, LIKE), v);
                    } else if (k.endsWith(GTTime)) {
                        Date date = getDate(v);
                        String column = getColumn(k, GTTime);
                        qw.gt(column, date);
                    } else if (k.endsWith(LTTime)) {
                        Date date = getDate(v);
                        qw.lt(getColumn(k, LTTime), date);
                    } else {
                        qw.eq(getColumn(k, EQUAL), v);
                    }

                }
            });
        }
    }

    private static Date getDate(Object v) {
        String vString = v.toString();
        Date date = null;
        if (vString.length() == 10) {
            date = DateUtil.parseDate(v.toString());
        } else if (vString.length() == 16) {
            date = DateUtil.parse(v.toString(), DatePattern.NORM_DATETIME_MINUTE_PATTERN);
        } else {
            date = DateUtil.parseDateTime(v.toString());
        }
        return date;
    }

    private static String getColumn(String column, String keyword) {
        return StringUtils.humpToUnderline(StringUtils.removeSuffix(column, keyword));
    }

    public static String filter(String param) {
        return param == null ? null : param.replaceAll("(?i)'|%|--|insert|delete|select|count|group|union|drop|truncate|alter|grant|execute|exec|xp_cmdshell|call|declare|sql", "");
    }


}


