package jnpf.base.service;

import jnpf.database.exception.DataException;
import jnpf.base.ActionResult;
import jnpf.database.model.DbTableDataForm;
import jnpf.database.model.DbTableFieldModel;
import jnpf.base.DbTableModel;

import java.util.List;

/**
 * 数据管理
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
public interface DbTableService {

    /**
     * 表列表
     *
     * @param dbId 连接Id
     * @return
     */
    List<DbTableModel> getList(String dbId) throws DataException;

    /**
     * 表字段
     *
     * @param dbId 连接Id
     * @param table  表名
     * @return
     */
    List<DbTableFieldModel> getFieldList(String dbId, String table) throws DataException;

    /**
     * 表数据
     *
     * @param dbTableDataForm 分页
     * @param dbId    连接Id
     * @param table     表名
     * @return
     */
    List getData(DbTableDataForm dbTableDataForm, String dbId, String table);

    /**
     * 验证名称
     *
     * @param dbId 连接Id
     * @param table  表名
     * @param oldTable     主键值
     * @return
     */
    boolean isExistByFullName(String dbId, String table, String oldTable) throws DataException;

    /**
     * 获取时间
     *
     * @param dbId 连接Id
     * @return
     */
    String getDbTime(String dbId) throws DataException;

    /**
     * 删除表
     *
     * @param dbId 连接Id
     * @param table  表名
     */
    void delete(String dbId, String table) throws DataException;

    /**
     * 创建表
     *
     * @param dbId         连接Id
     * @param dbTableModel     表对象
     * @param dbTableFieldModels 字段对象
     */
    ActionResult create(String dbId, DbTableModel dbTableModel, List<DbTableFieldModel> dbTableFieldModels) throws DataException;

    /**
     * 修改表
     *
     * @param dbId 连接Id
     * @param dbTableModel 表对象
     * @param dbTableFieldModels 字段对象
     */
    void update(String dbId, DbTableModel dbTableModel, List<DbTableFieldModel> dbTableFieldModels) throws DataException;

    int getSum(String dbId,String table)throws DataException;
}
