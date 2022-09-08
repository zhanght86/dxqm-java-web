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
import jnpf.database.util.JdbcUtil;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
public class DbOracle extends DbBase {
    public static final String DB_ENCODE = "Oracle";


    @Override
    public void setDbType() {
        mpDbType = DbType.ORACLE;
        connUrlEncode = "oracle";
        dbEncode = DB_ENCODE;
        driver = "oracle.jdbc.OracleDriver";
        deleteSql = "DROP TABLE " + DbSttEnum.TABLE.getTarget()+";";
        //TODO BINARY_FLOAT类型查询不出来，这个语句有隐患
        fieldSql = "SELECT * FROM \n" +
                "\n" +
                "(\n" +
                "SELECT DISTINCT\n" +
                "\tA.column_name AS "+DbAliasEnum.FIELD_NAME.AS()+",\n" +
                "\tA.data_type AS "+DbAliasEnum.DATA_TYPE.AS()+",\n" +
                "\tA.CHAR_COL_DECL_LENGTH AS "+DbAliasEnum.DATA_LENGTH.AS()+",\n" +
                "CASE\n" +
                "\t\tA.nullable \n" +
                "\t\tWHEN 'N' THEN\n" +
                "\t\t'0' ELSE '1' \n" +
                "\tEND AS "+DbAliasEnum.ALLOW_NULL.AS()+",\n" +
                "CASE\n" +
                "\tA.nullable \n" +
                "\tWHEN 'N' THEN\n" +
                "\t'1' ELSE '0' \n" +
                "\tEND AS "+DbAliasEnum.PRIMARY_KEY.AS()+",\n" +
                "\tB.comments AS "+DbAliasEnum.FIELD_COMMENT.AS() +
                "\nFROM\n" +
                "\tuser_tab_columns A,\n" +
                "\tuser_col_comments B,\n" +
                "\tall_cons_columns C,\n" +
                "\tUSER_TAB_COMMENTS D \n" +
                "WHERE\n" +
                "\ta.COLUMN_NAME = b.column_name \n" +
                "\tAND A.Table_Name = B.Table_Name \n" +
                "\tAND A.Table_Name = D.Table_Name \n" +
                "\tAND ( A.TABLE_NAME = c.table_name ) \n" +
                "\tAND A.Table_Name = '"+ DbSttEnum.TABLE.getTarget()+"'\n" +
                ") A,\n" +
                "(\n" +
                "select a.column_name name,case when a.column_name=t.column_name then 1 else 0 end "+ DbAliasEnum.PRIMARY_KEY.AS() +"\n" +
                "from user_tab_columns a\n" +
                "left join (select b.table_name,b.column_name from user_cons_columns b\n" +
                "join user_constraints c on c.CONSTRAINT_NAME=b.CONSTRAINT_NAME\n" +
                "where c.constraint_type   ='P') t\n" +
                "on a.table_name=t.table_name\n" +
                "where a.table_name='"+ DbSttEnum.TABLE.getTarget() +"'\n" +
                ") B WHERE A."+DbAliasEnum.FIELD_NAME.AS()+" = b.NAME";
        listSql = "SELECT " +
                "a.TABLE_NAME "+DbAliasEnum.TABLE_NAME.AS()+", " +
                "b.COMMENTS "+DbAliasEnum.TABLE_COMMENT.AS()+", " +
                "a.num_rows "+DbAliasEnum.TABLE_SUM.AS()+
                "\nFROM " +
                "user_tables a, " +
                "user_tab_comments b " +
                "WHERE " +
                "a.TABLE_NAME = b.TABLE_NAME " +
                "and a.TABLESPACE_NAME='"+ DbSttEnum.TABLE_SPACE.getTarget()+"'";
    }


