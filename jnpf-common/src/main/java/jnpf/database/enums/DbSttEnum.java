package jnpf.database.enums;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * 数据库 结构、参数 替换枚举 structure
 * @author JNPF开发平台组
 * @version V3.2.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/6/29
 */
public enum DbSttEnum {

    /**
     * 数据库
     */
    DB_URL("{dbUrl}"),
    /**
     * 数据库名
     */
    DB_NAME("{dbName}"),
    /**
     * 用户名
     */
    USER_NAME("{userName}"),
    /**
     * 模式
     * schema关键字,加前缀
     */
    DB_SCHEMA("{dbSchema}"),
    /**
     * 表空间
     */
    TABLE_SPACE("{tableSpace}"),
    /**
     * 表
     */
    TABLE("{table}"),
    /**
     * 替换符
     */
    SPLIT("split");
    /**
     * 替换目标
     */
    private String target;

    public String getTarget(){
        return this.target;
    }

    DbSttEnum(String target){
        this.target = target;
    }


}
