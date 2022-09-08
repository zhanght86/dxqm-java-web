package jnpf.database.source.impl;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import jnpf.base.DbTableModel;
import jnpf.database.data.DataSourceContextHolder;
import jnpf.database.enums.DataTypeEnum;
import jnpf.database.enums.DbAliasEnum;
import jnpf.database.enums.DbSttEnum;
import jnpf.database.exception.DataException;
import jnpf.database.model.DbLinkEntity;
import jnpf.database.model.dto.DbConnDTO;
import jnpf.database.source.DbBase;
import jnpf.database.model.DbTableFieldModel;
import jnpf.database.sql.DbSqlBase;
import jnpf.database.sql.impl.DbSqlOracle;
import jnpf.database.util.DbUtil;
import jnpf.database.util.JdbcUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DbSqlserver extends DbBase {
    public static final String DB_ENCODE = "SQLServer";

    @Override
    public void setDbType() {
        mpDbType = DbType.SQL_SERVER;
        connUrlEncode = "sqlserver";
        dbEncode = DB_ENCODE;
        driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        deleteSql = "DROP TABLE " + DbSttEnum.TABLE.getTarget()+";";
        fieldSql =  "SELECT cast(a.name as varchar(50)) "+DbAliasEnum.FIELD_NAME.AS()+" ," +
                    " cast(case when exists(SELECT 1 FROM sysobjects where xtype='PK' and name in ( " +
                    " SELECT name FROM sysindexes WHERE indid in( " +
                    " SELECT indid FROM sysindexkeys WHERE id = a.id AND colid=a.colid ))) " +
                    " then '1' else '0' end as varchar(50)) "+DbAliasEnum.PRIMARY_KEY.AS()+", " +
                    " cast(b.name as varchar(50)) "+DbAliasEnum.DATA_TYPE.AS()+", " +
                    " cast(COLUMNPROPERTY(a.id,a.name,'PRECISION') as varchar(50)) "+DbAliasEnum.DATA_LENGTH.AS()+", " +
                    " cast(case when a.isnullable=0 then '0'else '1' end as varchar(50)) "+DbAliasEnum.ALLOW_NULL.AS()+", " +
                    " cast(isnull(e.text,'') as varchar(50)) "+DbAliasEnum.DEFAULTS.AS()+", " +
                    " cast(isnull(g.[value],'') as varchar(50)) " +DbAliasEnum.FIELD_COMMENT.AS()+
                    "\nFROM syscolumns a " +
                    "left join systypes b on a.xusertype=b.xusertype " +
                    "inner join sysobjects d on a.id=d.id and d.xtype='U' and d.name<>'dtproperties' " +
                    "left join syscomments e on a.cdefault=e.id " +
                    "left join sys.extended_properties g on a.id=g.major_id and a.colid=g.minor_id " +
                    "left join sys.extended_properties f on d.id=f.major_id and f.minor_id=0 " +
                    "where d.name='"+ DbSttEnum.TABLE.getTarget() +"'" +
                    "order by a.id,a.colorder";
        listSql =  "SET NOCOUNT ON DECLARE @TABLEINFO TABLE ( NAME VARCHAR(50) ," +
                        " SUMROWS VARCHAR(11) , RESERVED VARCHAR(50) , DATA VARCHAR(50) ," +
                        " INDEX_SIZE VARCHAR(50) , UNUSED VARCHAR(50) , PK VARCHAR(50) )" +
                        " DECLARE @TABLENAME TABLE ( NAME VARCHAR(50) ) DECLARE @NAME VARCHAR(50)" +
                        " DECLARE @PK VARCHAR(50) INSERT INTO @TABLENAME ( NAME )" +
                        " SELECT O.NAME FROM SYSOBJECTS O , SYSINDEXES I " +
                        "WHERE O.ID = I.ID AND O.XTYPE = 'U' AND I.INDID < 2 ORDER BY I.ROWS DESC , O.NAME" +
                        " WHILE EXISTS ( SELECT 1 FROM @TABLENAME ) BEGIN SELECT TOP 1 " +
                        "@NAME = NAME FROM @TABLENAME DELETE @TABLENAME WHERE NAME = " +
                        "@NAME DECLARE @OBJECTID INT SET @OBJECTID = OBJECT_ID(@NAME) SELECT @PK = " +
                        "COL_NAME(@OBJECTID, COLID) FROM SYSOBJECTS AS O INNER JOIN SYSINDEXES AS I ON I.NAME = " +
                        "O.NAME INNER JOIN SYSINDEXKEYS AS K ON K.INDID = I.INDID WHERE O.XTYPE = " +
                        "'PK' AND PARENT_OBJ = @OBJECTID AND K.ID = @OBJECTID INSERT INTO " +
                        "@TABLEINFO ( NAME , SUMROWS , RESERVED , DATA , INDEX_SIZE , UNUSED ) EXEC SYS.SP_SPACEUSED " +
                        "@NAME UPDATE @TABLEINFO SET PK = @PK WHERE NAME = @NAME END SELECT cast(F.NAME AS varchar(50))" +
                        DbAliasEnum.TABLE_NAME.AS()+",cast(ISNULL( P.TDESCRIPTION, F.NAME )  AS varchar(50)) "+
                        DbAliasEnum.TABLE_COMMENT.AS()+",cast(F.RESERVED AS varchar(50))" +
                        " F_SIZE,cast(RTRIM( F.SUMROWS ) AS varchar(50)) "+DbAliasEnum.TABLE_SUM.AS()+",cast(F.PK AS varchar(50)) " +
                        "F_PRIMARYKEY FROM @TABLEINFO F LEFT JOIN ( SELECT NAME = CASE WHEN A.COLORDER = " +
                        "1 THEN D.NAME ELSE '' END , TDESCRIPTION = CASE WHEN A.COLORDER = 1 THEN ISNULL(F.VALUE, '')" +
                        " ELSE '' END FROM SYSCOLUMNS A LEFT JOIN SYSTYPES B ON A.XUSERTYPE = B.XUSERTYPE INNER JOIN SYSOBJECTS D" +
                        " ON A.ID = D.ID AND D.XTYPE = 'U' AND D.NAME <> 'DTPROPERTIES' LEFT JOIN SYS.EXTENDED_PROPERTIES F ON D.ID =" +
                        " F.MAJOR_ID WHERE A.COLORDER = 1 AND F.MINOR_ID = 0 ) P ON F.NAME = P.NAME WHERE 1 = 1 ORDER BY "+DbAliasEnum.TABLE_NAME.AS();
    }

    @Override
    public DbSqlBase getSqlBase() {
        return new DbSqlOracle();
    }

    @Override
    public String getDataType(DataTypeEnum dte){
        return dte.getSqlserverFieldType();
    }

    @Override
    public String getConnectionUrl(String host, Integer port, String dbName) {
        return "jdbc:sqlserver://" + host + ":" + port + ";databaseName=" + dbName;
    }

    @Override
    public void setDynamicMap(HashMap<String, TableNameHandler> map, DbTableModel dbTableModel) {
        map.put(dbTableModel.getTable(), (sql, tableName) -> DataSourceContextHolder.getDatasourceName()+".dbo."+dbTableModel.getTable());
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
        return DbUtil.getFieldModelCommon(result,this,new DbTableFieldModel());
    }

//    @Override
//    public void createTable(DbTableDTO dto) {
//        StringBuilder sql = new StringBuilder();
//        sql.append("create table " + dto.getNewTable() + " ");
//        sql.append("( ");
//        //默认主键为字符串类型
//        dto.getDbTableFieldList().stream().filter(t->t.getPrimaryKey()==1).collect(Collectors.toList()).get(0).setDataType("varchar");
//        for (DbTableFieldModel item : dto.getDbTableFieldList()) {
//            sql.append(item.getField() + " " + item.getDataType());
//            if ("varchar".equals(item.getDataType()) || "decimal".equals(item.getDataType())) {
//                sql.append("(" + item.getDataLength() + ") ");
//            }
//            if ("1".equals(String.valueOf(item.getPrimaryKey()))) {
//                sql.append(" NOT NULL PRIMARY KEY");
//            } else if (item.getAllowNull().compareTo(0) == 0) {
//                sql.append(" NOT NULL ");
//            } else {
//                sql.append(" NULL ");
//            }
//            sql.append(",");
//        }
//        sql.deleteCharAt(sql.length() - 1);
//        sql.append(");");
//        sql.append("declare @CurrentUser sysname\r\n");
//        sql.append("select @CurrentUser = user_name()\r\n");
//        sql.append("execute sp_addextendedproperty 'MS_Description', '" + dto.getTableComment() + "','user', @CurrentUser, 'table', '" + dto.getNewTable() + "'\r\n");
//        for (DbTableFieldModel item : dto.getDbTableFieldList()) {
//            sql.append("execute sp_addextendedproperty 'MS_Description', '" + item.getFieldName() + "', 'user', @CurrentUser, 'table', '" + dto.getNewTable() + "', 'column', '" + item.getField() + "'\r\n");
//        }
//        JdbcUtil.custom(dto.getConn(), sql.toString());
//    }
//
//    @Override
//    public void updateTable(DbTableDTO dto) {
//        StringBuilder sql = new StringBuilder();
//        sql.append("drop table " + dto.getOldTable() + ";");
//        sql.append("; create table " + dto.getNewTable() + " ");
//        sql.append("( ");
//        //默认主键为字符串类型
//        dto.getDbTableFieldList().stream().filter(t->t.getPrimaryKey()==1).collect(Collectors.toList()).get(0).setDataType("varchar");
//        for (DbTableFieldModel item : dto.getDbTableFieldList()) {
//            sql.append(item.getField() + " " + item.getDataType());
//
//            for(String arrayDataType : DataTypeEnum.getDbFieldType(DataTypeEnum.VARCHAR.getCommonFieldType(),this)){
//                if (arrayDataType.equals(item.getDataType()) || arrayDataType.equals(item.getDataType())) {
//                    sql.append("(" + item.getDataLength() + ") ");
//                }
//            }
//
//            if ("1".equals(String.valueOf(item.getPrimaryKey()))) {
//                sql.append(" NOT NULL PRIMARY KEY");
//            } else if (item.getAllowNull().compareTo(0) == 0) {
//                sql.append(" NOT NULL ");
//            } else {
//                sql.append(" NULL ");
//            }
//            sql.append(",");
//        }
//        sql = sql.deleteCharAt(sql.length() - 1);
//        sql.append(");");
//        sql.append("declare @CurrentUser sysname\r\n");
//        sql.append("select @CurrentUser = user_name()\r\n");
//        sql.append("execute sp_addextendedproperty 'MS_Description', '" + dto.getTableComment() + "','user', @CurrentUser, 'table', '" + dto.getNewTable() + "'\r\n");
//        for (DbTableFieldModel item : dto.getDbTableFieldList()) {
//            sql.append("execute sp_addextendedproperty 'MS_Description', '" + item.getFieldName() + "', 'user', @CurrentUser, 'table', '" + dto.getNewTable() + "', 'column', '" + item.getField() + "'\r\n");
//        }
//        JdbcUtil.custom(dto.getConn(), sql.toString());
//    }

    @Override
    public String getDbTime(Connection conn) {
        StringBuilder sql = new StringBuilder();
        sql.append("Select CONVERT(varchar(100), GETDATE(), 120) as TIME");
        return dbTimeCommon(conn,sql);
    }

    @Override
    public String getReplaceSql(String sql, String table, DbConnDTO dto) {
        //目前只涉及表名参数
        return DbUtil.getReplaceSql(sql,table,dto,
                false,
                false,
                false);
    }


    //-----------------------------sqlserver----------------------------------------------------

    /**
     * 数据库备份
     *
     * @param userName     用户名
     * @param password     密码
     * @param host         ip
     * @param port         端口
     * @param databaseName 数据库
     * @param path         备份路径
     * @param fileName     备份名称
     */
    public static String serverBackUp(String userName, String password, String host, Integer port, String path, String fileName, String databaseName) {
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            File saveFile = new File(path);
            if (!saveFile.exists()) {
                saveFile.mkdirs();
            }
            StringBuilder backup = new StringBuilder();
            backup.append("backup database ");
            backup.append(databaseName + " to disk='" + path + fileName + "' ");
            DbLinkEntity linkEntity = new DbLinkEntity();
            linkEntity.setUserName(userName);
            linkEntity.setPassword(password);
            linkEntity.setHost(host);
            linkEntity.setPort(port);
            linkEntity.setServiceName(databaseName);
            conn = JdbcUtil.getConn(linkEntity);
            stmt = conn.prepareStatement(backup.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage());
        } catch (DataException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return path + fileName;
    }


}
