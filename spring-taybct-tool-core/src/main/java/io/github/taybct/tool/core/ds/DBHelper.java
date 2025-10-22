package io.github.taybct.tool.core.ds;

import com.alibaba.druid.pool.DruidPooledPreparedStatement;
import com.alibaba.druid.proxy.jdbc.PreparedStatementProxyImpl;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.annotation.DbType;
import io.github.taybct.tool.core.exception.def.BaseException;
import io.github.taybct.tool.core.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.lang.Nullable;

import java.sql.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 数据库连接工具<br>
 * Statement 和 PreparedStatement之间的关系和区别. <br>
 * 关系：PreparedStatement继承自Statement,都是接口<br>
 * 区别：PreparedStatement可以使用占位符，是预编译的，批处理比Statement效率高
 *
 * @author xijieyin <br> 2022/8/5 22:18
 * @since 1.0.0
 */
@Slf4j
public class DBHelper {

    private final String dbms;   // 数据库类型
    private String dbmsDriver; // 数据库驱动
    private final String host;   // 主机
    private String port;   // 端口
    private final String db;   // 数据库名
    private final String user;   // 用户名
    private final String password; // 密码

    private Class beanClass = null;
    private Connection conn = null;

    /**
     * 获取数据库连接
     *
     * @param driver 驱动名
     * @param url    连接字符串
     * @param user   用户
     * @param pass   密码
     */
    public static Connection conn(String driver, String url, String user, String pass) {
        try {
            // 2.获得数据库链接
            Class.forName(driver);
            return DriverManager.getConnection(url, user, pass);
        } catch (Exception e) {
            log.error(String.format("数据库链接失败：驱动：%s，数据库链接：%s，用户：%s，密码：***，错误信息：%s", driver, url, user, e.getMessage()));
        }
        return null;
    }

    /**
     * 根据数据库类型设置驱动程序
     *
     * @param dbms 数据库类型，如mysql、postgresql等
     */
    private void fillDbmsDriver(String dbms) {
        if (dbms.equalsIgnoreCase("postgresql"))
            dbmsDriver = "org.postgresql.Driver";
        else if (dbms.equalsIgnoreCase("mysql"))
            dbmsDriver = "org.gjt.mm.mysql.Driver";
    }

    /**
     * 根据数据库类型设置端口
     *
     * @param dbms 数据库类型
     */
    private void fillDbmsPort(String dbms) {
        if (dbms.equalsIgnoreCase("postgresql"))
            port = "5432";
        else if (dbms.equalsIgnoreCase("mysql"))
            port = "3306";
    }


    /**
     * 设置数据连接时需要的参数
     *
     * @param dbms     数据库类型
     * @param host     主机
     * @param db       数据库名
     * @param user     用户名
     * @param password 密码
     */
    public DBHelper(String dbms, String host, String db, String user,
                    String password) {
        this.dbms = dbms;
        fillDbmsDriver(dbms);
        this.host = host;
        fillDbmsPort(dbms);
        this.db = db;
        this.user = user;
        this.password = password;
    }


    /**
     * 设置数据连接时需要的参数
     *
     * @param dbms     数据库类型
     * @param host     主机
     * @param port     端口
     * @param db       数据库名
     * @param user     用户
     * @param password 密码
     */
    public DBHelper(String dbms, String host, String port, String db,
                    String user, String password) {
        this.dbms = dbms;
        fillDbmsDriver(dbms);
        this.host = host;
        this.port = port;
        this.db = db;
        this.user = user;
        this.password = password;
    }


    /**
     * 加载驱动
     */
    public void connect() {
        if (conn != null) return;

        String strConn = String.format("jdbc:%s://%s:%s/%s", dbms, host, port, db);
        DbUtils.loadDriver(dbmsDriver);
        try {
            conn = DriverManager.getConnection(strConn, user, password);
        } catch (SQLException e) {
            log.error("Database connection failed!");
            log.error(e.getMessage());
        }
    }


    /**
     * 关闭Connection
     */
    public void close() {
        try {
            DbUtils.close(conn);
            conn = null;
        } catch (SQLException e) {
            log.error("Database close failed!");
            log.error(e.getMessage());
        }
    }


    /**
     * 根据无参sql语句进行查询，并返回一个对象，用于单条记录的查询
     *
     * @param sql 查询语句
     * @return Object
     */
    public Object query(String sql) {
        if (conn == null || beanClass == null)
            return null;

        QueryRunner run = new QueryRunner();
        ResultSetHandler h = new BeanHandler(beanClass);
        Object result = null;

        try {
            result = run.query(conn, sql, h);
        } catch (SQLException e) {
            log.info("SQLException in [" + sql + "]");
            log.error(e.getMessage());
        }

        return result;
    }


