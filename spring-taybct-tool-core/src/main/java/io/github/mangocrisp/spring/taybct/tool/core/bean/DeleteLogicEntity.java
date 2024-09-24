package io.github.mangocrisp.spring.taybct.tool.core.bean;

import com.baomidou.mybatisplus.annotation.TableLogic;
import io.github.mangocrisp.spring.taybct.tool.core.annotation.TableFieldDefault;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 逻辑删除实体类
 *
 * @author xijieyin <br> 2022/8/5 17:37
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DeleteLogicEntity<P extends Serializable, U extends Serializable> extends BaseEntity<P, U> {

    private static final long serialVersionUID = -8562886762482129322L;
    /**
     * 状态[0:未删除,1:删除]
     */
    @TableLogic
    @Schema(description = "是否已删除")
    @TableFieldDefault("0")
    private Byte isDeleted;

}