    public List<DbTableFieldModel> getTableFiledList(Connection conn,String sql) throws Exception{
        //Oracle特定一些方法
        @Cleanup ResultSet result = JdbcUtil.query(conn, sql);
        List<Map<String, Object>> mapList = JdbcUtil.convertList2(result);
        DbBase dbBase = new DbOracle();
        List<DbTableFieldModel> list = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            DbTableFieldModel model = dbBase.getFieldModel(result, map);
                list.add(model);
        }
        Collections.reverse(list);
        return list;
    }

    @Override
    public DbSqlBase getSqlBase() {
        return new DbSqlOracle();
    }

    @Override
    public String getDataType(DataTypeEnum dte){
        return dte.getOracleFieldType();
    }

    @Override
    public String getConnectionUrl(String host, Integer port, String dbName) {
        return "jdbc:oracle:thin:@" + host + ":" + port + ":" + dbName;
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
                true,
                true,
                false,
                true
        );
    }

    @Override
    public DbTableFieldModel getFieldModel(ResultSet result, Map<String, Object> map) throws SQLException, DataException {
        DbTableFieldModel model = new DbTableFieldModel();
        String dataType = DataTypeEnum.getCommonFieldType(String.valueOf(map.get(DbAliasEnum.DATA_TYPE.AS())), this);
        model.
            setField(String.valueOf(map.get(DbAliasEnum.FIELD_NAME.AS()))).
            setFieldComment(String.valueOf(map.get(DbAliasEnum.FIELD_COMMENT.AS()))).
            setDataType(dataType).
            setDescription(map.get(DbAliasEnum.FIELD_NAME.AS()) + "(" + map.get(DbAliasEnum.FIELD_COMMENT.AS()) + ")").
            setDataLength(String.valueOf(map.get(DbAliasEnum.DATA_LENGTH.AS())).equals("null")?"":String.valueOf(map.get(DbAliasEnum.DATA_LENGTH.AS()))).
            setAllowNull(Integer.valueOf(String.valueOf(map.get(DbAliasEnum.ALLOW_NULL.AS())))).
            setPrimaryKey(Integer.valueOf(String.valueOf(map.get(DbAliasEnum.PRIMARY_KEY.AS()))));
        return DbUtil.getFieldModelCommon(result, this,model);
    }

    @Override
    public String getDbTime(Connection conn) {
        StringBuilder sql = new StringBuilder();
        sql.append("select to_char(sysdate,'yyyy-mm-dd hh24:mi:ss') as TIME from dual");
        return dbTimeCommon(conn,sql);
    }

    @Override
    public String getReplaceSql(String sql, String table, DbConnDTO dto) {
        /**
         * 说明：一个实例对应一个数据库，多个模式，一个用户对应一个模式
         * 表空间需默认：JNPFCLOUD
         */
        return DbUtil.getReplaceSql(sql,table,dto,
                false,
                true,
                true);
    }


    //-----------------------------oracle----------------------------------------------------

    /**
     * 备份指定用户数据库
     *
     * @param userName 用户名
     * @param password 密码
     * @param sid      用户所在的SID
     * @param host     ip
     * @param path     保存路径
     * @param fileName 保存名称
     */
    public static String oracleBackUp(String userName, String password, String host, String sid, String path, String fileName) {
        try {
            File saveFile = new File(path);
            if (!saveFile.exists()) {
                saveFile.mkdirs();
            }
            StringBuffer exp = new StringBuffer();
            exp.append("exp ");
            exp.append(userName);
            exp.append("/");
            exp.append(password);
            exp.append("@");
            exp.append(host);
            exp.append("/");
            exp.append(sid);
            exp.append(" file=");
            exp.append(path + "/" + fileName);
            Process p = Runtime.getRuntime().exec(exp.toString());
            @Cleanup InputStreamReader isr = new InputStreamReader(p.getErrorStream());
            @Cleanup BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.indexOf("错误") != -1) {
                    break;
                }
            }
            p.destroy();
            p.waitFor();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return path + fileName;
    }





}
