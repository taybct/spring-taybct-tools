package io.github.taybct.tool.core.util.tree;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 基础树<br>
 * 定义了一个树结构的每一层基本需要有哪些字段，在{@link #detail} 里面可以扩展一些
 * 在基础树里面没有定义的字段，如果有要扩展这些字段，你可以把这个扩展类型给加上，方便
 * 反序列化 {@link #detailClass}
 *
 * @author xijieyin <br> 2022/8/5 18:49
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "基础树")
public class BaseTree implements TreeUtil.Tree<BaseTree> {

    @Serial
    private static final long serialVersionUID = -4711533324121794046L;
    /**
     * 主键
     */
    @Schema(description = "级别")
    private Integer level;
    /**
     * 主键
     */
    @Schema(description = "主键")
    private Serializable id;
    /**
     * 父 id
     */
    @Schema(description = "父 id")
    private Serializable sort;
    /**
     * 父级 id
     */
    @Schema(description = "父级 id")
    private Serializable parentId;
    /**
     * 标签
     */
    @Schema(description = "标签")
    private String label;
    /**
     * 详情
     */
    @Schema(description = "详情")
    private JSONObject detail;
    /**
     * 记录详情的类型，方便回显
     */
    @Schema(description = "记录详情的类型，方便回显")
    private Class<?> detailClass;
    /**
     * 子集
     */
    @Schema(description = "子集")
    private List<BaseTree> children;

    @Override
    public void setChildren(List<BaseTree> children) {
        this.children = children;
    }

    /**
     * 转换详情到 java bean
     */
    public <T> T convertDetailToJavaBean() {
        return (T) detail.toJavaObject(detailClass);
    }
}
