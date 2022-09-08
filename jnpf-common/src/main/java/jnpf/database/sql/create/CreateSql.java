package jnpf.database.sql.create;

import jnpf.database.enums.DataTypeEnum;
import jnpf.database.enums.DbAliasEnum;
import jnpf.database.model.DbTableFieldModel;
import jnpf.database.model.dto.DbTableDTO;
import jnpf.database.source.DbBase;
import jnpf.database.sql.impl.DbSqlMysql;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class CreateSql {

    private String table;
    private String fields;
    private String comments;
    private String field;
    private String dataType;
    private String notNull;
    private String primaryKey;
    private String mysqlComment;
    private DbSqlMysql sqlMysql = new DbSqlMysql();


    /**
     * 获取 - SQL
     * @param dto
     * @param dbBase
     * @return
     */
    public String getCreTabSql(DbTableDTO dto,DbBase dbBase){
        setFields(dto.getDbTableFieldList(),dbBase);
        setComments(dto.getNewTable(),dto.getTableComment(),dto.getDbTableFieldList(),dbBase);
        return createTableSqlFrame();
    }

    /**==================框架======================**/

    /**
     * 框架 - 基础
     * 基本框架：CREATE TABLE + {表名} + ({字段集合}) + {注释}
     * @return SQL
     */
    private String createTableSqlFrame(){
        return  "CREATE TABLE\t"+ table + fields + comments;
    }

    /**
     * 框架 - 字段
     * 字段：{字段名} + {字段类型} + ({字段长度} + {非空限定} + {主键判断} + [{mysql注释}],
     * @return 单个字段SQL
     */
    private String fieldsSqlFrame(){
        return field + " " + dataType + " " + notNull + " " + primaryKey + " " + mysqlComment + ",";
    }

    /**===================装配=====================**/


    /**
     * 装配 - 字段集合
     * @param dbTableFieldModels
     * @param dbBase
     */
    private void setFields(List<DbTableFieldModel> dbTableFieldModels , DbBase dbBase){
        fields = "";
        for(DbTableFieldModel item : dbTableFieldModels){
            //类型转换 (fieldName即是fieldComment);
            setField(item.getField(),item.getDataType(),item.getDataLength(),item.getAllowNull(),
                    item.getPrimaryKey(),item.getFieldName(),dbBase);
            fields = fields + fieldsSqlFrame();
        }
        //去除最后一个逗号
        fields = " ( " + fields.substring(0,fields.length()-1) + " ) ";
    }

    /**
     * 装配 - 单个字段
     * @param field 字段名
     * @param dataType 字段类型
     * @param dataLength 字段长度
     * @param allowNull 允空
     * @param primary 主键
     * @param fieldComment 字段注释
     * @param dbBase 数据库类型
     * @return 单个字段SQL片段
     */
    private void setField(String field, String dataType,String dataLength,
                                      Integer allowNull,Integer primary,String fieldComment, DbBase dbBase){
        //字段名
        this.field = field;
        //设置
        setDataType(dataLength,dataType,dbBase);
        //允空
        notNull = allowNull.equals(DbAliasEnum.ALLOW_NULL.isFalse()) ? "\tNOT NULL\t" : "";
        //主键
        primaryKey = primary.equals(DbAliasEnum.PRIMARY_KEY.isTrue()) ? "\tPRIMARY KEY\t" : "";
        //mysql注释
        mysqlComment = sqlMysql.getCreFieldComment(fieldComment,dbBase);
    }

    /**
     * 装配 - 注释、表名
     * @param tableComment
     * @param models
     * @param dbBase
     */
    private void setComments(String newTableName,String tableComment,List<DbTableFieldModel> models,DbBase dbBase){
        //设置表名
        table = newTableName;
        //mysql注释转换
        comments = sqlMysql.getTabComment(dbBase,tableComment);
        if(comments!=null){
            return;
        }
        //设置表注释
        comments = ";" + setComment(true,table,"",tableComment);
        //设置字段注释
        for(DbTableFieldModel model : models){
            comments = comments + setComment(false,table,model.getField(),model.getFieldName());
        }
    }

    /**===================设置=====================**/

    /**
     * 设置 - 类型
     * @param dataLength
     * @param dataType
     * @param dbBase
     */
    private void setDataType(String dataLength, String dataType, DbBase dbBase){
        //数据类型
        String[] dataTypes =  DataTypeEnum.getDbFieldType(dataType,dbBase);
        if(dataTypes!=null){
            this.dataType = dataTypes[0];
        }else {
            log.error("数据类型未找到,转换异常！");
        }

        //类型长度信息
        String[] lengthInfo =  DataTypeEnum.getTypeLength(dataType,dbBase);
        //长度设置：3种情况。null：不允许设置长度
        if(lengthInfo!=null){
            //1、长度参数为空及判断字符串是否是整型（无效长度）
            if(dataLength.equals("") || dataLength==null || !Pattern.compile("^[-\\+]?[\\d]*$").matcher(dataLength).matches()){
                //默认长度
                dataLength = lengthInfo[0];
            }else {
                //2、如果长度小于0，按默认长度
                if(Integer.valueOf(dataLength) < 1){
                    dataLength = lengthInfo[0];
                }
                //3、判断超过长度限制，按限制长度
                if(Integer.valueOf(dataLength) > Integer.valueOf(lengthInfo[1])){
                    dataLength = lengthInfo[1];
                }
            }
            this.dataType = this.dataType + "("+ dataLength +")";
        }
    }

    /**
     * 设置 - 注释
     * @param tableCommentFlag
     * @param table
     * @param clumnName
     * @param comment
     * @return
     */
    public static String setComment(Boolean tableCommentFlag,String table,String clumnName,String comment){
        String whereComment;
        if(tableCommentFlag){
            whereComment = "\tTABLE\t" + table;
        }else{
            whereComment = "\tCOLUMN\t" + table + "." + clumnName;
        }
        return "COMMENT ON\t" + whereComment + "\tis\t" + "\'"+ comment +"\';";
    }


}
