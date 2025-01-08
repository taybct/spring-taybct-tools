package io.github.mangocrisp.spring.taybct.tool.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作状态
 *
 * @author xijieyin <br> 2022/8/5 18:32
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public final class OperateStatus {
    /**
     * 正常
     */
    public static final OperateStatus SUCCESS = new OperateStatus("SUCCESS", 1, "正常");
    /**
     * 失败
     */
    public static final OperateStatus FAILED = new OperateStatus("FAILED", 0, "失败");
    /**
     * 代码
     */
    private final String code;
    /**
     * 数字代码
     */
    private final int intCode;
    /**
     * 描述
     */
    private final String description;


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        OperateStatus that = (OperateStatus) obj;
        return this.getIntCode() == that.intCode;
    }

    @Override
    public int hashCode() {
        return this.getCode().hashCode();
    }
}
