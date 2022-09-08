package jnpf.database.sql.create;
import lombok.Data;

import java.util.List;

@Data
public class InsertSql {

    /**==================框架======================**/
    /**
     * 框架 - 基础
     * MySQL基本框架：INSERT INTO + {表名} + VALUES ({字段集合}),... + {注释}
     * Oracle基本框架：INSERT INTO + {表名} + VALUES ({字段集合}) + {注释}; ...
     * @return SQL
     */
    /**=================================================================**/
    private String oracleInsertBasicSql = "";
    private String mysqlInsertBasicSql = "";
    private String basicSql;
    private String batchInsertSeparator;
    public String getBasicSql(String table){
        return this.basicSql =  "INSERT INTO " + table + " VALUES";
    }

    /**
     * 批量插入数据
     * 使用不指定字段名SQL语句
     */
    public String batch(List<List<String>> dataList){
        String sql = "";
        //遍历游标
        for(List<String> data :dataList){
            String values = "";
            //遍历字段
            for(Object value : data){
                value = value + ",";
                values += value;
            }
            sql += oracleInsertBasicSql + "(" + values.substring(0,values.length()-1) + ")"+ batchInsertSeparator;

        }
        return  mysqlInsertBasicSql + sql;
    }
}
