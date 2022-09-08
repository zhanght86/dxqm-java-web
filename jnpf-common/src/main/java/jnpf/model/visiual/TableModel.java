package jnpf.model.visiual;

import lombok.Data;

import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:56
 */
@Data
public class TableModel {

     /**
     * 类型：1-主表、0-子表
     */
     private String typeId;
     /**
     * 表名
     */
     private String table;
     /**
     * 说明
     */
     private String tableName;
     /**
     * 主键
     */
     private String tableKey;
     /**
     * 外键字段
     */
     private String tableField;
     /**
     * 关联主表
     */
     private String relationTable;
     /**
     * 关联主键
     */
     private String relationField;

     private List<TableFields> fields;
}
