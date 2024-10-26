package io.github.mangocrisp.spring.taybct.tool.core.mybatis.util;

import io.github.mangocrisp.spring.taybct.tool.core.mybatis.constant.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * <pre>
 * 排序字段
 * </pre>
 *
 * @author XiJieYin
 * @since 2024/10/20 16:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderByField implements Serializable {

    
    private static final long serialVersionUID = 4615853301498882427L;
    /**
     * 字段名
     */
    String field;
    /**
     * 正序或者倒序
     */
    String sc = Constants.ASC;

}
