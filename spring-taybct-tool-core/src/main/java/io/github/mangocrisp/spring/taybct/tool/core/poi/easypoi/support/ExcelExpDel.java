package io.github.mangocrisp.spring.taybct.tool.core.poi.easypoi.support;

import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import io.github.mangocrisp.spring.taybct.tool.core.mybatis.support.SqlPageParams;
import com.alibaba.fastjson2.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Excel 导出处理类
 */
public interface ExcelExpDel {

    /**
     * 自定义导出的模板
     *
     * @param entities 方便导出一些图片这样的
     */
    default void customExpEntity(List<ExcelExportEntity> entities) {
    }

    /**
     * 获取数据行
     *
     * @param fields        需要查询的字段
     * @param params        查询条件
     * @param sqlPageParams 分页参数
     * @return 获取数据行
     */
    List<Map<String, Object>> getList(List<String> fields, JSONObject params, SqlPageParams sqlPageParams);
}
