package jnpf.database.enums;

import jnpf.database.source.DbBase;
import jnpf.database.source.impl.DbPostgre;

/**
 * 别名枚举
 * @author JNPF开发平台组
 * @version V3.2.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/6/29
 */
public enum DbAliasEnum {

    /**
     * 字段名
     */
    FIELD_NAME("F_FIELD_NAME"),
    /**
     * 注释
     */
    FIELD_COMMENT ("F_FIELD_COMMENT"),
    /**
     * 默认值
     */
    DEFAULTS("F_DEFAULTS"),
    /**
     * 类型
     * 字符串：varchar
     */
    DATA_TYPE("F_DATATYPE"),
    /**
     * 说明
     */
    DESCRIPTION("F_DESCRIPTION"),
    /**
     * 允空
     * 允许：1，不允许：0
     */
    ALLOW_NULL("F_ALLOW_NULL"){
        @Override
        public Integer isTrue(){
            return 1;
        }
        @Override
        public Integer isFalse(){
            return 0;
        }
    },
    /**
     * 长度
     */
    DATA_LENGTH("F_DATA_LENGTH"),
    /**
     * 主键
     * 存在：1，不存在：0
     */
    PRIMARY_KEY("F_PRIMARY_KEY"){
        @Override
        public Integer isTrue(){
            return 1;
        }
        @Override
        public Integer isFalse(){
            return 0;
        }
    },
    /**
     * 表名
     */
    TABLE_NAME("F_TABLE_NAME"),
    /**
     * 表注释
     */
    TABLE_COMMENT("F_TABLE_COMMENT"),
    /**
     * 表总数
     */
    TABLE_SUM("F_TABLE_SUM"),
    /**
     * 表大小
     */
    TABLE_SIZE("F_TABLE_SIZE")

    ;

    public Integer isTrue(){
        return null;
    }
    public Integer isFalse(){
        return null;
    }


    private String alias;

    DbAliasEnum(String alias){
        this.alias = alias;
    }

    public String AS(){
        return alias;
    }

    public String asByDb(DbBase db){
        if(DbPostgre.class.equals(db.getClass())){
            //postgre别名只能输出小写，Oracle只能大写
            return alias.toLowerCase();
        }else {
            return alias;
        }
    }

}
