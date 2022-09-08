package jnpf.base.util;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import jnpf.base.DataSourceInfo;
import jnpf.database.model.DbLinkEntity;
import jnpf.database.model.DataSourceUtil;
import jnpf.database.source.impl.DbDm;
import jnpf.database.source.impl.DbMysql;
import jnpf.database.source.impl.DbOracle;
import jnpf.database.source.impl.DbSqlserver;
import jnpf.database.util.DbTypeUtil;
import jnpf.util.StringUtil;
import jnpf.util.context.SpringContext;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
public class SourceUtil {

    public  DataSourceConfig dbConfig(String dbName){
        DataSourceConfig dsc=new DataSourceConfig();
        DataSourceUtil dataSourceUtil = SpringContext.getBean(DataSourceUtil.class);
        if (DbTypeUtil.checkDb(dataSourceUtil, DbMysql.DB_ENCODE)) {
            dsc.setDbType(DbType.MYSQL);
            dsc.setDriverName(DataSourceInfo.mysqlDriver);
        } else if (DbTypeUtil.checkDb(dataSourceUtil, DbOracle.DB_ENCODE)) {
            dsc.setDbType(DbType.ORACLE);
            dsc.setDriverName(DataSourceInfo.oracleDriver);
        }  else if (DbTypeUtil.checkDb(dataSourceUtil, DbSqlserver.DB_ENCODE)) {
            dsc.setDbType(DbType.SQL_SERVER);
            dsc.setDriverName(DataSourceInfo.sqlserverDriver);
        }else if(DbTypeUtil.checkDb(dataSourceUtil, DbDm.DB_ENCODE)){
            dsc.setDbType(DbType.DM);
            dsc.setDriverName(DataSourceInfo.dmDriver);
        }
        dsc.setUsername(dataSourceUtil.getUserName());
        dsc.setPassword(dataSourceUtil.getPassword());
        if(StringUtil.isEmpty(dbName)){
            dbName = dataSourceUtil.getDbName();
        }
        dsc.setUrl(DbTypeUtil.getUrl(dataSourceUtil,dbName));
        return dsc;
    }

    public  DataSourceConfig dbConfig(DbLinkEntity linkEntity){
        DataSourceConfig dsc=new DataSourceConfig();
        if (linkEntity.getDbType().equalsIgnoreCase(DbType.MYSQL.getDb())) {
            dsc.setDbType(DbType.MYSQL);
            dsc.setDriverName(DataSourceInfo.mysqlDriver);
            dsc.setUrl(DataSourceInfo.mysqlUrl
                    .replace("{host}",linkEntity.getHost())
                    .replace("{port}",linkEntity.getPort().toString())
                    .replace("{dbName}",linkEntity.getServiceName()));
        } else if (linkEntity.getDbType().equalsIgnoreCase(DbType.ORACLE.getDb())) {
            dsc.setDbType(DbType.ORACLE);
            dsc.setDriverName(DataSourceInfo.oracleDriver);
            dsc.setUrl(DataSourceInfo.oracleUrl
                    .replace("{host}",linkEntity.getHost())
                    .replace("{port}",linkEntity.getPort().toString())
                    .replace("{dbName}",linkEntity.getServiceName()));
            //oracle 默认 schema=username
            dsc.setSchemaName(linkEntity.getUserName().toUpperCase());
        }  else if (linkEntity.getDbType().equalsIgnoreCase(DbType.SQL_SERVER.getDb())) {
            dsc.setDbType(DbType.SQL_SERVER);
            dsc.setDriverName(DataSourceInfo.sqlserverDriver);
            dsc.setUrl(DataSourceInfo.sqlserverUrl
                    .replace("{host}",linkEntity.getHost())
                    .replace("{port}",linkEntity.getPort().toString())
                    .replace("{dbName}",linkEntity.getServiceName()));
        }else if(linkEntity.getDbType().equalsIgnoreCase(DbType.DM.getDb())){
            dsc.setDbType(DbType.DM);
            dsc.setDriverName(DataSourceInfo.dmDriver);
            dsc.setUrl(DataSourceInfo.dmUrl
                    .replace("{host}",linkEntity.getHost())
                    .replace("{port}",linkEntity.getPort().toString())
                    .replace("{dbName}",linkEntity.getServiceName()));
        }
        dsc.setUsername(linkEntity.getUserName());
        dsc.setPassword(linkEntity.getPassword());
        return dsc;
    }

    public DataSourceConfig dbConfig(String dbName, DbLinkEntity linkEntity) {
        if (linkEntity != null) {
            return dbConfig(linkEntity);
        } else {
            return dbConfig(dbName);
        }
    }

}
