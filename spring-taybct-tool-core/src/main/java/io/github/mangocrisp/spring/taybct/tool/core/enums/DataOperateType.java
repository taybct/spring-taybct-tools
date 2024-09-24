package io.github.mangocrisp.spring.taybct.tool.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据库操作类型
 *
 * @author XiJieYin <br> 2023/1/29 16:12
 */
@Getter
@AllArgsConstructor
public enum DataOperateType {
    /**
     * 其它
     */
    OTHER(-1),
    /**
     * 查询
     */
    QUERY(0),
    /**
     * 新增
     */
    INSERT(1),

    /**
     * 修改
     */
    UPDATE(2),

    /**
     * 删除
     */
    DELETE(3),

    /**
     * 清空数据
     */
    CLEAN(4);

    private final int code;
}
