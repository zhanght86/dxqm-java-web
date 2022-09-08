package jnpf.database.sql.impl;

import jnpf.database.sql.DbSqlBase;
import jnpf.database.sql.create.InsertSql;

import java.sql.ResultSet;
import java.util.List;

public class DbSqlOracle extends DbSqlBase {

    @Override
    public String jdbcCreUpSql(String sql){
        String jdbcSql = "";
        //添加数据Sql处理
        if (sql.toLowerCase().contains("insert") && sql.replaceAll(" ", "").contains("),(")) {
            String[] splitSql = sql.split("\\),\\(");
            //centerSql取出INTO TEST_DETAILS ( F_ID, F_RECEIVABLEID)
            String centerSql = splitSql[0].split("VALUES")[0].split("INSERT")[1];
            //for循环尾部
            String lastSql = splitSql[splitSql.length - 1];
            splitSql[splitSql.length - 1] = lastSql.substring(0, lastSql.length() - 1);
            for (int i = 0; i < splitSql.length; i++) {
                //第一个语句INSERT INTO TEST_DETAILS ( F_ID, F_RECEIVABLEID) VALUES ( '71', '28bf3436e5d1'
                //需要拼接成 INSERT INTO TEST_DETAILS ( F_ID, F_RECEIVABLEID) VALUES ( '71', '28bf3436e5d1'）
                String sqlFlagm;
                if (i == 0) {
                    sqlFlagm = splitSql[i] + ")";
                } else {
                    sqlFlagm = "INSERT " + centerSql + "VALUES (" + splitSql[i] + ")";
                }
                jdbcSql = jdbcSql + sqlFlagm;
            }
        } else {
            jdbcSql = sql;
        }
        return jdbcSql;
    }

    public String batchInsertSql(List<List<String>> dataList, String table){
        InsertSql insertSql = new InsertSql();
        insertSql.setBatchInsertSeparator(";");
        insertSql.setOracleInsertBasicSql(insertSql.getBasicSql(table));
        return insertSql.batch(dataList);
    }




    public static String getOracleDataTime(ResultSet result,int i,String value)throws Exception{
            String dataType = result.getMetaData().getColumnTypeName(i).toLowerCase();
            if("date".equals(dataType) || dataType.contains("time")){
                value = "TO_DATE('" + value + "','YYYY-MM-DD HH24:MI:SS')";
            }else {
                value = "'" + value + "'";
            }
        return value;
    }





}
