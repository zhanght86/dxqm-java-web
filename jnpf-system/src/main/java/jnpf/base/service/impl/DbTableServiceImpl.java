package jnpf.base.service.impl;


import jnpf.base.DbTableModel;
import jnpf.database.enums.DataTypeEnum;
import jnpf.database.enums.DbSttEnum;
import jnpf.database.model.DataSourceUtil;
import jnpf.database.model.dto.DbConnDTO;
import jnpf.database.source.DbBase;
import jnpf.database.source.impl.DbKingbase;
import jnpf.database.exception.DataException;
import jnpf.base.ActionResult;
import jnpf.base.service.DbTableService;
import jnpf.base.service.DblinkService;
import jnpf.config.ConfigValueUtil;
import jnpf.database.model.DbLinkEntity;
import jnpf.database.model.DbTableDataForm;
import jnpf.database.model.DbTableFieldModel;
import jnpf.database.model.dto.DbTableDTO;
import jnpf.database.util.DbModelUtil;
import jnpf.database.util.JdbcUtil;
import jnpf.util.*;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据管理
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Slf4j
@Service
public class DbTableServiceImpl implements DbTableService {

    @Autowired
    private DblinkService dblinkService;
    @Autowired
    private DataSourceUtil dataSourceUtils;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private ConfigValueUtil configValueUtil;

    @Override
    public List<DbTableModel> getList(String dbId) throws DataException {
        return getListCommon(dbId, null,DbTableModel.class);
    }

    @Override
    public List<DbTableFieldModel> getFieldList(String dbId, String table) throws DataException {
       return getListCommon(dbId,table,DbTableFieldModel.class);
    }

    private <T> List<T> getListCommon(String dbId, String table, Class<T> modelType) throws DataException{
        DbConnDTO dbConnDTO = getDbConnDTO(dbId);
        Connection conn = dbConnDTO.getConnection();
        DbBase dbBase = dbConnDTO.getDbBase();
        //获取查询sql语句
        String sql = "";
        if(modelType==DbTableModel.class){
            sql = dbBase.getListSql();
        }else if(modelType==DbTableFieldModel.class){
            sql = dbBase.getFieldSql();
        }
        sql = dbBase.getReplaceSql(sql, table, dbConnDTO);
        //根据sql语句获取List
        return DbModelUtil.getModelList(conn,dbBase,sql,modelType);
    }


    @Override
    public List<Map<String, Object>> getData(DbTableDataForm dbTableDataForm, String dbId, String table) {
        List<Map<String, Object>> list = null;
        try {
            DbConnDTO dbConnDTO = getDbConnDTO(dbId);
            Connection conn = dbConnDTO.getConnection();
            DbBase dbBase = dbConnDTO.getDbBase();
            if (conn != null) {
                StringBuffer sql = new StringBuffer();
                sql.append(dbBase.getDataSql().replace(DbSttEnum.TABLE.getTarget(),table));
                //模糊查询
                if (!StringUtil.isEmpty(dbTableDataForm.getKeyword()) && !StringUtil.isEmpty(dbTableDataForm.getField())) {
                    sql.append(" where " + dbTableDataForm.getField() + " like '%" + dbTableDataForm.getKeyword() + "%'");
                }
                ResultSet query = JdbcUtil.query(conn, sql.toString());
                list = JdbcUtil.convertListString(query);
            }
        }catch (Exception e){
            e.getMessage();
        }
        return dbTableDataForm.setData(PageUtil.getListPage((int) dbTableDataForm.getCurrentPage(), (int) dbTableDataForm.getPageSize(), list), list.size());
    }

//    List getData(String dbId, String table){
//
//
//    }


    @Override
    public boolean isExistByFullName(String dbId, String table, String oldTable) {
        List<DbTableModel> data = null;
        try {
            data = this.getList(dbId).stream().filter(m -> m.getTable().equals(table)).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!StringUtil.isEmpty(oldTable)) {
            data = data.stream().filter(m -> !m.getTable().equals(oldTable)).collect(Collectors.toList());
        }
        return data.size() > 0;
    }

