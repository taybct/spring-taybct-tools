package io.github.mangocrisp.spring.taybct.tool.core.ds;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import io.github.mangocrisp.spring.taybct.tool.core.exception.def.BaseException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * JdbcTemplate 工具类
 *
 * @author XiJieYin <br> 2023/1/30 21:42
 */
public class JdbcTemplateUtil {

    final static ConcurrentMap<String, JdbcTemplate> jdbcTemplatePool = new ConcurrentHashMap<>();

    /**
     * 获取 jdbcTemplate，这里可以指定不同的数据源来获取 JdbcTemplate
     *
     * @param jdbcTemplate jdbcTemplate
     * @param dataSource   数据源（这个是在使用的时候指定的数据源，如果不指定，或者指定的是空的就无效，就会使用默认的数据源）
     * @return jdbcTemplate
     */
    public static JdbcTemplate getJdbcTemplate(JdbcTemplate jdbcTemplate, String dataSource) {
        if (StrUtil.isBlank(dataSource)) {
            return jdbcTemplate;
        }
        JdbcTemplate template = jdbcTemplatePool.get(dataSource);
        if (template != null) {
            return template;
        }
        template = new JdbcTemplate(Optional.ofNullable(jdbcTemplate.getDataSource())
                .map(d -> (DynamicRoutingDataSource) d)
                .orElseThrow(() -> new NullPointerException("数据源配置异常！"))
                .getDataSource(dataSource));
        jdbcTemplatePool.put(dataSource, template);
        return template;
    }

    /**
     * 获取 jdbcTemplate 里面的数据湖
     *
     * @param jdbcTemplate jdbcTemplate
     * @param dataSource   数据源名称
     * @return 数据湖
     */
    public static DataSource getDataSource(JdbcTemplate jdbcTemplate, String dataSource) {
        return Optional.ofNullable(jdbcTemplate.getDataSource())
                .map(d -> (DynamicRoutingDataSource) d)
                .orElseThrow(() -> new NullPointerException("数据源配置异常！"))
                .getDataSource(dataSource);
    }

    /**
     * 获取数据库连接
     *
     * @param jdbcTemplate jdbcTemplate
     * @param dataSource   数据源名称
     * @return 数据库链接
     */
    public static Connection getConnection(JdbcTemplate jdbcTemplate, String dataSource) {
        try {
            return getDataSource(jdbcTemplate, dataSource).getConnection();
        } catch (SQLException e) {
            throw new BaseException("获取数据库链接失败！");
        }
    }

}
