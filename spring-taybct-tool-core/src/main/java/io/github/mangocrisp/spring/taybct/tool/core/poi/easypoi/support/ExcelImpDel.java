package io.github.mangocrisp.spring.taybct.tool.core.poi.easypoi.support;

import java.util.List;

/**
 * Excel导入处理类
 */
public interface ExcelImpDel {

    /**
     * Excel读取到结果
     *
     * @param list   转换后的实体类List
     * @param params 其它参数
     * @return 是否导入成功
     */
    <T> boolean del(List<T> list, ExcelParamsDTO params);

}