    /**
     * 根据有参sql语句进行查询，并返回一个对象，用于单条记录的查询
     *
     * @param sql  查询语句
     * @param args 查询参数
     * @return Object
     */
    public Object query(String sql, Object... args) {
        if (conn == null || beanClass == null)
            return null;

        QueryRunner run = new QueryRunner();
        ResultSetHandler h = new BeanHandler(beanClass);
        Object result = null;

        try {
            result = run.query(conn, sql, args, h);
        } catch (SQLException e) {
            log.info("SQLException in [" + sql + "]");
            log.error(e.getMessage());
        }

        return result;
    }


    /**
     * 根据无参sql语句进行查询，并返回一个数据集，用于多条记录的查询
     *
     * @param sql 查询语句
     * @return ArrayList
     */
    public ArrayList queryRecords(String sql) {
        if (conn == null || beanClass == null)
            return null;

        QueryRunner run = new QueryRunner();
        ResultSetHandler h = new BeanListHandler(beanClass);
        ArrayList result = null;

        try {
            result = (ArrayList) run.query(conn, sql, h);
            // obtain the quaried records
        } catch (SQLException e) {
            log.info("SQLException in [" + sql + "]");
            log.error(e.getMessage());
        }

        return result;
    }


    /**
     * 根据有参sql语句进行查询，并返回一个数据集，用于多条记录的查询
     *
     * @param sql  查询语句
     * @param args 参数
     * @return ArrayList
     */
    public ArrayList queryRecords(String sql, Object... args) {
        if (conn == null || beanClass == null)
            return null;

        QueryRunner run = new QueryRunner();
        ResultSetHandler h = new BeanListHandler(beanClass);
        ArrayList result = null;

        try {
            result = (ArrayList) run.query(conn, sql, args, h);

            log.debug("{}", result.size());
            // obtain the quaried records
        } catch (SQLException e) {
            log.info("SQLException in [" + sql + "]");
            log.error(e.getMessage());
        }

        return result;
    }


    /**
     * 根据无参sql语句进行数据更新，并返回更新后影响的记录数
     *
     * @param sql 语句
     * @return int
     */
    public int update(String sql) {
        if (conn == null)
            return 0;

        QueryRunner run = new QueryRunner();
        int result = 0;

        try {
            result = run.update(conn, sql);
        } catch (SQLException e) {
            log.info("SQLException in [" + sql + "]");
            log.error(e.getMessage());
        }

        return result;
    }


    /**
     * 根据有参sql语句进行数据更新，并返回更新后影响的记录数
     *
     * @param sql  更新语句
     * @param args 参数
     * @return int
     */
    public int update(String sql, Object... args) {
        if (conn == null)
            return -1;

        QueryRunner run = new QueryRunner();
        int result = 0;

        try {
            result = run.update(conn, sql, args);
        } catch (SQLException e) {
            log.info("SQLException in [" + sql + "]");
            log.error(e.getMessage());
        }

        return result;
    }


