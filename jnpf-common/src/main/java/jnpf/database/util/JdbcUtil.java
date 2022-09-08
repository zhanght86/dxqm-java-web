package jnpf.database.util;

import com.mysql.cj.CacheAdapter;
import jnpf.database.enums.DbSttEnum;
import jnpf.database.exception.DataException;
import jnpf.database.model.DbLinkEntity;
import jnpf.database.model.dto.DbConnDTO;
import jnpf.database.model.DataSourceUtil;
import jnpf.database.source.DbBase;
import jnpf.database.source.impl.DbDm;
import jnpf.database.source.impl.DbOracle;
import jnpf.database.sql.DbSqlBase;
import jnpf.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;


/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:49
 */
@Slf4j
public class JdbcUtil {

    /**
     * 批量执行sql语句(适合增、删、改)
     * 输入的语句需要全小写，除了数据
     */
    public static int custom(Connection conn, String sql) throws DataException {
        try {
            //关闭事务自动提交（默认true，即自动提交事务）
            DbBase dbBase = DbTypeUtil.getDb(conn);
            conn.setAutoCommit(false);
            for (String sqlOne : sql.split(";")){
                //oracle特殊方法
                if(DbOracle.class == dbBase.getClass()){
                    sqlOne = dbBase.getSqlBase().jdbcCreUpSql(sqlOne);
                }
                PreparedStatement preparedStatement = conn.prepareStatement(sqlOne);
                preparedStatement.executeUpdate();
            }
            //批量提交事务
            conn.commit();
            return 1;
        //捕捉回滚操作
        }catch (SQLSyntaxErrorException s){
            throw DataException.tableExists(s.getMessage(),conn);
        }catch (Exception e) {
            throw DataException.rollbackDataException(e.getMessage(),conn);
        }
    }


    /**
     * 自定义sql语句(查)
     */
    public static ResultSet query(Connection conn, String sql) throws SQLException{
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        return preparedStatement.executeQuery();
    }

    /**====================================================================**/

    /**
     * 连接Connection
     */
    public static Connection getConn(DbLinkEntity link) throws DataException {
        String driverClass = DbTypeUtil.getDb(link).getDriver();
        String url = DbTypeUtil.getUrl(link);
        return connCommon(link.getUserName(),link.getPassword(),url,driverClass,"204");
    }
    /**
     * 多租户使用dbName可以指定，
     * 没有使用多租户时，用Null传值。
     */
    public static Connection getConn(DataSourceUtil dataSourceUtil,String dbName) throws DataException{
        String url = DbTypeUtil.getUrl(dataSourceUtil,dbName);
        String driverClass = DbTypeUtil.getDb(dataSourceUtil).getDriver();
        try {
            return connCommon(
                    dataSourceUtil.getUserName(),
                    dataSourceUtil.getPassword(),
                    url,
                    driverClass,"254");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Connection getConn(String userName, String password, String url) throws DataException{
        String driverClass = DbTypeUtil.getDb(url).getDriver();
        return connCommon(userName,password,url,driverClass,"254");
    }

    public static Connection getConn(DbConnDTO dbConnDTO) throws DataException {
        return connCommon(dbConnDTO.getUserName(),dbConnDTO.getPassword(),dbConnDTO.getUrl(),dbConnDTO.getDbDriver(),"301");
    }


    /**===========================getConn组成方法=========================================**/

    private static Connection connCommon(String userName, String password, String url, String driverClass,String warning) throws DataException{
        final Connection[] conn = {null};
        Callable<String> task = getTask(userName,password,url,driverClass,conn);
        futureGo(task,warning);
        return conn[0];
    }


    private static void futureGo(Callable<String> task,String warning) throws DataException{
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> future = executorService.submit(task);
        try {
            //设置超时时间
            String rst = future.get(3L, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw DataException.errorLink(warning,"连接超时");
        } catch (Exception e) {
            log.error(e.getMessage());
            throw DataException.errorLink(warning,"连接错误");
        } finally {
            executorService.shutdown();
        }
    }


    private static Callable<String> getTask(String userName, String password, String url, String driverClass, Connection[] conn)throws DataException{
        Callable<String> task = () -> {
            //执行耗时代码
            try {
                Class.forName(driverClass);
                conn[0] = DriverManager.getConnection(url, userName, password);
            } catch (Exception e) {
                throw new DataException(e.getMessage());
            }
            return "jdbc连接成功";
        };
        return task;
    }

    /**=======================================================================**/

    //----------------------jdbc转成对象------------------

    /**
     * jdbc 多条数据查询转成list
     *
     * @param rs result 查询的结果
     * @return
     */
    public static List<Map<String, Object>> convertList(ResultSet rs) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            while (rs.next()) {
                Map<String, Object> rowData = new HashMap<>(16);
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i).toLowerCase(), rs.getString(i));
                }
                list.add(rowData);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return list;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                rs = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }



    //----------------------jdbc转成对象String------------------

    /**
     * jdbc 多条数据查询转成list
     *
     * @param rs result 查询的结果
     * @return
     */
    public static List<Map<String, Object>> convertListString(ResultSet rs) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            while (rs.next()) {
                Map<String, Object> rowData = new HashMap<>(16);
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i).toLowerCase(), rs.getString(i));
                }
                list.add(rowData);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return list;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                rs = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * jdbc 单条数据查询转成map
     *
     * @param rs result 查询的结果
     * @return
     */
    public static Map<String, Object> convertMapString(ResultSet rs) {
        Map<String, Object> map = new TreeMap<>();
        try {
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    map.put(md.getColumnName(i), rs.getString(i));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return map;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                rs = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return map;
        }
    }

    /**
     * jdbc 多条数据查询转成list
     *
     * @param rs result 查询的结果，datetime转为时间戳
     * @return
     */
    public static List<Map<String, Object>> convertList2(ResultSet rs) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            while (rs.next()) {
                Map<String, Object> rowData = new HashMap<>(16);
                for (int i = 1; i <= columnCount; i++) {
                    if (rs.getObject(i) != null) {
                        if (md.getColumnType(i) != 93) {
                            rowData.put(md.getColumnLabel(i), String.valueOf(rs.getObject(i)));
                        } else {
                            rowData.put(md.getColumnLabel(i), DateUtil.stringToDate(String.valueOf(rs.getObject(i))).getTime());
                        }
                    }
                }
                list.add(rowData);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return list;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                rs = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;

    }

}
