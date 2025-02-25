package io.github.mangocrisp.spring.taybct.tool.core.poi.easypoi.service.impl;

import cn.afterturn.easypoi.entity.PoiBaseConstants;
import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import cn.afterturn.easypoi.handler.inter.IWriter;
import cn.afterturn.easypoi.util.PoiMergeCellUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import io.github.mangocrisp.spring.taybct.tool.core.exception.def.BaseException;
import io.github.mangocrisp.spring.taybct.tool.core.mybatis.support.SqlPageParams;
import io.github.mangocrisp.spring.taybct.tool.core.poi.easypoi.constants.EasyPOIConstant;
import io.github.mangocrisp.spring.taybct.tool.core.poi.easypoi.service.IExcelService;
import io.github.mangocrisp.spring.taybct.tool.core.poi.easypoi.support.*;
import io.github.mangocrisp.spring.taybct.tool.core.poi.easypoi.util.EasyPOIUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.BeanUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * <pre>
 * EasyPoi 业务工具类
 * </pre>
 *
 * @author XiJieYin
 * @since 2024/10/9 16:13
 */
public class ExcelServiceImpl implements IExcelService {

    /**
     * 拿到基础包
     */
    final String importModelBase;

    public ExcelServiceImpl() {
        this.importModelBase = EasyPOIConstant.IMPORT_MODEL_BASE_PACKAGE;
    }

    public ExcelServiceImpl(String importModelBase) {
        this.importModelBase = importModelBase;
    }

    @Override
    public ExcelReadData readExcel(ExcelParamsDTO params, MultipartFile file) throws Exception {
        if (file == null) {
            return null;
        }
        ExcelReadData result = new ExcelReadData();
        //读取的表
        String sheetName = params.getSheetName();
        //传入的信息行，就是表头需要录入的一些垃圾数据
        String infoRows = params.getInfoRows();
        //拿到的一行的单元格
        JSONObject infoRowCells = new JSONObject();
        if (infoRows != null) {
            JSONObject rows = JSONObject.parseObject(infoRows);
            for (String rowKey : rows.keySet()) {
                //拿到要读取的行
                int rownum = Integer.parseInt(rowKey);
                //这是数据指定每一个数据是在第几列 {0:xm,1:xb}这样的
                JSONObject infoReadCells = rows.getJSONObject(rowKey);
                for (String cellKey : infoReadCells.keySet()) {
                    //拿到要读取的列
                    int cellnum = Integer.parseInt(cellKey);
                    //字段名
                    String field = infoReadCells.getString(cellKey);
                    Object obj = EasyPOIUtil.readExcelData(file, sheetName, rownum, cellnum);
                    infoRowCells.put(field, obj);
                }
            }
        }
        result.setInfoRowCells(infoRowCells);
        //数据行
        JSONArray dataRows = new JSONArray();
        //是否读取多行数据，默认读取多选数据
        Boolean multiRows = params.getMultiRows();
        if (!multiRows) {
            //默认是循环读取行，如果不需要可以设置为false然后只拿表头信息
            result.setDataRows(dataRows);
            return result;
        }

        //是否合并表头数据，即，是否将表头的数据插入到每一行，这个在如果前端是json数据时候有用，如果是实体类，要看情况来
        Boolean mergeInfoRows = params.getMergeInfoRows();

        //标题行
        Integer titleRows = params.getTitleRows();
        //头部行
        Integer headerRows = params.getHeaderRows();
        ImportParams importParams = new ImportParams();
        importParams.setTitleRows(titleRows);
        importParams.setHeadRows(headerRows);
        //模型类型
        String modelType = params.getModelType();
        List<Object> list;
        if (modelType != null) {
            Class<?> clazz = Class.forName(this.importModelBase + "." + modelType);

            list = ExcelImportUtil.importExcel(file.getInputStream(), clazz, importParams);
        } else {
            list = ExcelImportUtil.importExcel(file.getInputStream(), Map.class, importParams);
        }
        for (Object obj : list) {
            JSONObject unknowable = JSONObject.parseObject(JSONObject.toJSONString(obj, JSONWriter.Feature.WriteMapNullValue));
            for (Object o : unknowable.values()) {
                if (o != null) {
                    if (mergeInfoRows) {
                        //把表头初始值设置进去
                        for (String key : infoRowCells.keySet()) {
                            unknowable.put(key, infoRowCells.get(key));
                        }
                    }
                    dataRows.add(unknowable);
                    break;
                }
            }
        }
        result.setDataRows(dataRows);
        return result;
    }

