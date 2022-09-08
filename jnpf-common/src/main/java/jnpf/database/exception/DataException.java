package jnpf.database.exception;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 10:10
 */
public class DataException extends Exception {
    public DataException(String message) {
        super(message);
    }

    public static DataException DbTypeCompare(String dbTypeSource) {
        return new DataException(dbTypeSource + "：数据类型编码不符合标准（请注意大小写）。" +
                "MySQL , SQLServer , Oracle , DM8 , KingbaseES , PostgreSQL");
    }

    public static DataException errorLink(String warningCode,String warning) {
        return new DataException(warningCode + " - " + warning + "：请检查 1、连接信息 2、网络通信 3、数据库服务启动状态。");
    }

    /**
     * mysql表重复
     * @param error
     * @return
     */
    public static DataException tableExists(String error,Connection rollbackConn){
        executeRollback(rollbackConn);
        //Mysql英文报错，临时解决方案
        error = error.replace("Table","表").replace("already exists","已经存在。");
        return new DataException(error);
    }

    public static DataException rollbackDataException(String message, Connection rollbackConn) {
        executeRollback(rollbackConn);
        return new DataException(message);
    }

    public static void executeRollback(Connection conn){
        try {
            conn.rollback();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

}
