package io.github.mangocrisp.spring.taybct.tool.core.poi.easyexcel.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.converters.longconverter.LongStringConverter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * EasyExcel 操作工具类
 */
public class EasyExcelUtil {

    @SneakyThrows
    public static void export(String fileName, HttpServletResponse response, Class<?> clazz, Supplier<Collection<?>> data) {
        configResponse(fileName, response);
        EasyExcel.write(response.getOutputStream(), clazz)
                // EasyExcel对象Long类型太长导致导出变成科学计数法解决方法，也可以在单独的字段加上如：@NumberFormat(value = “#”)
                .registerConverter(new LongStringConverter())
                .sheet("Sheet1").doWrite(data.get());
    }

    /**
     * 配置 response
     *
     * @param fileName 文件名
     * @param response response
     */
    public static void configResponse(String fileName, HttpServletResponse response) {
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        fileName = URLEncoder.encode((fileName + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss", Locale.CHINA)))
                , StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
    }

}
