package jnpf.database.source.impl;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import jnpf.base.DbTableModel;
import jnpf.database.data.DataSourceContextHolder;
import jnpf.database.enums.DataTypeEnum;
import jnpf.database.enums.DbAliasEnum;
import jnpf.database.enums.DbSttEnum;
import jnpf.database.exception.DataException;
import jnpf.database.model.dto.DbConnDTO;
import jnpf.database.source.DbBase;
import jnpf.database.model.DbTableFieldModel;
import jnpf.database.sql.DbSqlBase;
import jnpf.database.sql.impl.DbSqlOracle;
import jnpf.database.util.DbUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DbKingbase extends DbBase {
    public static final String DB_ENCODE = "KingbaseES";
    public static final String DATA_SUM_SQL = "SELECT count(*) AS COUNT_SUM FROM "+ DbSttEnum.TABLE.getTarget();
    public static final String FIELD_SQL = " \n" +
            "SELECT\n" +
            "\tpretty_sizes.table_name,\n" +
            "\tpretty_sizes.comment_info,\n" +
            "\tsys_size_pretty(table_size) AS table_size\n" +
            "\t/*,sys_size_pretty(total_size) AS total_size*/\n" +
            "FROM\n" +
            "\t(\n" +
            "\t\tSELECT\n" +
            "\t\t\ttable_name,\n" +
            "\t\t\tcomment_info,\n" +
            "\t\t\tsys_table_size(table_name) AS table_size,\n" +
            "\t\t\tsys_total_relation_size(table_name) AS total_size\n" +
            "\t\tFROM\n" +
            "\t\t\t(\n" +
            "\t\t\t\tSELECT \n" +
            "\t\t\t\t\tt.TABLE_NAME AS table_name,\n" +
            "\t\t\t\t\tc.COMMENTS AS comment_info\n" +
            "\t\t\t\tFROM\n" +
            "\t\t\t\t\tinformation_schema.TABLES AS t\n" +
            "\t\t\t\t\tLEFT JOIN\n" +
            "\t\t\t\t\t(SELECT TABLE_NAME,COMMENTS FROM DBA_TAB_COMMENTS)AS c\n" +
            "\t\t\t\tON\n" +
            "\t\t\t\t\tt.TABLE_NAME = c.TABLE_NAME\n" +
            "\t\t\t\tWHERE\n" +
            "\t\t\t\t \tTABLE_SCHEMA = 'YANYU'\n" +
            "\t\t\t) AS all_tables\n" +
            "\t\tORDER BY\n" +
            "\t\t\ttotal_size DESC\n" +
            "\t) AS pretty_sizes\n" +
            "\t";


    @Override
    public void setDbType() {
        mpDbType = DbType.KINGBASE_ES;
        connUrlEncode = "kingbase8";
        dbEncode = DB_ENCODE;
        driver = "com.kingbase8.Driver";
        fieldSql =
            "SELECT DISTINCT\n"+
            "c.relname,\n"+
            "a.attnum,\n"+
            "a.attname AS "+DbAliasEnum.FIELD_NAME.AS()+",\n"+
            "t.typname AS "+DbAliasEnum.DATA_TYPE.AS()+",\n"+
            "a.attnotnull AS "+DbAliasEnum.ALLOW_NULL.AS()+",\n"+
            "b.character_maximum_length AS "+DbAliasEnum.DATA_LENGTH.AS()+",\n"+
            "d.COMMENTS AS "+DbAliasEnum.FIELD_COMMENT.AS()+",\n"+
            "pk.colname AS "+DbAliasEnum.PRIMARY_KEY.AS()+"\n"+
            "FROM sys_class c\n"+
            "INNER JOIN sys_namespace n on c.relnamespace = n.oid\n"+
            "INNER JOIN sys_attribute a ON c.oid = a.attrelid\n"+
            "INNER JOIN information_schema.columns b ON c.relname = b.table_name\n"+
            "INNER JOIN DBA_COL_COMMENTS d on a.attname = d.column_name\n"+
            "INNER JOIN sys_type t ON a.atttypid = t.oid\n"+
            "LEFT JOIN\n"+
            "(SELECT\n"+
            "c.conname AS pk_name,\n"+
            "a.attname AS colname\n"+
            "FROM\n"+
            "sys_constraint AS c\n"+
            "INNER JOIN sys_class AS clz ON c.conrelid = clz.oid\n"+
            "INNER JOIN sys_attribute AS a ON a.attrelid = clz.oid\n"+
            "AND a.attnum = c.conkey [1]\n"+
            "WHERE\n"+
            "clz.relname = '"+ DbSttEnum.TABLE.getTarget()+"'\n"+
            "AND c.contype = 'p') AS pk ON a.attname = pk.colname\n"+
            "WHERE attnum > 0\n"+
            "AND b.column_name = a.attname\n"+
            "AND c.relname = d.table_name\n"+
            "AND nspname = '"+ DbSttEnum.DB_SCHEMA.getTarget()+"'\n"+
            "AND c.relname = '"+ DbSttEnum.TABLE.getTarget()+"'\n"+
            "ORDER BY attnum;";
        listSql =
                "SELECT t.TABLE_NAME AS "+DbAliasEnum.TABLE_NAME.AS()+",c.COMMENTS AS "+DbAliasEnum.TABLE_COMMENT.AS()+" FROM\n" +
                "information_schema.TABLES AS t\n" +
                "LEFT JOIN\n" +
                "(SELECT TABLE_NAME,COMMENTS FROM DBA_TAB_COMMENTS)AS c\n" +
                "ON\n" +
                "t.TABLE_NAME = c.TABLE_NAME\n" +
                "\n" +
                "WHERE\n" +
                " TABLE_SCHEMA = '"+ DbSttEnum.DB_SCHEMA.getTarget()+"'";


    }

    @Override
    public DbSqlBase getSqlBase() {
        return new DbSqlOracle();
    }

    @Override
    public String getDataType(DataTypeEnum dte){
        return dte.getKingbaseFieldType();
    }

    @Override
    public String getConnectionUrl(String host, Integer port, String dbName) {
        return "jdbc:kingbase8://"+ host +":"+ port +"/"+ dbName;
    }

    @Override
    public void setDynamicMap(HashMap<String, TableNameHandler> map, DbTableModel dbTableModel) {
        map.put(dbTableModel.getTable(), (sql, tableName) -> DataSourceContextHolder.getDatasourceName()+"."+dbTableModel.getTable());
    }

    @Override
    public DbTableModel getTableModel(ResultSet result) throws SQLException {
        return DbUtil.getTableModelCommon(result,
                true,
                true,
                true,
                false,
                false,
                false
        );
    }

    @Override
    public DbTableFieldModel getFieldModel(ResultSet result, Map<String, Object> map) throws SQLException, DataException {
        DbTableFieldModel model = new DbTableFieldModel();
        //主键
        if(result.getString(DbAliasEnum.PRIMARY_KEY.AS()) != null){
            model.setPrimaryKey(DbAliasEnum.PRIMARY_KEY.isTrue());
        }else {
            model.setPrimaryKey(DbAliasEnum.PRIMARY_KEY.isFalse());
        }
        //允空
        if(result.getBoolean(DbAliasEnum.ALLOW_NULL.AS())){
            model.setAllowNull(DbAliasEnum.ALLOW_NULL.isFalse());
        }else {
            model.setAllowNull(DbAliasEnum.ALLOW_NULL.isTrue());
        }
        return DbUtil.getFieldModelCommon(result, this,model);
    }


    @Override
    public String getDbTime(Connection conn) {
        return null;
    }

    @Override
    public String getReplaceSql(String sql, String table, DbConnDTO dto) {
        /**
         * 说明：一个实例对应多个数据库，一个数据库对应多个模式，一个用户对应一个模式
         * 数据库 = dbName;
         * 模式 = schema = userName;
         * 表空间需默认：JNPF
         */
        return DbUtil.getReplaceSql(sql,table,dto,
                true,
                true,
                true);
    }

}
