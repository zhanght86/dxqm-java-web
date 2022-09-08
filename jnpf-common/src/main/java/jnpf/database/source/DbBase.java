package jnpf.database.source;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import jnpf.base.DbTableModel;
import jnpf.database.enums.DataTypeEnum;
import jnpf.database.enums.DbSttEnum;
import jnpf.database.exception.DataException;
import jnpf.database.model.DbTableFieldModel;
import jnpf.database.model.dto.DbConnDTO;
import jnpf.database.model.dto.DbTableDTO;
import jnpf.database.source.impl.*;
import jnpf.database.sql.DbSqlBase;
import jnpf.database.sql.create.CreateSql;
import jnpf.database.util.JdbcUtil;
import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库基础表
 * @author JNPF开发平台组
 * @version V3.2.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/6/21
 */
@Data
public abstract class DbBase {
    protected String dbEncode;
    protected String connUrlEncode;
    protected String driver;
    protected String fieldSql;
    protected String listSql;
    protected String deleteSql = "DROP TABLE IF EXISTS " + DbSttEnum.TABLE.getTarget()+";";
    protected String dataSql = "SELECT * FROM "+ DbSttEnum.TABLE.getTarget();
    protected DbType mpDbType;
    public static final DbBase[] DB_BASES = {new DbMysql(),new DbSqlserver(),new DbDm(),new DbOracle(),
            new DbKingbase(),new DbPostgre()};
    public static final String[] DB_ENCODES = {DbMysql.DB_ENCODE,DbOracle.DB_ENCODE,DbSqlserver.DB_ENCODE,
            DbDm.DB_ENCODE,DbKingbase.DB_ENCODE,DbPostgre.DB_ENCODE};

    public DbBase(){
        setDbType();
    }

    /**
     * 设置数据库一些静态参数
     * 赋值继承父类的属性
     */
    protected abstract void setDbType();

    public abstract DbSqlBase getSqlBase();
    /**
     * 获取数据类型
     * @param dte 数据类型枚举
     * @return 数据类型code
     */
    public abstract String getDataType(DataTypeEnum dte);

    /**
     * 获取字段类型
     * @param dte 数据类型枚举
     * @return 字段类型
     */

    /**==================================数据源==================================**/
    /**
     * 获取数据库连接Url
     * @param host 地址
     * @param port 端口
     * @param dbName 数据库名
     * @return String
     */
    public abstract String getConnectionUrl(String host,Integer port,String dbName);

    /**
     * 动态设置数据源
     * @param map 数据源map
     * @param dbTableModel 表模型
     */
    public abstract void setDynamicMap(HashMap<String, TableNameHandler> map, DbTableModel dbTableModel);

    /**=====================================模型======================================**/

    public <T> ResultSet getResultSet(Connection conn,String sql,Class<T> modelType) throws Exception{
        return JdbcUtil.query(conn,sql);
    }

    /**
     * 获取表信息
     * @param result
     * @return 表对象模型
     * @throws SQLException SQL异常
     */
    public abstract DbTableModel getTableModel(ResultSet result) throws SQLException;

    /**
     * 获取字段信息
     * @param result 返回集
     * @param map Oracle信息
     * @return 表字段模型
     * @throws SQLException SQL异常
     */
    public abstract DbTableFieldModel getFieldModel(ResultSet result, Map<String, Object> map) throws SQLException, DataException;

    /**================================增改========================================**/
    /**
     * 创建表
     * @param dto 创建参数
     */
    public void createTable(DbTableDTO dto) throws DataException {
        createUpdateCommon(dto,"");
    }

    /**
     * 更新表
     * @param dto 更改参数
     */
    public void updateTable(DbTableDTO dto) throws DataException {
        //SqlServer：if exists语句跟其他的不一样
        String delSql = deleteSql.replace(DbSttEnum.TABLE.getTarget(),dto.getOldTable());
        createUpdateCommon(dto,delSql);
    }


    private void createUpdateCommon(DbTableDTO dto,String delSql) throws DataException {
        String sql = new CreateSql().getCreTabSql(dto,this);
        JdbcUtil.custom(dto.getConn(), delSql + sql);
    }


    /**==========================**/

    /**
     * 获取数据库时间
     * @return 时间
     */
    public abstract String getDbTime(Connection conn);

    public String dbTimeCommon(Connection conn,StringBuilder sql){
        String time = "";
        try {
            ResultSet result = JdbcUtil.query(conn, sql.toString());
            while (result.next()) {
                time = result.getString("TIME");
            }
            return time;
        } catch (Exception e) {
            e.getMessage();
        }
        return time;
    }

    /**
     * 不同数据库结构性替换SQL语句
     * @param sql SQL语句
     * @param table 表
     * @return 转换后SQL语句
     */
    public abstract String getReplaceSql(String sql, String table, DbConnDTO dto);




}
