package io.github.mangocrisp.spring.taybct.tool.core.bean;

import io.github.mangocrisp.spring.taybct.tool.core.util.BeanUtil;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * 源模型
 *
 * @param <T> 实体类
 */
@Data
@Schema(description = "原实体类字段数据传输对象")
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OriginalModel<T> implements Serializable, ModelConvertible<T> {

    private static final long serialVersionUID = -4873265993906544402L;

    /**
     * 实体类源字段对象
     */
    @Schema(description = "实体类源字段对象")
    @NotNull
    private T data;

    @Override
    public T bean(String... ignoreProperties) {
        T bean;
        setConvertedBean(bean = (T) BeanUtil.copyProperties(this.data, data.getClass(), ignoreProperties));
        return bean;
    }

    @Hidden
    private T convertedBean;

}
