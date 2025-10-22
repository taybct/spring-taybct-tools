package io.github.taybct.tool.core.aop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.taybct.tool.core.annotation.SafeConvert;
import io.github.taybct.tool.core.bean.ITypeConvert;
import io.github.taybct.tool.core.enums.EntityType;
import io.github.taybct.tool.core.result.R;
import io.github.taybct.tool.core.util.AOPUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import java.io.Serializable;
import java.util.Collection;

/**
 * SafeConvert 切面
 *
 * @author xijieyin <br> 2022/8/5 13:39
 * @see SafeConvert
 * @since 1.0.0
 */
@Aspect
@AutoConfiguration
@Slf4j
@ConditionalOnClass({IPage.class})
public class SafeConvertAspect implements ITypeConvert {

    /**
     * @param point       切点
     * @param safeConvert 注解
     */
    @Around("@annotation(safeConvert)")
    public Object doAround(ProceedingJoinPoint point, SafeConvert safeConvert) throws Throwable {
        log.debug("安全转换开始======");
        int index = AOPUtil.getParamIndex(point, safeConvert.key());
        // 获取到所有的参数
        Object[] args = point.getArgs();
        // 如果需要输入转换
        if (index > -1
                && index < point.getArgs().length
                && args[index] instanceof Serializable
        ) {
            log.debug("安全转换开始======安全输入");
            if (safeConvert.safeIn().length > 0) {
                // 找到需要输入转换的字段的位置进行转换
                args[index] = entityConvert2Some(args[index], safeConvert.safeIn()[0], safeConvert.ignoreIn());
            } else {
                // 如果不指定 安全输入的类 就是自己转成自己的类型，但是按照忽略字段来转换，被忽略的字段会被丢弃
                args[index] = entityConvert2Some(args[index], args[index].getClass(), safeConvert.ignoreIn());
            }
        }
        Object proceed = point.proceed(args);
        // 执行得到返回结果后，判断，如果需要进行输出结果转换
        if (safeConvert.safeOut().length > 0) {
            log.debug("安全转换开始======安全输出");
            Class<? extends Serializable> safeOutClass = safeConvert.safeOut()[0];
            // 如果结果的类型是 R
            if (proceed instanceof R r) {
                EntityType resultType = safeConvert.resultType();

                if (resultType.equals(EntityType.Entity)) {
                    return convert2SomeResult((R) proceed, safeOutClass, safeConvert.ignoreOut());
                } else if (resultType.equals(EntityType.Collection)) {
                    return convert2SomeResultCollection((R) proceed, safeOutClass, safeConvert.ignoreOut());
                } else if (resultType.equals(EntityType.Page)) {
                    return convert2SomeResultPage((R) proceed, safeOutClass, safeConvert.ignoreOut());
                }

                if (r.getData() instanceof Collection) {
                    return convert2SomeResultCollection(r, safeOutClass, safeConvert.ignoreOut());
                } else if (r.getData() instanceof IPage) {
                    return convert2SomeResultPage(r, safeOutClass, safeConvert.ignoreOut());
                } else {
                    return convert2SomeResult(r, safeOutClass, safeConvert.ignoreOut());
                }
            }
            log.debug("安全转换结束======");
        }
        return proceed;
    }

}
