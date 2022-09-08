package jnpf.base.service.impl;


import jnpf.base.service.DbTableService;
import jnpf.database.model.DbLinkEntity;
import jnpf.base.service.DbSyncService;
import jnpf.base.service.DblinkService;
import jnpf.database.source.DbBase;
import jnpf.database.util.DbModelUtil;
import jnpf.database.util.DbTypeUtil;
import jnpf.database.util.JdbcUtil;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

/**
 * 数据同步
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Slf4j
@Service
public class DbSyncServiceImpl implements DbSyncService {

    @Autowired
    private DblinkService dblinkService;
    @Autowired
    private DbTableService dbTableService;

    @Override
    public String importTableData(String dbConnectionFrom, String dbConnectionTo, String table){
        DbLinkEntity linkFrom = dblinkService.getInfo(dbConnectionFrom);
        DbLinkEntity linkTo = dblinkService.getInfo(dbConnectionTo);
        String s = linkFrom.getHost() + linkFrom.getPort() + linkFrom.getServiceName();
        String s1 = linkTo.getHost() + linkTo.getPort() + linkTo.getServiceName();
        if (s.equals(s1)){
            return "同一连接下不能同步数据";
        }
        try {
            @Cleanup Connection connectionStringFrom = JdbcUtil.getConn(linkFrom);
            @Cleanup Connection connectionStringTo = JdbcUtil.getConn(linkTo);
            if(connectionStringFrom==null){
                return "数据库"+linkFrom.getFullName()+"连接失败";
            }
            if(connectionStringTo==null){
                return "数据库"+linkTo.getFullName()+"连接失败";
            }


            String toSql = "SELECT count(*) FROM " + table;
            ResultSet result =JdbcUtil.query(connectionStringTo, toSql);
            int rowCount=0;
            if (result.next())
            {
                rowCount = result.getInt(1);
            }else {
                //TODO 添加表
//                dbTableService.getFieldList()
//                dbTableService.create();
                return "被同步表不存在。";
            }
            if(rowCount>0){
                return "同步失败,目标表存在数据";
            }

            //初始库获取数据
            String fromSql = "SELECT * FROM " + table;
            ResultSet resultFrom = JdbcUtil.query(connectionStringFrom, fromSql);
            DbBase dbBase = DbTypeUtil.getDb(connectionStringFrom);
            if(resultFrom!=null){
                List<List<String>> dataList =  DbModelUtil.getDataList(resultFrom,dbBase);
                String sql = dbBase.getSqlBase().batchInsertSql(dataList,table);
                /*JdbcUtil.custom(connectionStringFrom,sql);*/
                return "ok";
            }else {
                return "false";
            }
        } catch (Exception e) {
            return(e.getMessage());
        }
    }
}
