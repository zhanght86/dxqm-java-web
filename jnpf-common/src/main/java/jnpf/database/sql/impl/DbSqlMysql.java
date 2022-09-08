package jnpf.database.sql.impl;

import jnpf.database.source.DbBase;
import jnpf.database.sql.DbSqlBase;
import jnpf.database.source.impl.DbMysql;
import jnpf.database.sql.create.InsertSql;
import jnpf.util.StringUtil;

import java.util.List;

public class DbSqlMysql extends DbSqlBase {


    /**
     * 获取添加表时的字段注释
     * @param fieldComment 字段注释
     * @param dbBase 数据库类型
     * @return SQL片段
     */
    public String getCreFieldComment(String fieldComment, DbBase dbBase){
        String mysqlFieldComment = "";
        //判断是否是Mysql类型
        if(dbBase.getClass().equals(DbMysql.class)){
            if(StringUtil.isNotNull(fieldComment)){
                mysqlFieldComment = "\tCOMMENT\t\'" + fieldComment + "\'";
            }
        }
        return mysqlFieldComment;
    }

    /**
     * 获取注释
     * mysql独有注释方式
     * @param tableComment 表注释
     * @param dbBase 数据库类型
     * @return SQL片段
     */
    public String getTabComment(DbBase dbBase,String tableComment){
        if(dbBase.getClass().equals(DbMysql.class)){
            return  "\tCOMMENT\t\'" + tableComment + "\';";
        }else {
            return null;
        }
    }



    public String batchInsertSql(List<List<String>> dataList, String table){
        InsertSql insertSql = new InsertSql();
        insertSql.setBatchInsertSeparator(",");
        insertSql.setMysqlInsertBasicSql(insertSql.getBasicSql(table));
        String sql = insertSql.batch(dataList);
        //去除最后一个逗号
        return sql.substring(0,sql.length()-1)+";";
    }


}
