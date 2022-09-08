package jnpf.base.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import jnpf.base.UserInfo;
import jnpf.database.model.DataSourceUtil;
import jnpf.database.model.DbLinkEntity;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.entity.ProvinceEntity;
import jnpf.base.service.BillRuleService;
import jnpf.base.service.DataInterfaceService;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.service.ProvinceService;
import jnpf.config.ConfigValueUtil;
import jnpf.database.source.impl.DbOracle;
import jnpf.database.util.DbTypeUtil;
import jnpf.database.util.JdbcUtil;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.model.flowtask.FlowTableModel;
import jnpf.database.exception.DataException;
import jnpf.exception.WorkFlowException;
import jnpf.model.FormAllModel;
import jnpf.model.FormColumnModel;
import jnpf.model.FormEnum;
import jnpf.model.visiual.DataTypeConst;
import jnpf.model.visiual.JnpfKeyConsts;
import jnpf.model.visiual.fields.FieLdsModel;
import jnpf.model.visiual.fields.config.ConfigModel;
import jnpf.model.visiual.fields.props.PropsBeanModel;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.PositionService;
import jnpf.permission.service.UserService;
import jnpf.util.*;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 在线工作流开发
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Slf4j
@Component
public class FlowDataUtil {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private DataSourceUtil dataSourceUtil;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private UserService userService;
    @Autowired
    private BillRuleService billRuleService;
    @Autowired
    private DictionaryDataService dictionaryDataService;
    @Autowired
    private DataInterfaceService dataInterfaceService;
    @Autowired
    private ProvinceService provinceService;

    /**
     * 获取有表的数据库连接
     *
     * @return
     */
    private Connection getTableConn(DbLinkEntity link) throws SQLException {
        Connection conn = null;
        if (link != null) {
            try {
                conn = JdbcUtil.getConn(link);
            } catch (DataException e) {
                e.printStackTrace();
            }
        } else {
            String tenId = "";
            if (!Boolean.parseBoolean(configValueUtil.getMultiTenancy())) {
                tenId = dataSourceUtil.getDbName();
            } else {
                tenId = userProvider.get().getTenantDbConnectionString();
            }
            try {
                conn = JdbcUtil.getConn(dataSourceUtil, tenId);
            } catch (DataException e) {
                e.printStackTrace();
            }
        }
        if (conn == null) {
            throw new SQLException("连接数据库失败");
        }
        return conn;
    }

    /**
     * 获取有子表数据
     *
     * @param sql sql语句
     * @return
     * @throws DataException
     */
    private List<Map<String, Object>> getTableList(Connection conn, String sql) throws SQLException {
        ResultSet rs = JdbcUtil.query(conn, sql);
        List<Map<String, Object>> dataList = JdbcUtil.convertListString(rs);
        return dataList;
    }

    /**
     * 获取主表数据
     *
     * @param sql sql语句
     * @return
     * @throws DataException
     */
    private Map<String, Object> getMast(Connection conn, String sql) throws SQLException{
        ResultSet rs = JdbcUtil.query(conn, sql);
        Map<String, Object> mast = JdbcUtil.convertMapString(rs);
        Map<String, Object> mastData = new HashMap<>(16);
        for (String key : mast.keySet()) {
            mastData.put(key.toLowerCase(), mast.get(key));
        }
        return mastData;
    }

    /**
     * 返回主键名称
     *
     * @param conn
     * @param mainTable
     * @return
     */
    public String getKey(Connection conn, String mainTable) throws SQLException {
        String pKeyName = "f_id";
        //catalog 数据库名
        String catalog = conn.getCatalog();
        ResultSet primaryKeyResultSet = conn.getMetaData().getPrimaryKeys(catalog, null, mainTable);
        while (primaryKeyResultSet.next()) {
            pKeyName = primaryKeyResultSet.getString("COLUMN_NAME");
        }
        primaryKeyResultSet.close();
        return pKeyName;
    }

    //--------------------------------------------信息-----------------------------------------------------

    /**
     * 详情查询
     **/
    public Map<String, Object> info(List<FieLdsModel> fieLdslist, FlowTaskEntity entity, List<FlowTableModel> tableList, boolean convert, DbLinkEntity link) throws WorkFlowException {
        try {
            List<FormAllModel> formAllModel = new ArrayList<>();
            //递归遍历模板
            FormCloumnUtil.recursionForm(fieLdslist, formAllModel);
            //处理好的数据
            Map<String, Object> result = new HashMap<>(16);
            if (tableList.size() > 0) {
                result = this.tableInfo(entity, tableList, formAllModel, convert, link);
            } else {
                Map<String, Object> dataMap = JsonUtil.stringToMap(entity.getFlowFormContentJson());
                result = this.info(dataMap, formAllModel, convert);
            }
            return result;
        } catch (Exception e) {
            throw new WorkFlowException("查询异常,请自行排查");
        }
    }