    /**
     * 设置存放查询结果的Bean类，每个Bean类对象对应一条查询的结果记录
     *
     * @param beanClass，如 User.class
     */
    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }


    /**
     * 设置使用外部的Connection对象，如来自数据库连接池的连接
     *
     * @param conn Connection对象
     */
    public void setConn(Connection conn) {
        this.conn = conn;
    }


    @Override
    protected void finalize() throws Throwable {
        // TODO Auto-generated method stub
        close();
        super.finalize();
    }

    /**
     * 测试
     */
    public void test() {
        // TODO Auto-generated method stub
        int n = 3;
        DBHelper dh = new DBHelper("mysql", "localhost", "test", "root", "123456");

        dh.connect();
//        dh.setBeanClass(User.class);

        //u是从数据库查询出来的记录对象
//        User u = (User) dh.query("SELECT id,name,age,address FROM t_user WHERE id=9");

        //u2是从数据库查询出来的记录对象
//        User u2 = (User) dh.query("SELECT id,name,age,address FROM t_user WHERE id=?", n);

        // users和user2存放着从数据库查询出来的记录对象集合
        List users = dh.queryRecords("SELECT id,name,age,address FROM t_user");
        List users2 = dh.queryRecords("SELECT id,name,age,address FROM t_user WHERE id>?", n);


        //修改表记录
        dh.update("UPDATE t_user SET name='abc' WHERE id=9");

        //根据提供的参数修改表记录
        dh.update("UPDATE t_user SET name='abc' WHERE id=?", n);

        dh.close();
    }

    /**
     * 获取数据源连接的数据库类型
     *
     * @param connection 数据源连接
     * @return 类型
     */
    public static DbType getDbType(Connection connection) {
        return getDbType(connection, null);
    }

    /**
     * 获取数据源连接的数据库类型
     *
     * @param connection    数据源连接
     * @param defaultDbType 类型库类型
     * @return 类型
     */
    public static DbType getDbType(Connection connection, DbType defaultDbType) {
        try {
            return DbType.getDbType(connection.getMetaData().getDatabaseProductName().toLowerCase());
        } catch (SQLException e) {
            log.error("获取数据库类型失败！", e);
        }
        return defaultDbType;
    }

    /**
     * 查询
     *
     * @param con        jdbc 连接
     * @param sql        sql 语句
     * @param parameters 参数列表
     * @param argTypes   参数类型
     * @param clazz      返回结果的类型
     * @param <T>        返回的对象类型
     * @return 返回对象集合
     */
    public static <T> List<T> execSelect(Connection con, String sql, Object[] parameters, int[] argTypes, Class<T> clazz) {
        return execSelect(con, sql, parameters, argTypes, resultSet -> resultSetConvert(resultSet, clazz));
    }

    /**
     * 查询
     *
     * @param con             jdbc 连接
     * @param sql             sql 语句
     * @param parameters      参数列表
     * @param argTypes        参数类型
     * @param underlineToHump 是否需要下划线转驼峰的 key
     * @return 返回结果集
     */
    public static List<Map<String, Object>> execSelectMap(Connection con, String sql, Object[] parameters, int[] argTypes, boolean underlineToHump) {
        return execSelect(con, sql, parameters, argTypes, resultSet -> resultSetConvert(resultSet, underlineToHump));
    }


    /**
     * 查询
     *
     * @param con                jdbc 连接
     * @param sql                sql 语句
     * @param pss                自己设置 PreparedStatement
     * @param resultSetConvertor 结果转换器
     * @return 返回结果集
     */
    public static <T> T execSelect(Connection con, String sql, @Nullable PreparedStatementSetter pss, Function<ResultSet, T> resultSetConvertor) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement(sql);
            if (pss != null) {
                pss.setValues(ps);
            }
            printSql(ps);
            rs = ps.executeQuery();
            return resultSetConvertor.apply(rs);
        } catch (SQLException e) {
            log.error("查询失败", e);
            return null;
        } finally {
            release(ps, con, rs);
        }
    }

    /**
     * 打印 sql
     *
     * @param ps PreparedStatement 对象
     */
    public static void printSql(PreparedStatement ps) {
        if (ps instanceof DruidPooledPreparedStatement druidPs) {
            PreparedStatement rawPreparedStatement = druidPs.getRawPreparedStatement();
            if (rawPreparedStatement instanceof PreparedStatementProxyImpl cstmt) {
                log.debug(": ==>  \r\n{}", cstmt.getRawObject().toString());
            }
        }
    }

    /**
     * 查询
     *
     * @param sql                sql 语句
     * @param parameters         参数列表
     * @param argTypes           参数类型
     * @param con                jdbc 连接
     * @param resultSetConvertor 结果转换器
     * @return 返回结果集
     */
    public static <T> T execSelect(Connection con
            , String sql
            , Object[] parameters
            , int[] argTypes
            , Function<ResultSet, T> resultSetConvertor) {
        return execSelect(con, sql, ps -> {
            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    try {
                        ps.setObject(i + 1, parameters[i], argTypes[i]);
                    } catch (SQLException e) {
                        log.error("设置参数失败！", e);
                    }
                }
            }
        }, resultSetConvertor);
    }

    /**
     * 增删改
     *
     * @param con 连接
     * @param sql 执行 sql 语句
     * @param pss 自己设置 PreparedStatement
     * @return 影响的行数
     */
    public static int execUpdate(Connection con, String sql, @Nullable PreparedStatementSetter pss) {
        int rows = -1;
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(sql);
            if (pss != null) {
                pss.setValues(ps);
            }

            printSql(ps);
            rows = ps.executeUpdate();
        } catch (SQLException e) {
            log.error("增删改失败", e);
        } finally {
            release(ps, con, null);
        }
        return rows;
    }

    /**
     * 增删改
     *
     * @param con        连接
     * @param sql        执行 sql 语句
     * @param parameters 参数列表
     * @param argTypes   参数类型列表
     * @return 影响的行数
     */
    public static int execUpdate(Connection con, String sql, Object[] parameters, int[] argTypes) {
        return execUpdate(con, sql, ps -> {
            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    try {
                        ps.setObject(i + 1, parameters[i], argTypes[i]);
                    } catch (SQLException e) {
                        log.error("设置参数失败！", e);
                    }
                }
            }
        });
    }

    /**
     * 批量（多条 sql）增删改
     *
     * @param con 连接
     * @param pss 提供批量操作的 PreparedStatement
     * @return 景程的行数，如果批量里面有一个不行就都不行
     */
    public static int[] execUpdate(Connection con, Supplier<PreparedStatement[]> pss) {
        return execUpdate(con, pss, pse -> release(pse, null, null));
    }

    /**
     * 批量（多条 sql）增删改
     *
     * @param con 连接
     * @param pss 提供批量操作的 PreparedStatement
     * @param pse 每个 PreparedStatement 操作完之后的操作外面是否关闭
     * @return 景程的行数，如果批量里面有一个不行就都不行
     */
    public static int[] execUpdate(Connection con, Supplier<PreparedStatement[]> pss, Consumer<PreparedStatement> pse) {
        PreparedStatement[] ps = pss.get();
        int[] rows = new int[ps.length];
        try {
            con.setAutoCommit(false);
            for (int i = 0; i < ps.length; i++) {

                printSql(ps[i]);
                rows[i] = ps[i].executeUpdate();
                if (pse != null) {
                    pse.accept(ps[i]);
                }
            }
        } catch (SQLException e) {
            Arrays.fill(rows, -1);
            log.error("多语句增删改失败", e);
        } finally {
            if (Arrays.stream(rows).allMatch(row -> row > 0)) {
                try {
                    con.commit();
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                }
            } else {
                try {
                    con.rollback();
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                }
            }
            release(null, con, null);
        }
        return rows;
    }

    /**
     * 批量（多条 sql）增删改
     *
     * @param con        连接
     * @param sql        执行的 sql 语句
     * @param parameters 参数列表
     * @param argTypes   参数类型列表
     * @return 景程的行数，如果批量里面有一个不行就都不行
     */
    public static int[] execUpdate(Connection con, List<String> sql, List<Object[]> parameters, List<int[]> argTypes) {
        PreparedStatement[] preparedStatements = new PreparedStatement[sql.size()];
        try {
            for (int i = 0; i < sql.size(); i++) {
                PreparedStatement ps = con.prepareStatement(sql.get(i));
                for (int j = 0; j < parameters.get(i).length; j++) {
                    ps.setObject(j + 1, parameters.get(i)[j], argTypes.get(i)[j]);
                }
                preparedStatements[i] = ps;
            }
        } catch (SQLException e) {
            log.error("设置参数失败！", e);
            throw new BaseException("设置参数失败！", e);
        }
        return execUpdate(con, () -> preparedStatements);
    }

    /**
     * 获取到返回 java 类型对象结果集
     *
     * @param resultSet db 返回结果集
     * @param clazz     需要转换的类型
     * @param <T>       类型
     * @return 返回结果
     */
    public static <T> List<T> resultSetConvert(ResultSet resultSet, Class<T> clazz) {
        List<Map<String, Object>> maps = resultSetConvert(resultSet, true);
        return JSONArray.parseArray(JSONArray.toJSONString(maps)).toJavaList(clazz);
    }

    /**
     * 获取到 {@code Map<String, Object>} 类型的对象
     *
     * @param resultSet db 返回的结果集
     * @return 返回结果
     */
    public static List<Map<String, Object>> resultSetConvert(ResultSet resultSet) {
        return resultSetConvert(resultSet, false);
    }

    /**
     * 获取到 {@code Map<String, Object>} 类型的对象
     *
     * @param resultSet       db 返回的结果集
     * @param underlineToHump 是否需要下划线转驼峰的 key
     * @return 返回结果
     */
    public static List<Map<String, Object>> resultSetConvert(ResultSet resultSet, boolean underlineToHump) {
        // 获取ResultSet对象的列的数量、类型和属性。
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            ResultSetMetaData md = resultSet.getMetaData();
            int columnCount = md.getColumnCount();
            // 将ResultSet对象的列名和值存到map中，再将map转换为json字符串，最后将json字符串转换为实体类对象
            while (resultSet.next()) {
                Map<String, Object> rowData = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(underlineToHump ? StringUtil.underlineToHump(md.getColumnLabel(i)) : md.getColumnLabel(i), resultSet.getObject(i));
                }
                result.add(rowData);
            }
            return result;
        } catch (SQLException e) {
            log.error("转换失败！", e);
            throw new BaseException("转换失败！", e);
        } finally {
            release(null, null, resultSet);
        }
    }

    /**
     * 关闭各种 jdbc 的资源
     *
     * @param ps  PreparedStatement
     * @param con Connection
     * @param rs  ResultSet
     */
    public static void release(PreparedStatement ps, Connection con, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

}
