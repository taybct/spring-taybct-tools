package io.github.mangocrisp.spring.taybct.tool.core.poi.easypoi.service;

import io.github.mangocrisp.spring.taybct.tool.core.poi.easypoi.support.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;

/**
 * <pre>
 * 通用Excel读取和转换类
 * </pre>
 *
 * @author XiJieYin
 * @since 2024/10/9 16:46
 */
public interface IExcelService {

    /**
     * 读取Excel 需要传入的数据 sheetName[表名]:默认 sheet1,这个参数选填
     * infoRows[信息行]：{0:{0:xm,1:xb},1:{0:xx,1xx}},就是第一层是行数据，然后里面是列的数和对应的字段名,这个参数选填
     * titleRows[标题行]:默认0
     * headerRows[表头行数]:默认1
     * modelType[模型的类名]:默认空，就直接返回Map
     *
     * @param params 参数
     * @param file   文件
     * @return 读取到文件的内容
     * @throws Exception 报错
     */
    ExcelReadData readExcel(ExcelParamsDTO params, MultipartFile file) throws Exception;

    /**
     * 可以将 readExcel 方法读取到的数据转换成指定类型的实体类List
     *
     * @param excelData   readExcel读取到的excel数据
     * @param params      其实参数后面做扩展用
     * @param clazz       需要转换的实体类
     * @param excelImpDel 处理方法
     * @return 是否导入成功
     * @throws Exception 报错
     */
    <T> boolean batchImportExcel(ExcelReadData excelData, ExcelParamsDTO params, Class<T> clazz, ExcelImpDel excelImpDel) throws Exception;

    /**
     * 导出Excel
     *
     * @param template    模板，json 包含fileName 和 fields
     * @param request     请求
     * @param response    响应
     * @param excelExpDel 处理方法
     */
    <P extends Serializable> void exportExcel(ExportTemplate<P> template, HttpServletRequest request, HttpServletResponse response, ExcelExpDel excelExpDel) throws IOException;

    /**
     * 导出Excel
     *
     * @param template       模板，格式： {"fileName":"文件名","fields":{"key1":"value1","key2":"value2"},"dictFields":{"key1":{"type":"","default":""}}}
     * @param exportTemplate excel模板的路径
     * @param request        请求
     * @param response       响应
     */
    <P extends Serializable> void exportExcelByTemplate(ExportTemplate<P> template, String exportTemplate, HttpServletRequest request, HttpServletResponse response) throws IOException;
}
