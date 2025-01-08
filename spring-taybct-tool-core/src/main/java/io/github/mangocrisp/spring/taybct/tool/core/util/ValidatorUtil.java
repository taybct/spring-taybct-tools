package io.github.mangocrisp.spring.taybct.tool.core.util;

import cn.hutool.extra.validation.ValidationUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.HibernateValidator;

import java.util.Iterator;
import java.util.Set;

/**
 * <pre>
 * 手动校验器
 * </pre>
 *
 * @author xijieyin
 * @since 2024/11/22 15:14
 */
@Slf4j
public class ValidatorUtil extends ValidationUtil {

    private static Validator validatorFast = Validation.byProvider(HibernateValidator.class).configure().failFast(true).buildValidatorFactory().getValidator();
    private static Validator validatorAll = Validation.byProvider(HibernateValidator.class).configure().failFast(false).buildValidatorFactory().getValidator();

    /**
     * 校验遇到第一个不合法的字段直接返回不合法字段，后续字段不再校验
     *
     * @param <T>    校验类型
     * @param domain 实体类
     * @return 结果
     * @since 2024/11/22 15:14
     */
    public static <T> Set<ConstraintViolation<T>> validateFast(T domain) {
        Set<ConstraintViolation<T>> validateResult = validatorFast.validate(domain);
        if (validateResult.size() > 0) {
            log.error(validateResult.iterator().next().getPropertyPath() + "：" + validateResult.iterator().next().getMessage());
            throw new ConstraintViolationException(validateResult);
        }
        return validateResult;
    }

    /**
     * 校验所有字段并返回不合法字段
     *
     * @param <T>    校验类型
     * @param domain 实体类
     * @return 结果
     * @since 2024/11/22 15:14
     */
    public static <T> Set<ConstraintViolation<T>> validateAll(T domain) {
        Set<ConstraintViolation<T>> validateResult = validatorAll.validate(domain);
        if (validateResult.size() > 0) {
            Iterator<ConstraintViolation<T>> it = validateResult.iterator();
            while (it.hasNext()) {
                ConstraintViolation<T> cv = it.next();
                log.debug(cv.getPropertyPath() + "：" + cv.getMessage());
            }
        }
        return validateResult;
    }

}
