package jnpf.database.enums;
import jnpf.database.exception.DataException;
import jnpf.database.source.DbBase;
import jnpf.util.StringUtil;

import javax.xml.crypto.Data;

/**
 * 字段类型枚举
 * @author JNPF开发平台组
 * @version V3.2.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/6/29
 */
public enum DataTypeEnum {

    /**
     * 如{主类型},{次类型}:({默认字符长度},{限制长度}(*:不允许设置))
     */

    /**
     * 字符
     */
    VARCHAR(
            "varchar",
            "varchar:50,50",
            "NVARCHAR2,VARCHAR2:50,50",
            "nvarchar,varchar:50,50",
            "VARCHAR:50,50",
            "VARCHAR:50,50",
            "varchar:50,50"
    ),
    /**
     * 日期时间
     * 日期统一不指定长度
     */
    DATE_TIME(
            "datetime",
            "datetime:*",
            "DATE:*",
            "datetime:*",
            "TIMESTAMP,DATETIME,DATE,TIME:*",
            "DATE:*",
            "timestamp:*"
    ),
    /**
     * 浮点
     */
    DECIMAL(
            "decimal",
            "decimal:50,50",
            "DECIMAL:38,38",
            "decimal:38,38",
            "DECIMAL,DEC:*",
            "DECIMAL,NUMERIC:50,50",
            "numeric:50,50"
    ),
    /**
     * 文本
     */
    TEXT(
            "text",
            "text,tinytext,longtext:*",
            "CLOB:*",
            "CLOB:50,50",
            "TEXT,CLOB:*",
            "TEXT:*",
            "text:*"
    ),
    /**
     * 长整型
     */
    BIGINT(
            "bigint",
            "bigint:50,50",
            "NUMBER:*",
            "bigint:50,50",
            "BIGINT:*",
            "INT8:*",
            "int8:*"
    ),
    /**
     * 整型
     * SqlServer、PostGre:int不能指定长度
     */
    INT (
            "int",
            "int:*",
            "INT,NUMBER:*",
            "int:*",
            "INT:*",
            "INT4:*",
            "int4:*"
    );



    private String commonFieldType;
    private String mysqlFieldType;
    private String oracleFieldType;
    private String sqlserverFieldType;
    private String dmFieldType;
    private String kingbaseFieldType;
    private String postgreFieldType;



    DataTypeEnum(String commonFieldType,String mysqlFieldType,String oracleFieldType,String sqlserverFieldType,
                 String dmFieldType,String kingbaseFieldType,String postgreFieldType){
        this.commonFieldType = commonFieldType;
        this.mysqlFieldType = mysqlFieldType;
        this.oracleFieldType = oracleFieldType;
        this.sqlserverFieldType = sqlserverFieldType;
        this.dmFieldType = dmFieldType;
        this.kingbaseFieldType = kingbaseFieldType;
        this.postgreFieldType = postgreFieldType;
    }

    public String getCommonFieldType() {
        return commonFieldType;
    }

    public String getMysqlFieldType() {
        return mysqlFieldType;
    }

    public String getOracleFieldType() {
        return oracleFieldType;
    }

    public String getSqlserverFieldType() {
        return sqlserverFieldType;
    }

    public String getDmFieldType() {
        return dmFieldType;
    }

    public String getKingbaseFieldType() {
        return kingbaseFieldType;
    }

    public String getPostgreFieldType(){
        return postgreFieldType;
    }

    public static final String COLON = ":";
    public static final String COMMA = ",";
    public static final String ASTERISK = "*";

    /**
     * 根据不同数据库获取标准字段类型
     * @param dbFieldType
     */
    public static String getCommonFieldType(String dbFieldType, DbBase db) throws DataException {
        if(StringUtil.isNotNull(dbFieldType)){
            for (DataTypeEnum value : DataTypeEnum.values()) {
                for(String type : db.getDataType(value).split(COLON)[0].split(COMMA)){
                    if(type.equals(dbFieldType)){
                        return value.getCommonFieldType();
                    }
                }
            }
        }
        return null;
        /*throw  new DataException("表中字段类型与项目中默认配置不符。");*/
    }

    /**
     * 根据不同数据库获取各自数据库字段类型
     * @param commonFieldType
     * @param db
     * @return
     */
    public static String[] getDbFieldType(String commonFieldType,DbBase db){
        if(StringUtil.isNotNull(commonFieldType)){
            for (DataTypeEnum value : DataTypeEnum.values()) {
                if(value.getCommonFieldType().equals(commonFieldType)){
                    return db.getDataType(value).split(COLON)[0].split(COMMA);
                }
            }
        }
        return null;
    }

    /**
     * 根据不同数据库获取各自数据库字段长度限制
     * @param commonFieldType
     * @param db
     * @return
     */
    public static String[] getTypeLength(String commonFieldType,DbBase db){
        if(StringUtil.isNotNull(commonFieldType)){
            for (DataTypeEnum value : DataTypeEnum.values()) {
                if(value.getCommonFieldType().equals(commonFieldType)){
                    String typeLength = db.getDataType(value).split(COLON)[1];
                    //*:不允许设置长度
                    if(typeLength.equals(ASTERISK)){
                        return null;
                    }
                    return typeLength.split(COMMA);
                }
            }
        }
        return null;
    }


    public static String[] getDataLengths(DataTypeEnum dataTypeEnum,DbBase dbBase){



        return dbBase.getDataType(dataTypeEnum).split(COLON)[1].split(COMMA);
    }

    public static void main(String[] args) {
        String a = "varchar:50,50";
        System.out.println(a.split(COLON)[0]);

    }


}
