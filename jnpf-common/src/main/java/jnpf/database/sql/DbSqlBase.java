package jnpf.database.sql;

import lombok.Data;

import java.util.List;


@Data
public abstract class DbSqlBase {



    public String jdbcCreUpSql(String originSql){
        return originSql;
    }

    public abstract String batchInsertSql(List<List<String>> dataList, String table);

}