    /**
     * 有表详情
     **/
    private Map<String, Object> tableInfo(FlowTaskEntity entity, List<FlowTableModel> tableList, List<FormAllModel> formAllModel, boolean convert, DbLinkEntity dbLinkEntity) throws SQLException {
        //处理好的数据
        Map<String, Object> result = new HashMap<>(16);
        try {
            List<OrganizeEntity> orgMapList = organizeService.getOrgRedisList();
            List<UserEntity> allUser = userService.getList();
            List<PositionEntity> mapList = positionService.getPosRedisList();
            List<DictionaryDataEntity> list = dictionaryDataService.getList();
            List<FormAllModel> mastForm = formAllModel.stream().filter(t -> FormEnum.mast.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
            List<FormAllModel> tableForm = formAllModel.stream().filter(t -> FormEnum.table.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
            String mainId = entity.getId();
            @Cleanup Connection conn = this.getTableConn(dbLinkEntity);
            String mastTableName = tableList.stream().filter(t -> "1".equals(t.getTypeId())).findFirst().get().getTable();
            List<String> mastFile = mastForm.stream().map(t -> t.getFormColumnModel().getFieLdsModel().getVModel()).collect(Collectors.toList());
            String pKeyName = this.getKey(conn, mastTableName);
            //主表数据
            String mastInfo = " select " + String.join(",", mastFile) + " from " + mastTableName + " where " + pKeyName + " = '" + mainId + "'";
            Map<String, Object> mastData = getMast(conn, mastInfo);
            for (String key : mastData.keySet()) {
                FormAllModel model = mastForm.stream().filter(t -> key.equals(t.getFormColumnModel().getFieLdsModel().getVModel())).findFirst().orElse(null);
                if (model != null) {
                    FieLdsModel fieLdsModel = model.getFormColumnModel().getFieLdsModel();
                    String format = fieLdsModel.getFormat();
                    String jnpfkey = fieLdsModel.getConfig().getJnpfKey();
                    Object data = mastData.get(key);
                    data = this.info(jnpfkey, data, orgMapList, allUser, mapList);
                    data = this.infoTable(jnpfkey, data, format);
                    if (convert) {
                        data = this.converData(fieLdsModel, data, list, orgMapList, allUser, mapList);
                    }
                    result.put(key, data);
                }
            }
            //子表数据
            List<FlowTableModel> tableListAll = tableList.stream().filter(t -> !"1".equals(t.getTypeId())).collect(Collectors.toList());
            for (FlowTableModel tableModel : tableListAll) {
                String tableName = tableModel.getTable();
                FormAllModel childModel = tableForm.stream().filter(t -> tableName.equals(t.getChildList().getTableName())).findFirst().orElse(null);
                if (childModel != null) {
                    String childKey = childModel.getChildList().getTableModel();
                    List<String> childFile = childModel.getChildList().getChildList().stream().map(t -> t.getFieLdsModel().getVModel()).collect(Collectors.toList());
                    String tableInfo = "select " + String.join(",", childFile) + " from " + tableName + " where " + tableModel.getTableField() + "='" + mainId + "'";
                    List<Map<String, Object>> tableData = getTableList(conn, tableInfo);
                    List<Map<String, Object>> childdataAll = new ArrayList<>();
                    for (Map<String, Object> data : tableData) {
                        Map<String, Object> tablValue = new HashMap<>(16);
                        for (String key : data.keySet()) {
                            FormColumnModel columnModel = childModel.getChildList().getChildList().stream().filter(t -> key.equals(t.getFieLdsModel().getVModel())).findFirst().orElse(null);
                            if (columnModel != null) {
                                FieLdsModel fieLdsModel = columnModel.getFieLdsModel();
                                String format = fieLdsModel.getFormat();
                                String jnpfkey = fieLdsModel.getConfig().getJnpfKey();
                                Object childValue = data.get(key);
                                childValue = this.info(jnpfkey, childValue, orgMapList, allUser, mapList);
                                childValue = this.infoTable(jnpfkey, childValue, format);
                                if (convert) {
                                    childValue = this.converData(fieLdsModel, childValue, list, orgMapList, allUser, mapList);
                                }
                                tablValue.put(key, childValue);
                            }
                        }
                        childdataAll.add(tablValue);
                    }
                    result.put(childKey, childdataAll);
                }
            }
        } catch (Exception e) {
            throw new SQLException(e.getMessage());
        }
        return result;
    }

    /**
     * 无表详情
     **/
    private Map<String, Object> info(Map<String, Object> dataMap, List<FormAllModel> formAllModel, boolean convert) {
        //处理好的数据
        Map<String, Object> result = new HashMap<>(16);
        List<OrganizeEntity> orgMapList = organizeService.getOrgRedisList();
        List<UserEntity> allUser = userService.getList();
        List<PositionEntity> mapList = positionService.getPosRedisList();
        List<DictionaryDataEntity> list = dictionaryDataService.getList();
        List<FormAllModel> mastForm = formAllModel.stream().filter(t -> FormEnum.mast.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        List<FormAllModel> tableForm = formAllModel.stream().filter(t -> FormEnum.table.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        for (String key : dataMap.keySet()) {
            FormAllModel model = mastForm.stream().filter(t -> key.equals(t.getFormColumnModel().getFieLdsModel().getVModel())).findFirst().orElse(null);
            if (model != null) {
                FieLdsModel fieLdsModel = model.getFormColumnModel().getFieLdsModel();
                String jnpfkey = fieLdsModel.getConfig().getJnpfKey();
                Object data = dataMap.get(key);
                data = this.info(jnpfkey, data, orgMapList, allUser, mapList);
                if (convert) {
                    data = this.converData(fieLdsModel, data, list, orgMapList, allUser, mapList);
                }
                result.put(key, data);
            } else {
                FormAllModel childModel = tableForm.stream().filter(t -> key.equals(t.getChildList().getTableModel())).findFirst().orElse(null);
                if (childModel != null) {
                    String childKeyName = childModel.getChildList().getTableModel();
                    List<Map<String, Object>> childDataMap = (List<Map<String, Object>>) dataMap.get(key);
                    List<Map<String, Object>> childdataAll = new ArrayList<>();
                    for (Map<String, Object> child : childDataMap) {
                        Map<String, Object> tablValue = new HashMap<>(16);
                        for (String childKey : child.keySet()) {
                            FormColumnModel columnModel = childModel.getChildList().getChildList().stream().filter(t -> childKey.equals(t.getFieLdsModel().getVModel())).findFirst().orElse(null);
                            if (columnModel != null) {
                                FieLdsModel fieLdsModel = columnModel.getFieLdsModel();
                                String jnpfkey = fieLdsModel.getConfig().getJnpfKey();
                                Object childValue = child.get(childKey);
                                childValue = this.info(jnpfkey, childValue, orgMapList, allUser, mapList);
                                if (convert) {
                                    childValue = this.converData(fieLdsModel, childValue, list, orgMapList, allUser, mapList);
                                }
                                tablValue.put(childKey, childValue);
                            }
                        }
                        childdataAll.add(tablValue);
                    }
                    result.put(childKeyName, childdataAll);
                }
            }
        }
        return result;
    }

    /**
     * 详情转换成中文
     **/
    private Object converData(FieLdsModel fieLdsModel, Object dataValue, List<DictionaryDataEntity> list, List<OrganizeEntity> orgMapList, List<UserEntity> allUser, List<PositionEntity> mapList) {
        Object value = dataValue;
        ConfigModel config = fieLdsModel.getConfig();
        String jnpfKey = config.getJnpfKey();
        List<String> dataAll = new ArrayList<>();
        if (JnpfKeyConsts.RADIO.equals(jnpfKey) || JnpfKeyConsts.SELECT.equals(jnpfKey) || JnpfKeyConsts.CHECKBOX.equals(jnpfKey) || JnpfKeyConsts.CASCADER.equals(jnpfKey) || JnpfKeyConsts.TREESELECT.equals(jnpfKey)) {
            this.routine(fieLdsModel, dataValue, list, dataAll);
            value = String.join(",", dataAll);
        } else if (JnpfKeyConsts.ADDRESS.equals(jnpfKey) || JnpfKeyConsts.USERSELECT.equals(jnpfKey) || JnpfKeyConsts.POSSELECT.equals(jnpfKey) || JnpfKeyConsts.COMSELECT.equals(jnpfKey) || JnpfKeyConsts.DEPSELECT.equals(jnpfKey)) {
            this.system(fieLdsModel, dataValue, orgMapList, allUser, mapList, dataAll);
            value = String.join(",", dataAll);
        } else if (JnpfKeyConsts.DATE.equals(jnpfKey)) {
            if (value instanceof Long || value instanceof Integer) {
                String format = fieLdsModel.getFormat();
                Date date = new Date((Long) value);
                value = DateUtil.getDateString(date, format);
            }
        }
        return value;
    }


    /**
     * 修改有表赋值
     **/
    private Object infoTable(String jnpfKey, Object dataValue, String format) {
        Object value = dataValue;
        switch (jnpfKey) {
            case JnpfKeyConsts.UPLOADFZ:
            case JnpfKeyConsts.UPLOADIMG:
                value = JsonUtil.getJsonToListMap(String.valueOf(value));
                break;
            case JnpfKeyConsts.CHECKBOX:
            case JnpfKeyConsts.ADDRESS:
            case JnpfKeyConsts.DATERANGE:
            case JnpfKeyConsts.TIMERANGE:
            case JnpfKeyConsts.CASCADER:
                value = JsonUtil.getJsonToList(String.valueOf(value), String.class);
                break;
            case JnpfKeyConsts.DATE:
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(format);
                    value = sdf.parse(String.valueOf(value)).getTime();
                } catch (Exception e) {
                    value = dataValue;
                }
                break;
            case JnpfKeyConsts.SLIDER:
            case JnpfKeyConsts.SWITCH:
                try {
                    value = Integer.valueOf(String.valueOf(value));
                } catch (Exception e) {
                    value = dataValue;
                }
                break;
            case JnpfKeyConsts.CREATETIME:
            case JnpfKeyConsts.MODIFYTIME:
                if (!Objects.isNull(dataValue)) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = sdf.parse(String.valueOf(dataValue));
                        value = DateUtil.dateFormat(date);
                        System.out.println(date);
                    } catch (Exception e) {
                        value = dataValue;
                    }
                }
                break;
            default:
                break;
        }
        return value;
    }

    /**
     * 修改系统赋值
     **/
    private Object info(String jnpfKey, Object dataValue, List<OrganizeEntity> orgMapList, List<UserEntity> allUser, List<PositionEntity> mapList) {
        Object value = dataValue;
        switch (jnpfKey) {
            case JnpfKeyConsts.CURRORGANIZE:
            case JnpfKeyConsts.CURRDEPT:
                if (StringUtil.isNotEmpty(String.valueOf(dataValue))) {
                    OrganizeEntity organizeEntity = orgMapList.stream().filter(t -> t.getId().equals(String.valueOf(dataValue))).findFirst().orElse(null);
                    if (organizeEntity != null) {
                        value = organizeEntity.getFullName();
                    }
                }
                break;
            case JnpfKeyConsts.CREATEUSER:
            case JnpfKeyConsts.MODIFYUSER:
                if (StringUtil.isNotEmpty(String.valueOf(dataValue))) {
                    UserEntity userAllModel = allUser.stream().filter(t -> t.getId().equals(String.valueOf(dataValue))).findFirst().orElse(null);
                    if (userAllModel != null) {
                        value = userAllModel.getRealName();
                    }
                }
                break;
            case JnpfKeyConsts.CURRPOSITION:
                if (StringUtil.isNotEmpty(String.valueOf(dataValue))) {
                    PositionEntity positionEntity = mapList.stream().filter(t -> t.getId().equals(String.valueOf(dataValue))).findFirst().orElse(null);
                    if (positionEntity != null) {
                        value = positionEntity.getFullName();
                    }
                }
                break;
            default:
                break;
        }
        return value;
    }

    /**
     * 常规控件
     */
    private void routine(FieLdsModel fieLdsModel, Object dataValue, List<DictionaryDataEntity> list, List<String> dataAll) {
        ConfigModel config = fieLdsModel.getConfig();
        String dataType = config.getDataType();
        String jnpfKey = config.getJnpfKey();
        String props = "";
        if (JnpfKeyConsts.CASCADER.equals(jnpfKey) || JnpfKeyConsts.TREESELECT.equals(jnpfKey)) {
            props = fieLdsModel.getProps().getProps();
        } else {
            props = JsonUtilEx.getObjectToString(fieLdsModel.getConfig().getProps());
        }
        PropsBeanModel pro = JsonUtil.getJsonToBean(props, PropsBeanModel.class);
        String proFullName = pro.getLabel();
        String proId = pro.getValue();
        String proChildren = pro.getChildren();
        List<String> box = new ArrayList<>();
        if (dataValue instanceof String) {
            box = Arrays.asList((String.valueOf(dataValue)).split(","));
        } else if (dataValue instanceof List) {
            if (JnpfKeyConsts.CASCADER.equals(jnpfKey) && pro.getMultiple()) {
                List<List<String>> dataAlls = (List<List<String>>) dataValue;
                for (List data : dataAlls) {
                    box.addAll(data);
                }
            } else {
                box = (List<String>) dataValue;
            }
        }
        //获取list数据
        if (DataTypeConst.DICTIONARY.equals(dataType)) {
            for (String id : box) {
                List<String> name = list.stream().filter(t -> t.getId().equals(id) && StringUtil.isNotEmpty(t.getFullName())).map(t -> t.getFullName()).collect(Collectors.toList());
                dataAll.addAll(name);
            }
        } else if (DataTypeConst.STATIC.equals(dataType)) {
            List<Map<String, Object>> staticList = new ArrayList<>();
            if (JnpfKeyConsts.CASCADER.equals(jnpfKey) || JnpfKeyConsts.TREESELECT.equals(jnpfKey)) {
                staticList = JsonUtil.getJsonToListMap(fieLdsModel.getOptions());
            } else {
                staticList = JsonUtil.getJsonToListMap(fieLdsModel.getSlot().getOptions());
            }
            if (JnpfKeyConsts.CASCADER.equals(jnpfKey) || JnpfKeyConsts.TREESELECT.equals(jnpfKey)) {
                JSONArray data = JsonUtil.getListToJsonArray(staticList);
                staticList = new ArrayList<>();
                treeToList(proId, proFullName, proChildren, data, staticList);
            }
            for (String id : box) {
                List<String> name = staticList.stream().filter(t -> String.valueOf(id).equals(t.get(proId)) && ObjectUtils.isNotEmpty(t.get(proFullName))).map(t -> String.valueOf(t.get(proFullName))).collect(Collectors.toList());
                dataAll.addAll(name);
            }
        } else if (DataTypeConst.DYNAMIC.equals(dataType)) {
            String dynId = config.getPropsUrl();
            //获取远端数据
            Object object = dataInterfaceService.infoToId(dynId);
            Map<String, Object> dynamicMap = JsonUtil.entityToMap(object);
            String dataJson = "data";
            if (dynamicMap.get(dataJson) != null) {
                List<Map<String, Object>> dataList = JsonUtil.getJsonToListMap(dynamicMap.get("data").toString());
                if (JnpfKeyConsts.CASCADER.equals(jnpfKey) || JnpfKeyConsts.TREESELECT.equals(jnpfKey)) {
                    JSONArray data = JsonUtil.getListToJsonArray(dataList);
                    dataList = new ArrayList<>();
                    treeToList(proId, proFullName, proChildren, data, dataList);
                }
                for (String id : box) {
                    List<String> name = dataList.stream().filter(t -> String.valueOf(id).equals(t.get(proId)) && ObjectUtils.isNotEmpty(t.get(proFullName))).map(t -> String.valueOf(t.get(proFullName))).collect(Collectors.toList());
                    dataAll.addAll(name);
                }
            }
        }
    }

    /**
     * 系统控件
     */
    private void system(FieLdsModel fieLdsModel, Object dataValue, List<OrganizeEntity> orgMapList, List<UserEntity> allUser, List<PositionEntity> mapList, List<String> dataAll) {
        ConfigModel config = fieLdsModel.getConfig();
        String jnpfKey = config.getJnpfKey();
        if (JnpfKeyConsts.ADDRESS.equals(jnpfKey)) {
            List<String> box = (List<String>) dataValue;
            if (box != null) {
                List<ProvinceEntity> data = provinceService.getAllList();
                for (String id : box) {
                    List<String> name = data.stream().filter(t -> t.getId().equals(id) && StringUtil.isNotEmpty(t.getFullName())).map(t -> t.getFullName()).collect(Collectors.toList());
                    dataAll.addAll(name);
                }
            }
        } else if (JnpfKeyConsts.USERSELECT.equals(jnpfKey)) {
            List<String> box = Arrays.asList((String.valueOf(dataValue)).split(","));
            for (String id : box) {
                List<String> name = allUser.stream().filter(t -> t.getId().equals(id) && StringUtil.isNotEmpty(t.getRealName())).map(t -> t.getRealName()).collect(Collectors.toList());
                dataAll.addAll(name);
            }
        } else if (JnpfKeyConsts.POSSELECT.equals(jnpfKey)) {
            List<String> box = Arrays.asList((String.valueOf(dataValue)).split(","));
            for (String id : box) {
                List<String> name = mapList.stream().filter(t -> t.getId().equals(id) && StringUtil.isNotEmpty(t.getFullName())).map(t -> t.getFullName()).collect(Collectors.toList());
                dataAll.addAll(name);
            }
        } else if (JnpfKeyConsts.COMSELECT.equals(jnpfKey) || JnpfKeyConsts.DEPSELECT.equals(jnpfKey)) {
            List<String> box = Arrays.asList((String.valueOf(dataValue)).split(","));
            for (String id : box) {
                List<String> name = orgMapList.stream().filter(t -> t.getId().equals(id) && StringUtil.isNotEmpty(t.getFullName())).map(t -> t.getFullName()).collect(Collectors.toList());
                dataAll.addAll(name);
            }
        }
    }

    /**
     * 树转成list
     **/
    private static void treeToList(String id, String fullName, String children, JSONArray data, List<Map<String, Object>> result) {
        for (int i = 0; i < data.size(); i++) {
            JSONObject ob = data.getJSONObject(i);
            Map<String, Object> tree = new HashMap<>(16);
            tree.put(id, String.valueOf(ob.get(id)));
            tree.put(fullName, (String.valueOf(ob.get(fullName))));
            result.add(tree);
            if (ob.get(children) != null) {
                JSONArray childArray = ob.getJSONArray(children);
                treeToList(id, fullName, children, childArray, result);
            }
        }
    }

    //--------------------------------------------修改-----------------------------------------------------

    /**
     * 修改数据处理
     **/
    public Map<String, Object> update(Map<String, Object> allDataMap, List<FieLdsModel> fieLdsModelList, List<FlowTableModel> tableModelList, String mainId, DbLinkEntity link) throws WorkFlowException {
        try {
            List<FormAllModel> formAllModel = new ArrayList<>();
            //递归遍历模板
            FormCloumnUtil.recursionForm(fieLdsModelList, formAllModel);
            //处理好的数据
            Map<String, Object> result = new HashMap<>(16);
            if (tableModelList.size() > 0) {
                result = this.tableUpdate(allDataMap, formAllModel, tableModelList, mainId, link);
            } else {
                result = this.update(allDataMap, formAllModel);
            }
            return result;
        } catch (Exception e) {
            throw new WorkFlowException("修改异常,请自行排查");
        }
    }

    /**
     * 修改有表数据
     **/
    private Map<String, Object> tableUpdate(Map<String, Object> allDataMap, List<FormAllModel> formAllModel,
                                            List<FlowTableModel> tableModelList, String mainId, DbLinkEntity link) throws SQLException {
        //处理好的数据
        Map<String, Object> result = new HashMap<>(16);
        boolean isOracle = DbTypeUtil.checkDb(dataSourceUtil, DbOracle.DB_ENCODE);
        if (link != null) {
            isOracle = DbTypeUtil.checkDb(link, DbOracle.DB_ENCODE);
        }
        //系统数据
        List<UserEntity> userList = userService.getList();
        List<OrganizeEntity> orgMapList = organizeService.getOrgRedisList();
        List<PositionEntity> mapList = positionService.getPosRedisList();
        @Cleanup Connection conn = this.getTableConn(link);
        conn.setAutoCommit(false);
        String mastTableName = tableModelList.stream().filter(t -> "1".equals(t.getTypeId())).findFirst().get().getTable();
        String pKeyName = this.getKey(conn, mastTableName);
        List<FormAllModel> mastForm = formAllModel.stream().filter(t -> FormEnum.mast.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        List<FormAllModel> tableForm = formAllModel.stream().filter(t -> FormEnum.table.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        //主表的语句
        StringBuffer mastSql = new StringBuffer("INSERT INTO " + mastTableName + " ");
        StringJoiner mastFile = new StringJoiner(",");
        mastFile.add(pKeyName);
        StringJoiner mastFileValue = new StringJoiner(",");
        mastFileValue.add("?");
        List<Object> mastValue = new LinkedList<>();
        for (String key : allDataMap.keySet()) {
            FormAllModel model = mastForm.stream().filter(t -> key.equals(t.getFormColumnModel().getFieLdsModel().getVModel())).findFirst().orElse(null);
            if (model != null) {
                FieLdsModel fieLdsModel = model.getFormColumnModel().getFieLdsModel();
                String jnpfkey = fieLdsModel.getConfig().getJnpfKey();
                String format = "yyyy-MM-dd HH:mm:ss";
                Object data = allDataMap.get(key);
                //处理字段
                String file = "?";
                //添加字段
                mastFile.add(key);
                //处理系统自动生成
                data = this.update(fieLdsModel, data, orgMapList, userList, mapList);
                data = this.temp(jnpfkey, data, format);
                mastValue.add(data);
                if (isOracle) {
                    if (JnpfKeyConsts.DATE.equals(jnpfkey) || JnpfKeyConsts.MODIFYTIME.equals(jnpfkey) || JnpfKeyConsts.CREATETIME.equals(jnpfkey)) {
                        file = "to_date(?,'yyyy-mm-dd HH24:mi:ss')";
                    }
                }
                mastFileValue.add(file);
                result.put(key, data);
            } else {
                FormAllModel childModel = tableForm.stream().filter(t -> key.equals(t.getChildList().getTableModel())).findFirst().orElse(null);
                if (childModel != null) {
                    Map<String, Object> childData = this.childTableUpdate(allDataMap, conn, childModel, tableModelList, key, mainId, orgMapList, userList, mapList, isOracle);
                    result.putAll(childData);
                }
            }
        }
        //主表去掉最后
        String mastFileAll = "(" + mastFile + ")";
        String mastFileValueAll = "(" + mastFileValue + ")";
        mastSql.append(mastFileAll + " VALUES " + mastFileValueAll);
        String delSql = "delete from " + mastTableName + " where " + pKeyName + " =?";
        //插入主表数据
        mastSql(mastSql, mastValue, mainId, delSql, conn);
        return result;
    }

    /**
     * 修改子表数据
     **/
    private Map<String, Object> childTableUpdate(Map<String, Object> allDataMap, Connection conn, FormAllModel childModel, List<FlowTableModel> tableModelList, String key, String mainId, List<OrganizeEntity> orgMapList, List<UserEntity> userList, List<PositionEntity> mapList, boolean isOracle) throws SQLException {
        //处理好的子表数据
        Map<String, Object> result = new HashMap<>(16);
        //子表主键
        List<FormColumnModel> childList = childModel.getChildList().getChildList();
        String childTable = childModel.getChildList().getTableName();
        String childKeyName = this.getKey(conn, childTable);
        //关联字段
        String mastKeyName = tableModelList.stream().filter(t -> t.getTable().equals(childTable)).findFirst().get().getTableField();
        StringBuffer childFile = new StringBuffer();
        List<List<Object>> childData = new LinkedList<>();
        List<Map<String, Object>> childDataMap = (List<Map<String, Object>>) allDataMap.get(key);
        //子表处理的数据
        List<Map<String, Object>> childResult = new ArrayList<>();
        //子表的字段
        Map<String, String> child = new HashMap<>(16);
        for (FormColumnModel columnModel : childList) {
            String vmodel = columnModel.getFieLdsModel().getVModel();
            String jnpfKey = columnModel.getFieLdsModel().getConfig().getJnpfKey();
            child.put(vmodel, jnpfKey);
        }
        int num = 0;
        for (Map<String, Object> objectMap : childDataMap) {
            //子表处理的数据
            StringJoiner fileAll = new StringJoiner(",");
            StringJoiner fileValueAll = new StringJoiner(",");
            List<Object> value = new LinkedList<>();
            //子表主键
            value.add(RandomUtil.uuId());
            fileAll.add(childKeyName);
            fileValueAll.add("?");
            //关联字段
            value.add(mainId);
            fileAll.add(mastKeyName);
            fileValueAll.add("?");
            //子表单体处理的数据
            Map<String, Object> childOneResult = new HashMap<>(16);
            for (String childKey : child.keySet()) {
                FormColumnModel columnModel = childList.stream().filter(t -> childKey.equals(t.getFieLdsModel().getVModel())).findFirst().orElse(null);
                if (columnModel != null) {
                    FieLdsModel fieLdsModel = columnModel.getFieLdsModel();
                    String jnpfkey = fieLdsModel.getConfig().getJnpfKey();
                    String format = "yyyy-MM-dd HH:mm:ss";
                    Object data = objectMap.get(childKey);
                    //处理系统自动生成
                    data = this.update(fieLdsModel, data, orgMapList, userList, mapList);
                    data = this.temp(jnpfkey, data, format);
                    //添加字段
                    fileAll.add(childKey);
                    String file = "?";
                    if (isOracle) {
                        if (JnpfKeyConsts.DATE.equals(jnpfkey)) {
                            file = "to_date(?,'yyyy-mm-dd HH24:mi:ss')";
                        }
                    }
                    value.add(data);
                    fileValueAll.add(file);
                    childOneResult.put(childKey, data);
                }
            }
            childResult.add(childOneResult);
            //子表去掉最后
            if (num == 0) {
                String file = "(" + fileAll + ")";
                String fileValue = "(" + fileValueAll + ")";
                //添加单行的数据
                childFile.append(file + " VALUES " + fileValue);
                num++;
            }
            childData.add(value);
        }
        //删除子表
        String delSql = "delete from " + childTable + " where " + mastKeyName + "=?";
        String[] del = new String[]{delSql, mainId};
        //插入子表数据
        this.tableSql(childFile, childData, childTable, del, conn);
        result.put(key, childResult);
        return result;
    }

    /**
     * 修改无表数据
     **/
    private Map<String, Object> update(Map<String, Object> allDataMap, List<FormAllModel> formAllModel) {
        //处理好的数据
        Map<String, Object> result = new HashMap<>(16);
        //系统数据
        List<UserEntity> userList = userService.getList();
        List<OrganizeEntity> orgMapList = organizeService.getOrgRedisList();
        List<PositionEntity> mapList = positionService.getPosRedisList();
        List<FormAllModel> mastForm = formAllModel.stream().filter(t -> FormEnum.mast.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        List<FormAllModel> tableForm = formAllModel.stream().filter(t -> FormEnum.table.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        for (String key : allDataMap.keySet()) {
            FormAllModel model = mastForm.stream().filter(t -> key.equals(t.getFormColumnModel().getFieLdsModel().getVModel())).findFirst().orElse(null);
            if (model != null) {
                FieLdsModel fieLdsModel = model.getFormColumnModel().getFieLdsModel();
                Object data = allDataMap.get(key);
                //处理系统自动生成
                data = this.update(fieLdsModel, data, orgMapList, userList, mapList);
                result.put(key, data);
            } else {
                FormAllModel childModel = tableForm.stream().filter(t -> key.equals(t.getChildList().getTableModel())).findFirst().orElse(null);
                if (childModel != null) {
                    List<Map<String, Object>> childDataMap = (List<Map<String, Object>>) allDataMap.get(key);
                    //子表处理的数据
                    List<Map<String, Object>> childResult = new ArrayList<>();
                    for (Map<String, Object> objectMap : childDataMap) {
                        //子表单体处理的数据
                        Map<String, Object> childOneResult = new HashMap<>(16);
                        for (String childKey : objectMap.keySet()) {
                            FormColumnModel columnModel = childModel.getChildList().getChildList().stream().filter(t -> childKey.equals(t.getFieLdsModel().getVModel())).findFirst().orElse(null);
                            if (columnModel != null) {
                                FieLdsModel fieLdsModel = columnModel.getFieLdsModel();
                                Object data = objectMap.get(childKey);
                                data = this.update(fieLdsModel, data, orgMapList, userList, mapList);
                                childOneResult.put(childKey, data);
                            }
                        }
                        childResult.add(childOneResult);
                    }
                    result.put(key, childResult);
                }
            }
        }
        return result;
    }

    /**
     * 修改系统赋值
     **/
    private Object update(FieLdsModel fieLdsModel, Object dataValue, List<OrganizeEntity> orgMapList, List<UserEntity> userList, List<PositionEntity> mapList) {
        String jnpfkey = fieLdsModel.getConfig().getJnpfKey();
        String rule = fieLdsModel.getConfig().getRule();
        UserInfo userInfo = userProvider.get();
        Object value = dataValue;
        switch (jnpfkey) {
            case JnpfKeyConsts.CURRORGANIZE:
                if (StringUtil.isNotEmpty(String.valueOf(dataValue))) {
                    OrganizeEntity organizeEntity = orgMapList.stream().filter(t -> t.getFullName().equals(String.valueOf(dataValue))).findFirst().orElse(null);
                    if (organizeEntity != null) {
                        value = organizeEntity.getId();
                    }
                } else {
                    value = userInfo.getOrganizeId();
                }
                break;
            case JnpfKeyConsts.CURRDEPT:
                if (StringUtil.isNotEmpty(String.valueOf(dataValue))) {
                    OrganizeEntity organizeEntity = orgMapList.stream().filter(t -> t.getFullName().equals(String.valueOf(dataValue))).findFirst().orElse(null);
                    if (organizeEntity != null) {
                        value = organizeEntity.getId();
                    }
                } else {
                    value = userInfo.getDepartmentId();
                }
                break;
            case JnpfKeyConsts.CREATEUSER:
                if (StringUtil.isNotEmpty(String.valueOf(dataValue))) {
                    UserEntity userEntity = userList.stream().filter(t -> t.getRealName().equals(String.valueOf(dataValue))).findFirst().orElse(null);
                    if (userEntity != null) {
                        value = userEntity.getId();
                    }
                } else {
                    value = userInfo.getUserId();
                }
                break;
            case JnpfKeyConsts.CREATETIME:
                if (StringUtil.isEmpty(String.valueOf(dataValue))) {
                    value = DateUtil.getNow("+8");
                }
                break;
            case JnpfKeyConsts.MODIFYUSER:
                value = userInfo.getUserId();
                break;
            case JnpfKeyConsts.MODIFYTIME:
                value = DateUtil.getNow("+8");
                break;
            case JnpfKeyConsts.CURRPOSITION:
                if (StringUtil.isNotEmpty(String.valueOf(dataValue))) {
                    PositionEntity positionEntity = mapList.stream().filter(t -> t.getFullName().equals(String.valueOf(dataValue))).findFirst().orElse(null);
                    if (positionEntity != null) {
                        value = positionEntity.getId();
                    }
                } else {
                    UserEntity userEntity = userService.getInfo(userInfo.getUserId());
                    PositionEntity positionEntity = positionService.getInfo(userEntity.getPositionId().split(",")[0]);
                    value = positionEntity != null ? positionEntity.getId() : "";
                }
                break;
            case JnpfKeyConsts.BILLRULE:
                if (Objects.isNull(value)) {
                    try {
                        value = billRuleService.getBillNumber(rule, false);
                    } catch (Exception e) {
                        value = dataValue;
                    }
                }
                break;
            default:
                break;
        }
        return value;
    }

    //--------------------------------------------新增-------------------------------------------------------

    /**
     * 新增数据处理
     **/
    public Map<String, Object> create(Map<String, Object> allDataMap, List<FieLdsModel> fieLdsModelList, List<FlowTableModel> tableModelList, String mainId, Map<String, String> billData, DbLinkEntity link) throws WorkFlowException {
        try {
            List<FormAllModel> formAllModel = new ArrayList<>();
            //递归遍历模板
            FormCloumnUtil.recursionForm(fieLdsModelList, formAllModel);
            //处理好的数据
            Map<String, Object> result = new HashMap<>(16);
            if (tableModelList.size() > 0) {
                result = this.tableCreate(allDataMap, formAllModel, tableModelList, mainId, billData, link);
            } else {
                result = this.create(allDataMap, formAllModel, billData);
            }
            return result;
        } catch (SQLException sqlException) {
            log.error("新增异常：{}", sqlException.getMessage());
            throw new WorkFlowException("新增异常,请自行排查");
        }
    }

    /**
     * 有表插入数据
     **/
    private Map<String, Object> tableCreate(Map<String, Object> allDataMap, List<FormAllModel> formAllModel, List<FlowTableModel> tableModelList, String mainId, Map<String, String> billData, DbLinkEntity link) throws SQLException {
        //处理好的数据
        Map<String, Object> result = new HashMap<>(16);
        boolean isOracle = DbTypeUtil.checkDb(dataSourceUtil, DbOracle.DB_ENCODE);
        if (link != null) {
            isOracle = DbTypeUtil.checkDb(link, DbOracle.DB_ENCODE);
        }
        @Cleanup Connection conn = this.getTableConn(link);
        conn.setAutoCommit(false);
        String mastTableName = tableModelList.stream().filter(t -> "1".equals(t.getTypeId())).findFirst().get().getTable();
        String pKeyName = this.getKey(conn, mastTableName);
        List<FormAllModel> mastForm = formAllModel.stream().filter(t -> FormEnum.mast.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        List<FormAllModel> tableForm = formAllModel.stream().filter(t -> FormEnum.table.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        //主表的语句
        StringBuffer mastSql = new StringBuffer("INSERT INTO " + mastTableName + " ");
        StringJoiner mastFile = new StringJoiner(",");
        mastFile.add(pKeyName);
        StringJoiner mastFileValue = new StringJoiner(",");
        mastFileValue.add("?");
        List<Object> mastValue = new LinkedList<>();
        for (String key : allDataMap.keySet()) {
            FormAllModel model = mastForm.stream().filter(t -> key.equals(t.getFormColumnModel().getFieLdsModel().getVModel())).findFirst().orElse(null);
            if (model != null) {
                FieLdsModel fieLdsModel = model.getFormColumnModel().getFieLdsModel();
                String jnpfkey = fieLdsModel.getConfig().getJnpfKey();
                String format = "yyyy-MM-dd HH:mm:ss";
                Object data = allDataMap.get(key);
                //处理字段
                String file = "?";
                //添加字段
                mastFile.add(key);
                //处理系统自动生成
                data = this.create(fieLdsModel, data);
                data = this.temp(jnpfkey, data, format);
                mastValue.add(data);
                if (isOracle) {
                    if (JnpfKeyConsts.DATE.equals(jnpfkey) || JnpfKeyConsts.MODIFYTIME.equals(jnpfkey) || JnpfKeyConsts.CREATETIME.equals(jnpfkey)) {
                        file = "to_date(?,'yyyy-mm-dd HH24:mi:ss')";
                    }
                }
                mastFileValue.add(file);
                result.put(key, data);
            } else {
                Map<String, Object> childData = this.childCreate(allDataMap, conn, tableForm, tableModelList, mainId, key, isOracle);
                result.putAll(childData);
            }
        }
        //主表去掉最后
        String mastFileAll = "(" + mastFile + ")";
        String mastFileValueAll = "(" + mastFileValue + ")";
        mastSql.append(mastFileAll + " VALUES " + mastFileValueAll);
        //插入主表数据
        this.mastSql(mastSql, mastValue, mainId, null, conn);
        return result;
    }

    /**
     * 新增子表数据
     **/
    private Map<String, Object> childCreate(Map<String, Object> allDataMap, Connection conn, List<FormAllModel> tableForm, List<FlowTableModel> tableModelList, String mainId, String key, boolean isOracle) throws SQLException {
        //处理好的数据
        Map<String, Object> result = new HashMap<>(16);
        FormAllModel childModel = tableForm.stream().filter(t -> key.equals(t.getChildList().getTableModel())).findFirst().orElse(null);
        if (childModel != null) {
            //子表主键
            List<FormColumnModel> childList = childModel.getChildList().getChildList();
            String childTable = childModel.getChildList().getTableName();
            String childKeyName = this.getKey(conn, childTable);
            //关联字段
            String mastKeyName = tableModelList.stream().filter(t -> t.getTable().equals(childTable)).findFirst().get().getTableField();
            StringBuffer childFile = new StringBuffer();
            List<List<Object>> childData = new LinkedList<>();
            List<Map<String, Object>> childDataMap = (List<Map<String, Object>>) allDataMap.get(key);
            //子表处理的数据
            List<Map<String, Object>> childResult = new ArrayList<>();
            //子表的字段
            Map<String, String> child = new HashMap<>(16);
            for (FormColumnModel columnModel : childList) {
                String vmodel = columnModel.getFieLdsModel().getVModel();
                String jnpfKey = columnModel.getFieLdsModel().getConfig().getJnpfKey();
                child.put(vmodel, jnpfKey);
            }
            int num = 0;
            for (Map<String, Object> objectMap : childDataMap) {
                //子表处理的数据
                StringJoiner fileAll = new StringJoiner(",");
                StringJoiner fileValueAll = new StringJoiner(",");
                List<Object> value = new LinkedList<>();
                //子表主键
                value.add(RandomUtil.uuId());
                fileAll.add(childKeyName);
                fileValueAll.add("?");
                //关联字段
                value.add(mainId);
                fileAll.add(mastKeyName);
                fileValueAll.add("?");
                //子表单体处理的数据
                Map<String, Object> childOneResult = new HashMap<>(16);
                for (String childKey : child.keySet()) {
                    FormColumnModel columnModel = childList.stream().filter(t -> childKey.equals(t.getFieLdsModel().getVModel())).findFirst().orElse(null);
                    if (columnModel != null) {
                        FieLdsModel fieLdsModel = columnModel.getFieLdsModel();
                        String jnpfkey = fieLdsModel.getConfig().getJnpfKey();
                        String format = "yyyy-MM-dd HH:mm:ss";
                        Object data = objectMap.get(childKey);
                        //处理系统自动生成
                        data = this.create(fieLdsModel, data);
                        data = this.temp(jnpfkey, data, format);
                        //添加字段
                        fileAll.add(childKey);
                        String file = "?";
                        if (isOracle) {
                            if (JnpfKeyConsts.DATE.equals(jnpfkey)) {
                                file = "to_date(?,'yyyy-mm-dd HH24:mi:ss')";
                            }
                        }
                        value.add(data);
                        fileValueAll.add(file);
                        childOneResult.put(childKey, data);
                    }
                }
                childResult.add(childOneResult);
                //子表去掉最后
                if (num == 0) {
                    String file = "(" + fileAll + ")";
                    String fileValue = "(" + fileValueAll + ")";
                    //添加单行的数据
                    childFile.append(file + " VALUES " + fileValue);
                    num++;
                }
                childData.add(value);
            }
            String[] delSql = new String[]{};
            //插入子表数据
            this.tableSql(childFile, childData, childTable, delSql, conn);
            result.put(key, childResult);
        }
        return result;
    }

    /**
     * 无表插入数据
     **/
    private Map<String, Object> create(Map<String, Object> allDataMap, List<FormAllModel> formAllModel, Map<String, String> billData) {
        //处理好的数据
        Map<String, Object> result = new HashMap<>(16);
        List<FormAllModel> mastForm = formAllModel.stream().filter(t -> FormEnum.mast.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        List<FormAllModel> tableForm = formAllModel.stream().filter(t -> FormEnum.table.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        for (String key : allDataMap.keySet()) {
            FormAllModel model = mastForm.stream().filter(t -> key.equals(t.getFormColumnModel().getFieLdsModel().getVModel())).findFirst().orElse(null);
            if (model != null) {
                FieLdsModel fieLdsModel = model.getFormColumnModel().getFieLdsModel();
                Object data = allDataMap.get(key);
                //处理系统自动生成
                data = this.create(fieLdsModel, data);
                result.put(key, data);
            } else {
                FormAllModel childModel = tableForm.stream().filter(t -> key.equals(t.getChildList().getTableModel())).findFirst().orElse(null);
                if (childModel != null) {
                    //子表主键
                    List<FormColumnModel> childList = childModel.getChildList().getChildList();
                    List<Map<String, Object>> childDataMap = (List<Map<String, Object>>) allDataMap.get(key);
                    //子表处理的数据
                    List<Map<String, Object>> childResult = new ArrayList<>();
                    for (Map<String, Object> objectMap : childDataMap) {
                        //子表单体处理的数据
                        Map<String, Object> childOneResult = new HashMap<>(16);
                        for (String childKey : objectMap.keySet()) {
                            FormColumnModel columnModel = childList.stream().filter(t -> childKey.equals(t.getFieLdsModel().getVModel())).findFirst().orElse(null);
                            if (columnModel != null) {
                                FieLdsModel fieLdsModel = columnModel.getFieLdsModel();
                                Object data = objectMap.get(childKey);
                                //处理系统自动生成
                                data = this.create(fieLdsModel, data);
                                childOneResult.put(childKey, data);
                            }
                        }
                        childResult.add(childOneResult);
                    }
                    result.put(key, childResult);
                }
            }
        }
        return result;
    }

    /**
     * 子表插入数据
     **/
    private void tableSql(StringBuffer childFile, List<List<Object>> childData, String childTable, String[] del, Connection conn) throws SQLException {
        if (del.length > 0) {
            PreparedStatement delete = conn.prepareStatement(del[0]);
            delete.setObject(1, del[1]);
            delete.addBatch();
            delete.executeBatch();
        }
        for (int i = 0; i < childData.size(); i++) {
            List<Object> dataAll = childData.get(i);
            boolean result = dataAll.size() > 2;
            String sql = "INSERT INTO " + childTable + " " + childFile.toString();
            PreparedStatement save = conn.prepareStatement(sql);
            if (result) {
                int num = 1;
                for (Object data : dataAll) {
                    save.setObject(num, data);
                    num++;
                }
                save.addBatch();
            }
            save.executeBatch();
        }
    }

    /**
     * 主表插入语句
     **/
    private void mastSql(StringBuffer mastSql, List<Object> mastValue, String mainId, String delteSql, Connection conn) throws SQLException {
        try {
            if (StringUtil.isNotEmpty(delteSql)) {
                PreparedStatement delete = conn.prepareStatement(delteSql);
                delete.setObject(1, mainId);
                delete.addBatch();
                delete.executeBatch();
            }
            PreparedStatement save = conn.prepareStatement(mastSql.toString());
            int num = 1;
            save.setObject(num, mainId);
            num++;
            for (Object data : mastValue) {
                save.setObject(num, data);
                num++;
            }
            save.addBatch();
            save.executeBatch();
            conn.commit();
        }catch (SQLException e){
            conn.rollback();
            throw new SQLException(e.getMessage());
        }

    }

    /**
     * 新增系统赋值
     **/
    private Object create(FieLdsModel fieLdsModel, Object dataValue) {
        String jnpfKey = fieLdsModel.getConfig().getJnpfKey();
        String rule = fieLdsModel.getConfig().getRule();
        UserInfo userInfo = userProvider.get();
        Object value = dataValue;
        switch (jnpfKey) {
            case JnpfKeyConsts.CREATEUSER:
                value = userInfo.getUserId();
                break;
            case JnpfKeyConsts.CREATETIME:
                value = DateUtil.getNow("+8");
                break;
            case JnpfKeyConsts.CURRORGANIZE:
                value = userInfo.getOrganizeId();
                break;
            case JnpfKeyConsts.CURRDEPT:
                value = userInfo.getDepartmentId();
                break;
            case JnpfKeyConsts.MODIFYTIME:
                value = null;
                break;
            case JnpfKeyConsts.MODIFYUSER:
                value = null;
                break;
            case JnpfKeyConsts.CURRPOSITION:
                UserEntity userEntity = userService.getInfo(userInfo.getUserId());
                PositionEntity positionEntity = positionService.getInfo(userEntity.getPositionId().split(",")[0]);
                value = positionEntity != null ? positionEntity.getId() : "";
                break;
            case JnpfKeyConsts.BILLRULE:
                try {
                    value = billRuleService.getBillNumber(rule, false);
                } catch (Exception e) {
                    value = null;
                }
                break;
            default:
                break;
        }
        return value;
    }

    /**
     * 转换时间和其他的类型
     **/
    private Object temp(String jnpfKey, Object dataValue, String format) {
        if (JnpfKeyConsts.DATE.equals(jnpfKey)) {
            if (dataValue != null) {
                dataValue = DateUtil.dateToString(new Date(Long.valueOf(String.valueOf(dataValue))), format);
            }
        } else if (dataValue != null) {
            dataValue = String.valueOf(dataValue);
        }
        return dataValue;
    }

}
