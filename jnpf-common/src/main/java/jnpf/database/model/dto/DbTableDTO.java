package jnpf.database.model.dto;

import jnpf.base.DbTableModel;
import jnpf.database.model.DbTableDataForm;
import jnpf.database.model.DbTableFieldModel;
import lombok.Data;

import java.sql.Connection;
import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class DbTableDTO {


    public DbTableDTO(Connection conn, DbTableModel dbTableModel, List<DbTableFieldModel> dbTableFieldList,String tableSpace){
        this.conn = conn;
        this.dbTableFieldList = dbTableFieldList;
        this.newTable = dbTableModel.getNewTable();
        this.oldTable = dbTableModel.getTable();
        this.tableComment = dbTableModel.getTableComment();
        this.tableSpace = tableSpace;
    }

    /**
     * 主键改变标识
     */
    private Boolean priChangFlag = false;

    /**
     * 数据源
     */
    private Connection conn;

    /**==============数据库信息==============**/

    private String dbName;

    private String tableSpace;

    /**===============表信息=================**/

    /**
     * 表名
     */
    private String oldTable;

    /**
     *
     */
    private String newTable;

    /**
     * 查询时被使用表名
     */
    private String originTable;

    /**
     * 表说明
     */
    private String tableComment;


    /**===============字段信息=================**/

    /**
     * 字段信息集合
     */
    private List<DbTableFieldModel> dbTableFieldList;



}
