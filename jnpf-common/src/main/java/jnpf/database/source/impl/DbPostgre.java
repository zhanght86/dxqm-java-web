package jnpf.database.source.impl;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import jnpf.base.DbTableModel;
import jnpf.database.data.DataSourceContextHolder;
import jnpf.database.enums.DataTypeEnum;
import jnpf.database.enums.DbAliasEnum;
import jnpf.database.enums.DbSttEnum;
import jnpf.database.exception.DataException;
import jnpf.database.model.DbTableFieldModel;
import jnpf.database.model.dto.DbConnDTO;
import jnpf.database.source.DbBase;
import jnpf.database.sql.DbSqlBase;
import jnpf.database.sql.impl.DbSqlOracle;
import jnpf.database.util.DbUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DbPostgre extends DbBase {
    public static final String DB_ENCODE = "PostgreSQL";

    @Override
    public void setDbType() {
        mpDbType = DbType.POSTGRE_SQL;
        connUrlEncode = "postgresql";
        dbEncode = DB_ENCODE;
        driver = "org.postgresql.Driver";
        fieldSql =  "SELECT\n" +
                "\tbase.\"column_name\"\n"+DbAliasEnum.FIELD_NAME.AS()+",\n" +
                "\tcol_description ( t1.oid, t2.attnum )\n"+DbAliasEnum.FIELD_COMMENT.AS()+",\n" +
                "\tbase.udt_name\n"+DbAliasEnum.DATA_TYPE.AS()+",\n" +
                "\tt2.attnotnull AS\t"+ DbAliasEnum.ALLOW_NULL.AS()+",\n" +
                "\tCOALESCE(character_maximum_length, numeric_precision, datetime_precision)\n"+DbAliasEnum.DATA_LENGTH.AS()+",\n" +
                "\t(CASE\n" +
                "\t\tWHEN ( SELECT t2.attnum = ANY ( conkey ) FROM pg_constraint WHERE conrelid = t1.oid AND contype = 'p' ) = 't' \n" +
                "\t\tTHEN 1 ELSE 0 \n" +
                "\tEND ) \n" + DbAliasEnum.PRIMARY_KEY.AS() +
                "\nFROM\n" +
                "\tinformation_schema.COLUMNS base,\n" +
                "\tpg_class t1,\n" +
                "\tpg_attribute t2 \n" +
                "WHERE\n" +
                "\tbase.\"table_name\" = '"+DbSttEnum.TABLE.getTarget()+"' \n" +
                "\tAND t1.relname = base.\"table_name\" \n" +
                "\tAND t2.attname = base.\"column_name\" \n" +
                "\tAND t1.oid = t2.attrelid \n" +
                "\tAND t2.attnum > 0;\n";
        listSql =/*"select tablename "+ DbAliasEnum.TABLE_NAME.AS() +" from pg_tables where schemaname='public'";*/
                "select relname as "+ DbAliasEnum.TABLE_NAME.AS() +",cast(obj_description(relfilenode,'pg_class') " +
                        "as varchar) as "+DbAliasEnum.TABLE_COMMENT.AS()+" from pg_class c\n" +
                        "where relname in (select tablename from pg_tables where schemaname='public'" +
                        " and position('_2' in tablename)=0);";

    }
    @Override
    public DbSqlBase getSqlBase() {
        return new DbSqlOracle();
    }

    @Override
    public String getDataType(DataTypeEnum dte){
        return dte.getPostgreFieldType();
    }
    
    @Override
    public String getConnectionUrl(String host, Integer port, String dbName) {
        return "jdbc:postgresql://"+ host +":"+ port +"/"+ dbName;
    }

    @Override
    public void setDynamicMap(HashMap<String, TableNameHandler> map, DbTableModel dbTableModel) {
        map.put(dbTableModel.getTable().toLowerCase(), (sql, tableName) -> DataSourceContextHolder.getDatasourceName().toUpperCase()+"."+dbTableModel.getTable());
    }

    @Override
    public DbTableModel getTableModel(ResultSet result) throws SQLException {
        return DbUtil.getTableModelCommon(result,
                true,
                true,
                false,
                false,
                false,
                false
        );
    }

    @Override
    public DbTableFieldModel getFieldModel(ResultSet result, Map<String, Object> map) throws SQLException, DataException {
        DbTableFieldModel model =  new DbTableFieldModel();
        //"t"不允许为空,"f"允许为空
        if(result.getString(DbAliasEnum.ALLOW_NULL.asByDb(this)).equals("t")){
            model.setAllowNull(DbAliasEnum.ALLOW_NULL.isFalse());
        }else {
            model.setAllowNull(DbAliasEnum.ALLOW_NULL.isTrue());
        }
        return DbUtil.getFieldModelCommon(result,this,model);
    }

    @Override
    public String getDbTime(Connection conn) {
        return null;
    }

    @Override
    public String getReplaceSql(String sql, String table, DbConnDTO dto) {
        return DbUtil.getReplaceSql(sql,table,dto,
                true,
                false,
                false);
    }
}
