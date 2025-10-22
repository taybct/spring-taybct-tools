package io.github.taybct.tool.core.ds;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * PreparedStatement 参数设置
 *
 * @author XiJieYin <br> 2024/4/18 14:11
 */
@FunctionalInterface
public interface PreparedStatementSetter {

    /**
     * Set parameter values on the given PreparedStatement.
     *
     * @param ps the PreparedStatement to invoke setter methods on
     * @throws SQLException if an SQLException is encountered
     *                      (i.e. there is no need to catch SQLException)
     */
    void setValues(PreparedStatement ps) throws SQLException;
}
