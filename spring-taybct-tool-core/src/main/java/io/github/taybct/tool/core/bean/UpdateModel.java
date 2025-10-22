package io.github.taybct.tool.core.bean;

import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSONObject;
import io.github.taybct.tool.core.exception.def.BaseException;
import io.github.taybct.tool.core.util.BeanUtil;
import io.github.taybct.tool.core.util.ObjectUtil;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 修改参数模型
 *
 * @param <T> 实体类
 * @param <M> 需要被修改的数据也就是实体类
 * @param <P> 修改这个实体类需要的查询参数
 */
@Schema(description = "修改对象模型")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UpdateModel<T
        , M extends ModelConvertible<? extends T>
        , P extends ModelConvertible<? extends T>> implements Serializable {

    @Serial
    private static final long serialVersionUID = -2960287293763696007L;

    /**
     * 需要被修改的数据，如果有批量修改的数据就批量修改里面的数据的优先黑更高，这个数据的值就只当作是默认值，如果没有批量修改的数据会生成一条默认的
     */
    @Schema(description = "需要被修改的数据")
    private M data;

    /**
     * 转换之后的需要修改的实体类数据
     */
    @Hidden
    private T bean;

    /**
     * 如果批量修改里的数据里面没有 dto 设置就使用最外层的这个，如果有就以里面的优先级更高
     */
    @Schema(description = "修改数据使用到的参数")
    private P dto;

    /**
     * 如果批量修改里的数据里面没有 params 设置就使用最外层的这个，如果有就以里面的优先级更高
     */
    @Hidden
    private JSONObject params;

    @Hidden
    private boolean converted = false;

    @Hidden
    protected void setBean(T bean) {
        throw new BaseException("not support!");
    }

    @Hidden
    public T getBean() {
        if (this.bean == null) {
            if (ObjectUtil.isNotEmpty(this.data)) {
                this.bean = this.data.bean();
            }
        }
        return this.bean;
    }

    @Hidden
    public void setParams(JSONObject params) {
        throw new BaseException("not support!");
    }

    @Hidden
    public JSONObject getParams() {
        if (this.params != null) {
            return this.params;
        }
        if (this.dto == null) {
            return new JSONObject();
        }
        return (this.params = JSONObject.from(dto));
    }

    @Hidden
    protected boolean isConverted() {
        return converted;
    }

    @Hidden
    protected void setConverted(boolean converted) {
        throw new BaseException("not support!");
    }

    /**
     * 批量修改的数据，默认只有第二层有效
     */
    @Schema(description = "批量修改的数据，默认只有第二层有效")
    private List<UpdateModel<T, M, P>> updateList;

    /**
     * 批量修改的数据
     */
    public List<UpdateModel<T, M, P>> getUpdateList() {
        if (!this.converted) {
            this.converted = true;
            if (ObjectUtil.isEmpty(this.params)) {
                this.params = new JSONObject();
            }
            if (CollectionUtil.isEmpty(this.updateList)) {
                this.updateList = new ArrayList<>();
                this.updateList.add(BeanUtil.copyProperties(this, this.getClass(), "convertedBean", "params", "bean", "converted"));
            } else {
                this.updateList.forEach(e -> {
                    // 如果里面的数据没有值就填充默认值
                    JSONObject p = e.getParams();
                    this.params.forEach((k, v) -> {
                        if (!p.containsKey(k)) {
                            p.put(k, v);
                        }
                    });
                    if (ObjectUtil.isNotEmpty(this.data)) {
                        // 先复制最外层的 data 作为默认值
                        M copyData = (M) BeanUtil.copyProperties(this.data, this.data.getClass(), "convertedBean", "params", "bean", "converted");
                        // 然后把内层的数据覆盖过去这个外层的默认值里面就会被替换
                        BeanUtil.copyProperties(e.getData(), copyData, new CopyOptions().setIgnoreNullValue(true));
                        e.setData(copyData);
                    }
                });
            }
        }
        return this.updateList;
    }
}
