package io.github.taybct.tool.core.ds.sync;

/**
 * 目标数据库类型
 */
public interface DriverType {
    /**
     * mysql 5.x 版本（旧版驱动）
     */
    String mysql = "com.mysql.jdbc.Driver";
    /**
     * mysql 8.x 版本（新版驱动）
     */
    String mysqlCJ = "com.mysql.cj.jdbc.Driver";
    /**
     * oracle 6.x 版本（旧版驱动）
     */
    String oracle6andBefore = "oracle.jdbc.driver.OracleDriver";
    /**
     * oracle 7.x 版本（新版驱动）
     */
    String oracle7AndThen = "oracle.jdbc.OracleDriver";
    /**
     * postgresql
     */
    String postgresSQL = "org.postgresql.Driver";

}
