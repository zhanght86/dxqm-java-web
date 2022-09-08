package jnpf.database.util;

import jnpf.base.DbTableModel;
import jnpf.database.exception.DataException;
import jnpf.database.model.DbTableFieldModel;
import jnpf.database.source.DbBase;
import jnpf.database.source.impl.DbDm;
import jnpf.database.source.impl.DbOracle;
import jnpf.database.sql.impl.DbSqlOracle;
import lombok.Cleanup;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DbModelUtil {
    /**
     * 从result获取list
     * @param conn 数据源
     * @param dbBase 数据库类型
     * @param sql SQL语句
     * @return 数据List容器
     * @throws SQLException 异常
     */
    public static <T> List<T> getModelList(Connection conn, DbBase dbBase, String sql,Class<T> modelType) throws DataException {
        List<T> list = new ArrayList<>();
        try{
            if(dbBase.getClass()==DbOracle.class && modelType == DbTableFieldModel.class){
                return (List<T>) new DbOracle().getTableFiledList(conn,sql);
            }
            @Cleanup ResultSet result = dbBase.getResultSet(conn,sql,modelType);
            while (result.next()) {
                T model = null;
                if(modelType==DbTableModel.class){
                    model = (T)dbBase.getTableModel(result);
                }else if(modelType==DbTableFieldModel.class){
                    model = (T)dbBase.getFieldModel(result, null);
                }
                list.add(model);
            }
        }catch (SQLException e){
            e.getMessage();
        }catch (Exception e){
            throw new DataException(e.getMessage());
        }
        return list;
    }






    public static List<List<String>> getDataList(ResultSet result, DbBase dbBase)throws Exception{
        //获得结果集结构信息,元数据
        ResultSetMetaData md = result.getMetaData();
        //获得列数
        int columnCount = md.getColumnCount();
        List<List<String>> dataList = new ArrayList<>();
        boolean oracleFlag = dbBase.getClass()== DbOracle.class;
        while (result.next()) {
            List<String> data = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                //jdbc中通过下标获取数据，integer、long类型可以通用getString
                String value = result.getString(i);
                //oracle类型判断
                if(oracleFlag){
                    value = DbSqlOracle.getOracleDataTime(result,i,value);
                }else {
                    value = "'" + value + "'";
                }
                data.add(value);
            }
            dataList.add(data);
        }
        return dataList;
    }




}