    @Override
    public String getDbTime(String dbId) throws DataException {
        String time = "";
        try {
            DbConnDTO dbConnDTO = getDbConnDTO(dbId);
            Connection conn = dbConnDTO.getConnection();
            DbBase db = dbConnDTO.getDbBase();
            time = db.getDbTime(conn);
        } catch (Exception e) {
            throw new DataException(e.getMessage());
        }
        return time;
    }


    @Override
    public void delete(String dbId, String table) {
        try {
            DbConnDTO dbConnDTO = getDbConnDTO(dbId);
            Connection conn = dbConnDTO.getConnection();
            DbBase dbBase = dbConnDTO.getDbBase();
            if (conn != null) {
                String sql = dbBase.getDeleteSql().replace(DbSttEnum.TABLE.getTarget(),table);
                JdbcUtil.custom(conn, sql);
            }
        }catch (Exception e){
            e.getMessage();
        }
    }

    @Override
    public ActionResult create(String dbId, DbTableModel dbTableModel, List<DbTableFieldModel> tableFieldList) throws DataException {
        DbConnDTO dbConnDTO = getDbConnDTO(dbId);
        Connection conn = dbConnDTO.getConnection();
        if(conn != null){
            checkCreateUpdateCommon(tableFieldList);
            dbConnDTO.getDbBase().createTable(
                    new DbTableDTO(
                            conn,
                            dbTableModel,
                            tableFieldList,
                            dbConnDTO.getTableSpace()
                    )
            );
            return ActionResult.success("新建成功");
        }else {
            return ActionResult.fail("连接失败");
        }
    }

    @Override
    public void update(String dbId, DbTableModel dbTableModel, List<DbTableFieldModel> tableFieldList) throws DataException {
            DbConnDTO dbConnDTO = getDbConnDTO(dbId);
            Connection conn = dbConnDTO.getConnection();
            if(conn != null) {
                checkCreateUpdateCommon(tableFieldList);
                dbConnDTO.getDbBase().updateTable(
                        new DbTableDTO(
                                conn,
                                dbTableModel,
                                tableFieldList,
                                dbConnDTO.getTableSpace()
                        )
                );
            }
    }

    private void checkCreateUpdateCommon(List<DbTableFieldModel> tableFieldList) throws DataException{
        //默认主键为字符串类型
        //主键会自动添加"非空"限制，所以不用做判断。(为空不加语句，且数据库默认字段可为空)
        for(DbTableFieldModel field : tableFieldList) {
            if (field.getPrimaryKey() == 1) {
                if(!field.getDataType().equals(DataTypeEnum.VARCHAR.getCommonFieldType())){
                    throw new DataException("请修改主键为默认\"字符串\"类型。");
                }
            }
        }
    }

    @Override
    public int getSum(String dbId,String table)throws DataException{
        DbLinkEntity link = dblinkService.getInfo(dbId);
        int sum = 0;
        try {
            DbConnDTO dbConnDTO = getDbConnDTO(dbId);
            Connection conn = dbConnDTO.getConnection();
            if (conn != null) {
                String sql = DbKingbase.DATA_SUM_SQL.replace(DbSttEnum.TABLE.getTarget(),table);
                ResultSet query = JdbcUtil.query(conn, sql);
                if(query.next()){
                    sum = Integer.valueOf(query.getString("COUNT_SUM"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sum;
    }


    private DbConnDTO getDbConnDTO(String linkId)throws DataException {
        DbConnDTO dbConnDTO;
        try {
            @Cleanup Connection conn = null;
            if("0".equals(linkId)){
                //多租户是否开启
                String tenSource = Boolean.parseBoolean(configValueUtil.getMultiTenancy()) ?
                        userProvider.get().getTenantDbConnectionString() : null;
                //默认数据库查询，从配置获取数据源信息
                dbConnDTO = new DbConnDTO(dataSourceUtils,tenSource);
            }else {
                dbConnDTO = new DbConnDTO(dblinkService.getInfo(linkId));
            }
            dbConnDTO.setConnection(JdbcUtil.getConn(dbConnDTO));
            return dbConnDTO;
        }catch (DataException e){
            throw e;
        }catch (Exception e){
            e.getMessage();
        }
        return null;
    }


}
