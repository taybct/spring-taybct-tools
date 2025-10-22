package io.github.taybct.tool.core.enums;

import java.io.Serializable;

/**
 * 操作状态
 *
 * @param code        代码
 * @param intCode     数字代码
 * @param description 描述
 * @author xijieyin <br> 2022/8/5 18:32
 * @since 1.0.0
 */
public record OperateStatus(String code, int intCode, String description) implements Serializable {
    /**
     * 正常
     */
    public static final OperateStatus SUCCESS = new OperateStatus("SUCCESS", 1, "正常");
    /**
     * 失败
     */
    public static final OperateStatus FAILED = new OperateStatus("FAILED", 0, "失败");

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        OperateStatus that = (OperateStatus) obj;
        return this.intCode() == that.intCode;
    }

    @Override
    public int hashCode() {
        return this.code().hashCode();
    }
}
