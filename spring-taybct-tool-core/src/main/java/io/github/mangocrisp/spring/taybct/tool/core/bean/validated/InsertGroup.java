package io.github.mangocrisp.spring.taybct.tool.core.bean.validated;

/**
 * 校验分组，新增的时候的校验<br>
 * {@link org.springframework.validation.annotation.Validated} 里面的有做详细的说明
 * 意思是他做校验的时候会根据分组的不同对不同的字段做校验 {@link javax.validation.constraints}
 * 包下面的这些注释里面都有一个 {@code Class<?>[] groups() default { };}，可以用来指定类型
 *
 * @author xijieyin <br> 2022/8/5 14:45
 * @see org.springframework.validation.annotation.Validated
 * @see javax.validation.constraints
 * @since 1.0.0
 */
public interface InsertGroup {
}
