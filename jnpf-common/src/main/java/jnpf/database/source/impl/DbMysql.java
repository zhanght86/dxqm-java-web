package jnpf.database.source.impl;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.toolkit.Constants;
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
import jnpf.database.sql.impl.DbSqlMysql;
import jnpf.database.util.DbUtil;
import jnpf.util.StringUtil;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DbMysql extends DbBase {
    public static final String DB_ENCODE = "MySQL";

    @Override
    public void setDbType() {
        mpDbType = DbType.MYSQL;
        connUrlEncode = "mysql";
        dbEncode = DB_ENCODE;
        driver = "com.mysql.cj.jdbc.Driver";
        fieldSql =  "SELECT COLUMN_NAME "+DbAliasEnum.FIELD_NAME.AS()+",data_type "+DbAliasEnum.DATA_TYPE.AS()+
                ",CHARACTER_MAXIMUM_LENGTH "+DbAliasEnum.DATA_LENGTH.AS()+", " +
                    "NUMERIC_PRECISION 精度,NUMERIC_SCALE 小数位数, "+
                    "IF ( IS_NULLABLE = 'YES', '1', '0' ) "+DbAliasEnum.ALLOW_NULL.AS()+", COLUMN_COMMENT "+DbAliasEnum.FIELD_COMMENT.AS()+","+
                    "IF ( COLUMN_KEY = 'PRI', '1', '0' ) "+DbAliasEnum.PRIMARY_KEY.AS()+", "+
                    "column_default "+DbAliasEnum.DEFAULTS.AS()+","+
                    "CONCAT(upper(COLUMN_NAME),'(',COLUMN_COMMENT,')') as 'F_DESCRIPTION' "+
                    "FROM INFORMATION_SCHEMA.COLUMNS "+
                    "WHERE TABLE_NAME = '"+ DbSttEnum.TABLE.getTarget()+"'AND TABLE_SCHEMA='"+ DbSttEnum.DB_NAME.getTarget()+"'";
        listSql = "SELECT table_name "+DbAliasEnum.TABLE_NAME.AS() +",table_rows "+DbAliasEnum.TABLE_SUM.AS()+"," +
                " data_length "+DbAliasEnum.TABLE_SIZE.AS()+", table_comment "+DbAliasEnum.TABLE_COMMENT.AS()+", " +
                        "CONCAT(table_name,'(',table_comment,')') as 'F_DESCRIPTION' FROM information_schema.TABLES WHERE " +
                        "TABLE_SCHEMA = '"+ DbSttEnum.DB_NAME.getTarget() +"'";
    }

    @Override
    public DbSqlBase getSqlBase() {
        return new DbSqlMysql();
    }

    @Override
    public String getDataType(DataTypeEnum dte){
        return dte.getMysqlFieldType();
    }

    @Override
    public String getConnectionUrl(String host, Integer port, String dbName) {
        return "jdbc:mysql://" + host + ":" + port + "/" + dbName +
                "?useUnicode=true&autoReconnect=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8";
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
                true,
                true
        );
    }

    @Override
    public DbTableFieldModel getFieldModel(ResultSet result, Map<String, Object> map) throws SQLException, DataException {
        DbTableFieldModel model = new DbTableFieldModel();
        if (!StringUtil.isEmpty(result.getString(DbAliasEnum.DATA_LENGTH.AS()))) {
            model.setDataLength(result.getString(DbAliasEnum.DATA_LENGTH.AS()));
        } else if (!StringUtil.isEmpty(result.getString("精度"))) {
            model.setDataLength(result.getString("精度") + "," + result.getString("小数位数"));
        }
        return DbUtil.getFieldModelCommon(result, this,model);
    }



    @Override
    public String getDbTime(Connection conn) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s') as TIME");
        return dbTimeCommon(conn,sql);
    }

    @Override
    public String getReplaceSql(String sql, String table, DbConnDTO dto) {
        /**
         * 说明：一个实例对应多个数据库
         * 表空间默认不常用,模式schema也没有
         */
        return DbUtil.getReplaceSql(sql,table,dto,
                true,
                false,
                false);
    }

    //-----------------------------mysql----------------------------------------------------

    /**
     * mysql 备份命令
     *
     * @param root     账号
     * @param pwd      密码
     * @param host     ip
     * @param dbName   数据库
     * @param backPath 备份路径
     * @param backName 文件名称
     * @return
     */
    public static String mysqlBackUp(String host, String root, String pwd, String dbName, String backPath, String backName) {
        StringBuffer mysql = new StringBuffer();
        mysql.append("mysqldump");
        mysql.append(" -h" + host);
        mysql.append(" -u" + root);
        mysql.append(" -p" + pwd);
        mysql.append(" " + dbName);

        String command = mysql.toString();
        String savePath = backPath + backName;

        boolean flag;
        // 获得与当前应用程序关联的Runtime对象
        Runtime r = Runtime.getRuntime();
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            // 在单独的进程中执行指定的字符串命令
            Process p = r.exec(command);
            // 获得连接到进程正常输出的输入流，该输入流从该Process对象表示的进程的标准输出中获取数据
            @Cleanup InputStream is = p.getInputStream();
            // InputStreamReader是从字节流到字符流的桥梁：它读取字节，并使用指定的charset将其解码为字符
            @Cleanup InputStreamReader isr = new InputStreamReader(is, Constants.UTF_8);
            //BufferedReader从字符输入流读取文本，缓冲字符，提供字符，数组和行的高效读取
            br = new BufferedReader(isr);
            String s;
            StringBuffer sb = new StringBuffer("");
            // 组装字符串
            while ((s = br.readLine()) != null) {
                sb.append(s + System.lineSeparator());
            }
            s = sb.toString();
            // 创建文件输出流
            @Cleanup FileOutputStream fos = new FileOutputStream(savePath);
            // OutputStreamWriter是从字符流到字节流的桥梁，它使用指定的charset将写入的字符编码为字节
            @Cleanup OutputStreamWriter osw = new OutputStreamWriter(fos, Constants.UTF_8);
            // BufferedWriter将文本写入字符输出流，缓冲字符，以提供单个字符，数组和字符串的高效写入
            bw = new BufferedWriter(osw);
            bw.write(s);
            bw.flush();
            flag = true;
        } catch (IOException e) {
            flag = false;
            e.printStackTrace();
        } finally {
            //由于输入输出流使用的是装饰器模式，所以在关闭流时只需要调用外层装饰类的close()方法即可，
            //它会自动调用内层流的close()方法
            try {
                if (null != bw) {
                    bw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (null != br) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(flag){
            System.out.println("备份成功！");
        }
        return backPath + backName;

    }

}
