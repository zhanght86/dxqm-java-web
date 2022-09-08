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
import jnpf.database.util.DbTypeUtil;
import jnpf.database.util.DbUtil;
import jnpf.database.util.JdbcUtil;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

import javax.sql.rowset.JdbcRowSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DbDm extends DbBase {
    public static final String DB_ENCODE = "DM8";
    public static final String SUM_SQL = "select count(*) F_SUM from " + DbSttEnum.TABLE.getTarget();
    public static final String sizeSql = "SELECT segment_name AS TABLENAME,\n" +
                             /*"BYTES B,\n" +*/
                             "BYTES / 1024 KB\n" +
                             /*"BYTES / 1024 / 1024 MB\n" +*/
                             "FROM user_segments\n" +
                             "where segment_name = ('"+ DbSttEnum.TABLE.getTarget() +"');";

    @Override
    public void setDbType() {
        mpDbType = DbType.DM;
        connUrlEncode = "dm";
        dbEncode = DB_ENCODE;
        driver = "dm.jdbc.driver.DmDriver";
        fieldSql = "select distinct A.column_name AS "+DbAliasEnum.FIELD_NAME.AS()+"," +
                        " A.data_type AS "+DbAliasEnum.DATA_TYPE.AS()+", " +
                        "A.data_length AS "+DbAliasEnum.DATA_LENGTH.AS()+"," +
                        "case A.nullable when 'N' then '1' else '0' end AS "+DbAliasEnum.PRIMARY_KEY.AS()+"," +
                        "case A.nullable when 'N' then '0' else '1' end AS "+DbAliasEnum.ALLOW_NULL.AS()+"," +
                        "B.comments AS "+DbAliasEnum.FIELD_COMMENT.AS()+" from user_tab_columns A " +
                        "left join user_col_comments B on " +
                        "A.table_name=B.table_name and A.column_name=B.column_name " +
                        "where A.table_name = '"+ DbSttEnum.TABLE.getTarget() +"'";
        listSql =
                "delete from " +  DbSttEnum.DB_SCHEMA.getTarget() + ".TEMP_TABLE;\n" + DbSttEnum.SPLIT.getTarget() +
                /*"create global temporary table TEMP_TABLE\n" +
                "(TABLE_NAME varchar(30),ROW_NUM int)\n" +
                "on commit preserve rows; split\n" +*/
                "\ndeclare \n" +
                "count_rows int;\n" +
                "count_size int;\n" +
                "v_sql varchar(100);\n" +
                "s_sql varchar(100);\n" +
                "begin\n" +
                " count_rows:= 0;\n" +
                " for a in(select TABLE_NAME from user_tables)\n" +
                " loop\n" +
                " v_sql:='select count(*) from "+DbSttEnum.DB_SCHEMA.getTarget()+".'||a.TABLE_NAME;\n" +
                " execute immediate v_sql into count_rows;\n" +
                " insert into TEMP_TABLE values(a.TABLE_NAME,count_rows);\n" +
                " end loop;\n" +
                "end; \n" + DbSttEnum.SPLIT.getTarget() +
                "\nSELECT \n" +
                " ut.TABLE_NAME " + DbAliasEnum.TABLE_NAME.AS() +
                /*",BYTES/1024||'KB' " + DbAliasEnum.TABLE_SIZE.AS() +*/
                ",tt.ROW_NUM " + DbAliasEnum.TABLE_SUM.AS() +
                ",utc.COMMENTS " + DbAliasEnum.TABLE_COMMENT.AS() +
                "\nFROM USER_TABLES AS ut \n" +
                /*"LEFT JOIN \n" +
                " user_segments AS us\n" +
                "ON \n" +
                " ut.TABLE_NAME = us.segment_name\n" +*/
                "LEFT JOIN \n" +
                " user_tab_comments AS utc\n" +
                "ON \n" +
                " ut.TABLE_NAME = utc.TABLE_NAME\n" +
                "LEFT JOIN\n" +
                " (select * from TEMP_TABLE) AS tt\n" +
                "ON\n" +
                " ut.TABLE_NAME = tt.TABLE_NAME \n" +
                "ORDER BY " + DbAliasEnum.TABLE_NAME.AS() +";" ;
                /*"DROP TABLE "+ DbSttEnum.DB_SCHEMA.getTarget() + ".TEMP_TABLE; ";*/
    }

    @Override
    public <T> ResultSet getResultSet(Connection conn, String sql,Class<T> modelType) throws Exception{
        if(modelType==DbTableModel.class) {
            //检查是否有临时表
            String checkTempTableSql ="select count(*) from user_tables where TABLE_NAME = 'TEMP_TABLE'";
            ResultSet resultSet = JdbcUtil.query(conn,checkTempTableSql);
            while(resultSet.next()){
                if("0".equals(resultSet.getString(1))){
                   String createTempTableSql =  "create global temporary table TEMP_TABLE\n" +
                    "(TABLE_NAME varchar(30),ROW_NUM int)\n" +
                    "on commit preserve rows;\n";
                    JdbcUtil.query(conn,createTempTableSql);
                }
            }
            /**
             * 1.删除临时表信息
             * 2.临时表添加表相关信息
             * 3.查询表
             **/
            String[] sqlList = sql.split(DbSttEnum.SPLIT.getTarget());
            ResultSet r = null;
            if(sqlList.length>1){
                for(int i=0;i<sqlList.length;i++){
                    r = JdbcUtil.query(conn,sqlList[i]);
                }
            }
            return r;
        }else {
            return super.getResultSet(conn, sql,null);
        }
    }

    @Override
    public DbSqlBase getSqlBase() {
        return new DbSqlOracle();
    }

    @Override
    public String getDataType(DataTypeEnum dte){
        return dte.getDmFieldType();
    }

    @Override
    public String getConnectionUrl(String host, Integer port, String dbName) {
        return "jdbc:dm://" + host + ":" + port + "/" + dbName +
                "?zeroDateTimeBehavior = convertToNull&useUnicode=true&characterEncoding=utf-8";
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
                true,
                false,
                true);
    }

    public static String getSumSizeSql(String sqlOrigin,DbTableModel model,Integer sum,Integer size){
        String sql = "INSERT INTO \""+DbSttEnum.TABLE.getTarget()+"\"\n" +
                "(\"F_TABLE_NAME\", \"F_SUM\", \"F_SIZE\",\"F_COMMENT\") \n" +
                "VALUES\n" +
                "('"+ model.getTableName() +
                "',"+ sum +
                ","+ size +
                ",'"+ model.getTableComment() +"');";

        sql = sqlOrigin + sql;
        return sql;
    }

    public static Integer getSum(Connection conn,DbTableModel model)throws SQLException{
        String sql = DbDm.SUM_SQL.replace(DbSttEnum.TABLE.getTarget(),model.getTable());


        Integer sum = null;
        try {
            @Cleanup ResultSet result =JdbcUtil.query(conn,sql);
            while (result.next()){
                sum = result.getInt("F_SUM");
            }
        }catch (Exception e){
            e.getMessage();
        }

        return sum;
    }



    @Override
    public DbTableFieldModel getFieldModel(ResultSet result, Map<String, Object> map) throws SQLException, DataException {
        return DbUtil.getFieldModelCommon(result,this,new DbTableFieldModel());
    }


    @Override
    public String getDbTime(Connection conn) {
        StringBuilder sql = new StringBuilder();
        sql.append("select to_char(sysdate,'yyyy-mm-dd hh24:mi:ss') as TIME ");
        return dbTimeCommon(conn,sql);
    }

    @Override
    public String getReplaceSql(String sql, String table, DbConnDTO dto) {
        /**
         * 说明：一个实例对应一个数据库，多个模式，一个用户对应一个模式，同oracle
         * 表空间需默认：MAIN
         */
        return DbUtil.getReplaceSql(sql,table,dto,
                false,
                true,
                true);
    }


    //---------------------------------dm---------------------------------------------------------------

    /**
     * 数据库备份
     *
     * @param userName
     * @param password
     * @param host
     * @param port
     * @param path
     * @param fileName
     */
    public static String dmBackUp(String userName, String password, String host, String port, String path,
                                  String fileName) {
        try {
            File saveFile = new File(path);
            if (!saveFile.exists()) {
                saveFile.mkdirs();
            }
            StringBuffer dexp = new StringBuffer();
            dexp.append("dexp ");
            dexp.append(userName);
            dexp.append("/");
            dexp.append(password);
            dexp.append("@");
            dexp.append(host);
            dexp.append(":");
            dexp.append(port);
            dexp.append(" file=");
            dexp.append(path + fileName);
            dexp.append(" owner=" + userName);
            Process p = Runtime.getRuntime().exec(dexp.toString());
            @Cleanup BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.forName("GBK")));
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            br.close();
            p.waitFor();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return path + fileName;
    }

}
