package jnpf.base.util;


import jnpf.database.exception.DataException;
import jnpf.database.util.JdbcUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
public class DbSyncUtil {


    /**
     * 同步表
     * @param connectionStringTo
     * @param result
     * @param table
     * @return
     * @throws DataException
     * @throws SQLException
     */
    public static String tableSync(Connection connectionStringTo, ResultSet result, String table) throws DataException, SQLException {


        /*JdbcUtil.custom(connectionStringTo, insert.toString());*/

        return "ok";
    }

}