    @Override
    public <T> boolean batchImportExcel(ExcelReadData excelData, ExcelParamsDTO params, Class<T> clazz, ExcelImpDel excelImpDel) throws Exception {
        //最后读取出来的结果，可能是一条数据，也可能是多条数据
        List<T> list = new ArrayList<>();
        //所有的表头信息
        JSONObject infoRowCells = JSONObject.parseObject(JSONObject.toJSONString(excelData.getInfoRowCells(), JSONWriter.Feature.WriteMapNullValue));
        //所有的数据表
        JSONArray dataRows = JSONArray.parseArray(JSONArray.toJSONString(excelData.getDataRows(), JSONWriter.Feature.WriteMapNullValue));
        //用来获取实体类有哪些字段
        Constructor<T> declaredConstructor = clazz.getDeclaredConstructor();
        T temp = declaredConstructor.newInstance();
        JSONObject fields = JSONObject.parseObject(JSONObject.toJSONString(temp, JSONWriter.Feature.WriteMapNullValue));
        //默认读取多行数据
        Boolean multiRows = params.getMultiRows();
        if (!multiRows) {
            //默认是循环读取行，如果不需要可以设置为false然后只拿表头信息
            JSONObject voJsonObj = new JSONObject();
            for (String key : fields.keySet()) {
                //infoRowCells.get(...)是用来获取表头信息的数据
                Object infoRowCell = infoRowCells.get(key);
                //表头的信息只能是全局的
                if (infoRowCell != null) {
                    voJsonObj.put(key, infoRowCell);
                }
            }
            T vo = voJsonObj.toJavaObject(clazz);
            list.add(vo);
        } else {
            //如果是要读取多行
            for (Object rowObj : dataRows) {
                JSONObject row = JSONObject.parseObject(JSONObject.toJSONString(rowObj, JSONWriter.Feature.WriteMapNullValue));

                JSONObject voJsonObj = new JSONObject();
                for (String key : fields.keySet()) {
                    //infoRowCells.get(...)是用来获取表头信息的数据
                    Object infoRowCell = infoRowCells.get(key);
                    //表头的信息只能是全局的
                    if (infoRowCell != null) {
                        voJsonObj.put(key, infoRowCell);
                    }
                    //row.get(...)是用来获取数据行的数据
                    //而单行如果也有，肯定是单行的优先
                    Object field = row.get(key);
                    if (field != null) {
                        voJsonObj.put(key, field);
                    }
                }
                T vo = voJsonObj.toJavaObject(clazz);
                list.add(vo);
            }
        }
        return excelImpDel.del(list, params);
    }

    @Override
    public <P extends Serializable> void exportExcel(ExportTemplate<P> template
            , HttpServletRequest request
            , HttpServletResponse response
            , ExcelExpDel excelExpDel) throws IOException {
        //要导出的Excel字段实体
        List<ExcelExportEntity> entity = new ArrayList<>();
        //字段
        List<String> dbFields = new ArrayList<>();
        //模板字段数组
        List<ExportTemplateField> templateFields = template.getExportTemplateField();
        // 按字段排序
        templateFields.sort(Comparator.comparingInt(ExportTemplateField::getOrderNum));
        List<Integer> mergeColumns = new ArrayList<>();
        for (int i = 0; i < templateFields.size(); i++) {
            ExportTemplateField field = templateFields.get(i);
            if (StringUtil.isBlank(field.getKey())) {
                throw new BaseException("key[字段]不能为空！");
            }
            if (StringUtil.isBlank(field.getFormat()) || !field.getFormat().equals(PoiBaseConstants.IS_ADD_INDEX)) {
                // 如果是索引就不添加到数据库字段里面去
                // 数据库查询字段，这里替换一下一些特殊字符，防止 sql 注入
                String dbField = field.getKey().replaceAll(";", "")
                        .replaceAll("\\(", "")
                        .replaceAll("\\)", "");
                dbFields.add(dbField);
            }
            //添加Excel字段
            ExcelExportEntity exportEntity = new ExcelExportEntity(field.getName());
            // 行高
            exportEntity.setHeight(template.getHeight());
            // 设置其他属性
            BeanUtil.copyProperties(field, exportEntity, CopyOptions.create().ignoreNullValue());
            exportEntity.setWrap(field.isWrap());
            exportEntity.setColumnHidden(field.isColumnHidden());
            exportEntity.setStatistics(field.isStatistics());
            entity.add(exportEntity);
            if (field.isMergeSame()) {
                mergeColumns.add(i);
            }
        }
        excelExpDel.customExpEntity(entity);
        // 查询参数
        JSONObject params = template.getParamsJSONObject();
        // 分页参数
        SqlPageParams sqlPageParams = template.getSqlPageParams();
        // 查询结果
        List<Map<String, Object>> list = excelExpDel.getList(dbFields, params, sqlPageParams);
        if (list == null) {
            list = new ArrayList<>();
        }
        String fileName = template.getFileName();
        String excelFileName = fileName + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss", Locale.CHINA));
        //Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams(fileName, template.getSheetName()), entity, list);
        // 大数据导出
        ExportParams exportParams = new ExportParams(fileName, template.getSheetName(), template.getExcelType());
        // 设置字典翻译
        exportParams.setDictHandler(EasyPOIUtil.excelDictHandler);
        IWriter<Workbook> writer = ExcelExportUtil.exportBigExcel(exportParams, entity);
        writer.write(list);
        Workbook workbook = writer.get();
        if (CollectionUtil.isNotEmpty(mergeColumns)) {
            PoiMergeCellUtil.mergeCells(workbook.getSheetAt(0), template.getMergeSameStartRow(), ArrayUtil.toArray(mergeColumns, Integer.class));
        }
        EasyPOIUtil.userDefinedExport(excelFileName, response, workbook);
    }


    @Override
    public <P extends Serializable> void exportExcelByTemplate(ExportTemplate<P> template, String exportTemplate, HttpServletRequest request, HttpServletResponse response) throws IOException {
        TemplateExportParams params = new TemplateExportParams(exportTemplate);
        //所有的要导出的字段
        List<ExportTemplateField> fields = template.getExportTemplateField();
        //字典转换
        JSONObject converted = new JSONObject();
        for (ExportTemplateField field : fields) {
            converted.put(field.getKey(), field.getValue());
        }
        //文件名
        String fileName = template.getFileName();
        Workbook workbook = ExcelExportUtil.exportExcel(params, converted);
        String excelFileName = fileName + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss", Locale.CHINA));
        EasyPOIUtil.userDefinedExport(excelFileName, response, workbook);
    }
}
