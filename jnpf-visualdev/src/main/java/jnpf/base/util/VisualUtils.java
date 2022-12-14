package jnpf.base.util;


import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import jnpf.base.UserInfo;
import jnpf.database.model.DataSourceUtil;
import jnpf.database.model.DbLinkEntity;
import jnpf.config.ConfigValueUtil;
import jnpf.base.entity.VisualdevEntity;
import jnpf.database.source.impl.*;
import jnpf.database.util.DbTypeUtil;
import jnpf.database.util.JdbcUtil;
import jnpf.engine.entity.FlowEngineEntity;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.service.FlowEngineService;
import jnpf.engine.service.FlowTaskService;
import jnpf.exception.WorkFlowException;
import jnpf.engine.util.ModelUtil;
import jnpf.model.FormAllModel;
import jnpf.model.FormEnum;
import jnpf.model.visiual.*;
import jnpf.onlinedev.entity.VisualdevModelDataEntity;
import jnpf.database.exception.DataException;
import jnpf.base.vo.DownloadVO;
import jnpf.model.visiual.fields.FieLdsModel;
import jnpf.model.visiual.fields.config.ConfigModel;
import jnpf.model.visiual.fields.props.PropsBeanModel;
import jnpf.model.visiual.DataTypeConst;
import jnpf.onlinedev.model.OnlineDevListModel.VisualColumnSearchVO;
import jnpf.util.*;
import jnpf.util.JsonUtil;
import jnpf.util.context.SpringContext;
import lombok.Cleanup;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 可视化工具类
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年3月13日16:37:40
 */
public class VisualUtils {


    private static DataSourceUtil dataSourceUtil;
    private static ConfigValueUtil configValueUtil;
    private static UserProvider userProvider;

    /**
     * 初始化
     */
    public static void init() {
        configValueUtil = SpringContext.getBean(ConfigValueUtil.class);
        userProvider = SpringContext.getBean(UserProvider.class);
        dataSourceUtil = SpringContext.getBean(DataSourceUtil.class);
    }


    /**
     * 去除无意义控件，并且字段转小写
     *
     * @return
     */
    public static List<FieLdsModel> deleteVmodel(List<FieLdsModel> modelList) {
        List<FieLdsModel> newModelList = new ArrayList<>();
        for (FieLdsModel model : modelList) {
            if (JnpfKeyConsts.CHILD_TABLE.equals(model.getConfig().getJnpfKey())) {
                List<FieLdsModel> childModelList = JsonUtil.getJsonToList(model.getConfig().getChildren(), FieLdsModel.class);
                List<FieLdsModel> newchildModelList = new ArrayList<>();
                for (FieLdsModel childModel : childModelList) {
                    if (StringUtil.isNotEmpty(childModel.getVModel())) {
                        newchildModelList.add(childModel);
                    }
                }
                model.getConfig().setChildren(newchildModelList);
                newModelList.add(model);
            } else {
                if (StringUtil.isNotEmpty(model.getVModel())) {
                    newModelList.add(model);
                }
            }
        }
        return newModelList;
    }

    /**
     * 去除多级嵌套控件
     *
     * @return
     */
    public static List<FieLdsModel> deleteMoreVmodel(FieLdsModel model) {
        if ("".equals(model.getVModel()) && model.getConfig().getChildren() != null) {
            List<FieLdsModel> childModelList = JsonUtil.getJsonToList(model.getConfig().getChildren(), FieLdsModel.class);
            return childModelList;
        }
        return null;
    }

    public static List<FieLdsModel> deleteMore(List<FieLdsModel> modelList) {
        List<FieLdsModel> newModelList = new ArrayList<>();
        for (FieLdsModel model : modelList) {
            List<FieLdsModel> newList = deleteMoreVmodel(model);
            if (newList == null || JnpfKeyConsts.CHILD_TABLE.equals(model.getConfig().getJnpfKey())) {
                newModelList.add(model);
            } else {
                newModelList.addAll(deleteMore(newList));
            }
        }
        return newModelList;
    }

    /**
     * 获取有表列表数据
     *
     * @param conn
     * @param sql
     * @param pKeyName
     * @return
     * @throws DataException
     */
    public static List<VisualdevModelDataEntity> getTableDataList(Connection conn, String sql, String pKeyName) throws SQLException {
        List<VisualdevModelDataEntity> list = new ArrayList<>();
        ResultSet rs = JdbcUtil.query(conn, sql);
        List<Map<String, Object>> dataList = JdbcUtil.convertList(rs);
        for (Map<String, Object> dataMap : dataList) {
            VisualdevModelDataEntity dataEntity = new VisualdevModelDataEntity();
            dataMap = toLowerKey(dataMap);
            dataEntity.setData(JsonUtil.getObjectToStringDateFormat(dataMap, "yyyy-MM-dd HH:mm"));
            if (dataMap.containsKey(pKeyName.toUpperCase())) {
                dataEntity.setId(String.valueOf(dataMap.get(pKeyName.toUpperCase())));
            }
            list.add(dataEntity);
        }
        return list;
    }

    /**
     * 判断是否存在主键
     *
     * @param feilds
     * @param pKeyName
     * @return
     */
    public static Boolean existKey(String feilds, String pKeyName) {
        String[] strs = feilds.split(",");
        if (strs.length > 0) {
            for (String feild : strs) {
                if (feild.equals(pKeyName)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 获取不同情况下的插入语句
     *
     * @param table
     * @param feilds
     * @return
     */
    public static String getInsertSql(String table, String feilds, String mainpKeyName, String childPkName, String mainId) {
        StringBuilder insertSql = new StringBuilder();

        feilds = feilds.toLowerCase().trim();
        mainpKeyName = mainpKeyName.toLowerCase();
        childPkName = childPkName.toLowerCase();
        if (existKey(feilds, mainpKeyName) && existKey(feilds, childPkName)) {
            insertSql.append("INSERT INTO " + table + "(" + feilds + ") " + " VALUES (");
        } else if (existKey(feilds, mainpKeyName) && !existKey(feilds, childPkName)) {
            insertSql.append("INSERT INTO " + table + "(" + childPkName + "," + feilds + ") " + " VALUES ('" + RandomUtil.uuId() + "',");
        } else if (!existKey(feilds, mainpKeyName) && existKey(feilds, childPkName)) {
            insertSql.append("INSERT INTO " + table + "(" + mainpKeyName + "," + feilds + ") " + " VALUES ('" + mainId + "',");
        } else {
            insertSql.append("INSERT INTO " + table + "(" + childPkName + "," + mainpKeyName + "," + feilds + ") " + " VALUES ('" + RandomUtil.uuId() + "','" + mainId + "',");
        }
        return insertSql.toString();
    }

    /**
     * 获取不同情况下的插入语句
     *
     * @param baseSql
     * @return
     */
    public static String getRealSql(String baseSql, String mainId) {
        String[] sql = baseSql.split(",");
        String realSql = baseSql;
        if (sql.length == 2) {
            realSql = "('" + RandomUtil.uuId() + "'," + sql[1] + ",";
        } else if (sql.length == 1 && !baseSql.contains(mainId)) {
            realSql = "('" + RandomUtil.uuId() + "',";
        }
        return realSql;
    }


    /**
     * 返回主键名称
     *
     * @param conn
     * @param mainTable
     * @return
     */
    public static String getpKey(Connection conn, String mainTable) throws SQLException {
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


    /**
     * 获取有表单条数据
     * @param sql
     * @return
     * @throws DataException
     */
    public static List<Map<String, Object>> getTableDataInfo(String sql,DbLinkEntity linkEntity) throws SQLException {
       @Cleanup Connection conn=getTableConn();
        if (linkEntity!=null){
            conn = getDataConn(linkEntity);
        }
        ResultSet rs = JdbcUtil.query(conn, sql);
        List<Map<String, Object>> dataList = JdbcUtil.convertList(rs);
        for (Map<String, Object> dataMap : dataList) {
            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                //判断是否是字符串数组时间戳，如果是则转成Json数组时间戳
                if (entry.getValue() != null && String.valueOf(entry.getValue()).contains(",") && !"[\"".equals(String.valueOf(entry.getValue()).substring(0, 2)) && entry.getValue().toString().contains("]")) {
                    JSONArray list = JsonUtil.getJsonToJsonArray(String.valueOf(entry.getValue()));
                    entry.setValue(list);
                }
            }

        }
        return dataList;
    }

    /**
     * 转换有表单条数据格式，子表多条数据
     *
     * @param modelList
     * @param dataMapList
     * @return
     * @throws ParseException
     */
    public static List<Map<String, Object>> swapTableDataInfoList(List<FieLdsModel> modelList, List<Map<String, Object>> dataMapList) throws ParseException {
        List<Map<String, Object>> newDataMapList = new ArrayList<>();
        for (Map<String, Object> dataMap : dataMapList) {
            Map<String, Object> newDataMap = new HashMap<>(16);
            for (FieLdsModel fieLdsModel : modelList) {
                for (Map.Entry<String, Object> entryMap : dataMap.entrySet()) {
                    if (entryMap.getKey().equals(fieLdsModel.getVModel())) {
                        if (JnpfKeyConsts.UPLOADFZ.equals(fieLdsModel.getConfig().getJnpfKey()) || JnpfKeyConsts.UPLOADIMG.equals(fieLdsModel.getConfig().getJnpfKey())) {
                            String value = String.valueOf(entryMap.getValue());
                            if (!"[]".equals(value)) {
                                List<Map<String, Object>> map = JsonUtil.getJsonToListMap(value);
                                newDataMap.put(entryMap.getKey(), map);
                            }
                        } else if ("checkbox".equals(fieLdsModel.getConfig().getJnpfKey())) {
                            List<String> list = JsonUtil.getJsonToList(String.valueOf(entryMap.getValue()), String.class);
                            newDataMap.put(entryMap.getKey(), list);
                        } else if (JnpfKeyConsts.CREATETIME.equals(fieLdsModel.getConfig().getJnpfKey()) || JnpfKeyConsts.MODIFYTIME.equals(fieLdsModel.getConfig().getJnpfKey())) {
                            newDataMap.put(entryMap.getKey(), String.valueOf(entryMap.getValue()));
                        } else if (fieLdsModel.getConfig().getJnpfKey().contains("date") && entryMap.getValue() != null) {
                            if (entryMap.getValue().toString().contains(",")) {
                                List<String> list = JsonUtil.getJsonToList(String.valueOf(entryMap.getValue()), String.class);
                                SimpleDateFormat sdf = new SimpleDateFormat(fieLdsModel.getFormat());
                                List<Object> newList = new ArrayList<>();
                                for (String dateStr : list) {
                                    try {
                                        newList.add(Long.valueOf(dateStr));
                                    } catch (Exception e) {
                                        Long s = sdf.parse(dateStr).getTime();
                                        newList.add(s.toString());
                                    }
                                }
                                newDataMap.put(entryMap.getKey(), newList);
                            } else {
                                SimpleDateFormat sdf = new SimpleDateFormat(fieLdsModel.getFormat());
                                Long s = sdf.parse(entryMap.getValue().toString()).getTime();
                                newDataMap.put(entryMap.getKey(), s);
                            }
                        }//级联控件需要转成数组格式
                        else if (JnpfKeyConsts.CASCADER.equals(fieLdsModel.getConfig().getJnpfKey())){
                                //对关联的相关字段重新赋值
                            if (entryMap.getKey().equals(fieLdsModel.getVModel())){
                                String[] s=new String[0];
                                if (entryMap.getValue()!=null || entryMap.getValue()!=""){
                                    String value = String.valueOf(entryMap.getValue());
                                    value= value.replaceAll("\"","").replace("[","").replace("]","");
                                    s = value.split(",");
                                    entryMap.setValue(s);
                                }
                            }
                            newDataMap.put(entryMap.getKey(), entryMap.getValue());
                        }
                        else {
                            newDataMap.put(entryMap.getKey(), entryMap.getValue());
                        }
                    }
                }
            }
            newDataMapList.add(newDataMap);
        }

        return newDataMapList;
    }


    /**
     * 转换有表数据格式,单条数据
     *
     * @param modelList
     * @param dataMap
     * @return
     * @throws ParseException
     */
    public static Map<String, Object> swapTableDataInfoOne(List<FieLdsModel> modelList, Map<String, Object> dataMap) throws ParseException {
        Map<String, Object> newDataMap = new HashMap<>(16);
        for (FieLdsModel fieLdsModel : modelList) {
            for (Map.Entry<String, Object> entryMap : dataMap.entrySet()) {
                if (entryMap.getKey().equals(fieLdsModel.getVModel())) {
                    if (JnpfKeyConsts.UPLOADFZ.equals(fieLdsModel.getConfig().getJnpfKey()) || JnpfKeyConsts.UPLOADIMG.equals(fieLdsModel.getConfig().getJnpfKey())) {
                        String value = String.valueOf(entryMap.getValue());
                        if (!"[]".equals(value)) {
                            List<Map<String, Object>> map = JsonUtil.getJsonToListMap(value);
                            newDataMap.put(entryMap.getKey(), map);
                        }
                    }
                    else if (JnpfKeyConsts.SWITCH.equals(fieLdsModel.getConfig().getJnpfKey())){
                        newDataMap.put(entryMap.getKey(),  entryMap.getValue()!=null ? Integer.parseInt(String.valueOf(entryMap.getValue())):null);
                    }
                    else if (JnpfKeyConsts.SLIDER.equals(fieLdsModel.getConfig().getJnpfKey())){
                        newDataMap.put(entryMap.getKey(), entryMap.getValue()!=null ? Integer.parseInt(String.valueOf(entryMap.getValue())):null);
                    }
                    else if (JnpfKeyConsts.CHECKBOX.equals(fieLdsModel.getConfig().getJnpfKey())) {
                        List<String> list = JsonUtil.getJsonToList(String.valueOf(entryMap.getValue()), String.class);
                        newDataMap.put(entryMap.getKey(), list);
                    } else if (JnpfKeyConsts.CREATETIME.equals(fieLdsModel.getConfig().getJnpfKey()) || JnpfKeyConsts.MODIFYTIME.equals(fieLdsModel.getConfig().getJnpfKey())) {
                        newDataMap.put(entryMap.getKey(), entryMap.getValue()!=null ?String.valueOf(entryMap.getValue()):"");
                    } else if (fieLdsModel.getConfig().getJnpfKey().contains("date") && entryMap.getValue() != null) {
                        if (String.valueOf(entryMap.getValue()).contains(",")) {
                            List<String> list = JsonUtil.getJsonToList(String.valueOf(entryMap.getValue()), String.class);
                            List<String> newList = new ArrayList<>();
                            SimpleDateFormat sdf = new SimpleDateFormat(fieLdsModel.getFormat());
                            for (String dateStr : list) {
                                try {
                                    Long.valueOf(dateStr);
                                } catch (Exception e) {
                                    Long s = sdf.parse(dateStr).getTime();
                                    newList.add(s.toString());
                                }
                            }
                            newDataMap.put(entryMap.getKey(), newList);
                        } else {
                            String fieldFormat = fieLdsModel.getFormat()!=null ? fieLdsModel.getFormat() : fieLdsModel.getType()==JnpfKeyConsts.DATE ? "yyyy-MM-dd":"yyyy-MM-dd HH:mm:ss";
                            SimpleDateFormat sdf = new SimpleDateFormat(fieldFormat);
                            Long s = sdf.parse(entryMap.getValue().toString()).getTime();
                            newDataMap.put(entryMap.getKey(), s);
                        }
                    } //级联控件需要转成数组格式
                    else if (JnpfKeyConsts.CASCADER.equals(fieLdsModel.getConfig().getJnpfKey())){
                        //对关联的相关字段重新赋值
                        if (entryMap.getKey().equals(fieLdsModel.getVModel())){
                            String[] s=new String[0];
                            if (entryMap.getValue()!=null || entryMap.getValue()!=""){
                                String value = String.valueOf(entryMap.getValue());
                                value= value.replaceAll("\"","").replace("[","").replace("]","");
                                s = value.split(",");
                                entryMap.setValue(s);
                            }
                        }
                        newDataMap.put(entryMap.getKey(), entryMap.getValue());
                    }
                    //省市区控件需要转成数组格式
                    else if (JnpfKeyConsts.ADDRESS.equals(fieLdsModel.getConfig().getJnpfKey())){
                        //对关联的相关字段重新赋值
                        if (entryMap.getKey().equals(fieLdsModel.getVModel())){
                            String[] s=new String[0];
                            if (entryMap.getValue()!=null || entryMap.getValue()!=""){
                                String value = String.valueOf(entryMap.getValue());
                                value= value.replaceAll("\"","").replace("[","").replace("]","");
                                s = value.split(",");
                                entryMap.setValue(s);
                            }
                        }
                        newDataMap.put(entryMap.getKey(), entryMap.getValue());
                    }
                    else {
                        newDataMap.put(entryMap.getKey(), entryMap.getValue());
                    }
                }
            }
        }
        return newDataMap;
    }


    /**
     * 增加删除修改有表单条数据
     * @param sql
     */
    public static void opaTableDataInfo(String sql, DbLinkEntity linkEntity) throws SQLException {
        Connection conn;
        if (linkEntity!=null){
            conn = getDataConn(linkEntity);
        }else {
            conn = getTableConn();
        }
        try {
            JdbcUtil.custom(conn, sql);
        } catch (DataException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取有表的数据库连接
     * @return
     */
    public static Connection getTableConn() {
        init();
        String tenId;
        if (!Boolean.parseBoolean(configValueUtil.getMultiTenancy())) {
            tenId = dataSourceUtil.getDbName();
        } else {
            tenId = userProvider.get().getTenantDbConnectionString();
        }
        try {
            return JdbcUtil.getConn(dataSourceUtil,tenId);
        } catch (DataException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 在线开发多数据源连接
     * @return
     */
    public static Connection getDataConn(DbLinkEntity linkEntity) throws SQLException {
        Connection conn = null;
        if (linkEntity != null) {
            try {
                conn = JdbcUtil.getConn(linkEntity);
            } catch (DataException e) {
                e.printStackTrace();
            }
        }else {
            conn=getTableConn();
        }
        if (conn == null) {
            throw new SQLException("连接数据库失败");
        }
        return conn;
    }

    /**
     * 获取表总记录数
     * @param sql
     * @return
     * @throws SQLException
     * @throws DataException
     */
    public static Integer getTableDataCount(String sql) throws SQLException, DataException {
        init();
        String tenId;
        if (!Boolean.parseBoolean(configValueUtil.getMultiTenancy())) {
            tenId = dataSourceUtil.getDbName();
        } else {
            tenId = userProvider.get().getTenantDbConnectionString();
        }
        Connection conn = JdbcUtil.getConn(dataSourceUtil,tenId);
        ResultSet rs = JdbcUtil.query(conn, sql);
        int count = 0;
        while (rs.next()) {
            count = rs.getInt("rec");
        }
        return count;
    }

    /**
     * 为字段数据字典赋值
     * @param fieldList
     * @param fieldStr
     * @param props
     * @param options
     * @param fieLdsModel
     * @return
     */
    public static String setDicValue(List<String> fieldList, String fieldStr, PropsBeanModel props, List<Map<String, Object>> options, FieLdsModel fieLdsModel) {
        if (fieldList != null && fieldList.size() > 0) {
            for (String fieStr : fieldList) {

                for (Map<String, Object> optMap : options) {
                    if (fieLdsModel.getSlot().getOptions() != null) {
                        String label;
                        String value;
                        if (props != null) {
                            label = props.getLabel();
                            value = props.getValue();
                            if (fieStr.equals(optMap.get(value).toString())) {
                                return optMap.get(label).toString();
                            }
                        }
                    }
                }
            }
        } else {
            for (Map<String, Object> optMap : options) {
                if (fieLdsModel.getSlot().getOptions() != null) {
                    //判断prop是否有值，是否有取别名
                    String label;
                    String value;
                    if (props != null) {
                        label = props.getLabel();
                        value = props.getValue();
                        if (fieldStr.equals(optMap.get(value).toString())) {
                            return optMap.get(label).toString();
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 去除多余字段
     *
     * @return
     */
    public static List<VisualdevModelDataEntity> deleteKey(List<VisualdevModelDataEntity> list, String[] keys) {
        for (VisualdevModelDataEntity entity : list) {
            if (!StringUtil.isEmpty(entity.getData()) && keys.length > 0) {
                Map<String, Object> keyMap = JsonUtil.stringToMap(entity.getData());
                Map<String, Object> keyResult = new HashMap<>(16);

                for (String selkey : keys) {
                    for (Map.Entry<String, Object> entry : keyMap.entrySet()) {
                        String key = entry.getKey();
                        if (key.equals(selkey)) {
                            keyResult.put(key, entry.getValue());
                        }
                    }
                }
                entity.setData(JSON.toJSONString(keyResult));
            }
        }
        return list;
    }

    /**
     * 判断字段数据是数组还是字符串
     * @param field
     * @return
     */
    public static List<String> analysisField(String field) {
        List<String> keyList = new ArrayList<>();
        if (field != null) {
            try {
                keyList = JsonUtil.getJsonToList(field, String.class);
                if (keyList != null) {
                    return keyList;
                }
                return new ArrayList<>();
            } catch (Exception e) {
                return keyList;
            }
        }
        return keyList;
    }

    /**
     * 导出在线开发的表格
     * @param formData
     * @param path
     * @param list
     * @param keys
     * @param userInfo
     * @return
     */
    public static DownloadVO createModelExcel(String formData, String path, List<Map<String, Object>> list, String[] keys, UserInfo userInfo) {
        DownloadVO vo = DownloadVO.builder().build();
        try {
            FormDataModel formDataModel = JsonUtil.getJsonToBean(formData, FormDataModel.class);
            List<FieLdsModel> fieLdsModelList = JsonUtil.getJsonToList(formDataModel.getFields(), FieLdsModel.class);
            List<FormAllModel> formAllModel = new ArrayList<>();
            FormCloumnUtil.recursionForm(fieLdsModelList,formAllModel);
            List<FormAllModel> mast = formAllModel.stream().filter(t -> FormEnum.mast.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
            List<ExcelExportEntity> entitys = new ArrayList<>();
            for (FormAllModel model : mast) {
                FieLdsModel fieLdsModel = model.getFormColumnModel().getFieLdsModel();
                if (keys.length > 0) {
                    for (String key : keys) {
                        if (key.equals(fieLdsModel.getVModel())) {
                            entitys.add(new ExcelExportEntity(fieLdsModel.getConfig().getLabel(), fieLdsModel.getVModel()));
                        }
                    }
                }
            }
            ExportParams exportParams = new ExportParams(null, "表单信息");
            Workbook workbook = ExcelExportUtil.exportExcel(exportParams, entitys, list);
            String fileName = "表单信息" + DateUtil.dateNow("yyyyMMddHHmmss") + ".xls";
            vo.setName(fileName);
            vo.setUrl(UploaderUtil.uploaderFile(userInfo.getId() + "#" + fileName + "#" + "Temporary"));
            path = path + fileName;
            FileOutputStream fos = new FileOutputStream(path);
            workbook.write(fos);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return vo;
    }

    public static List<String> getLabelMapSize(List<Map<String, Object>> optMapList, String label, String value, List<String> fieldList) {
        if (optMapList.size() > 0) {
            List<String> result = new ArrayList<>();
            for (Map<String, Object> optMap : optMapList) {
                List<Map<String, Object>> optChildMapList = JsonUtil.getJsonToListMap(JsonUtil.getObjectToString(optMap.get("children")));
                if (fieldList.contains(String.valueOf(optMap.get(value)))) {
                    result.add(String.valueOf(optMap.get(label)));
                }
                if (optChildMapList != null && optChildMapList.size() > 0) {
                    result.addAll(getLabelMapSize(optChildMapList, label, value, fieldList));
                    return result;
                }
            }
            return result;
        }
        return null;
    }

    public static List<String> getValueMapSize(List<Map<String, Object>> optMapList, String value, List<String> fieldList) {
        if (optMapList.size() > 0) {
            List<String> result = new ArrayList<>();
            for (Map<String, Object> optMap : optMapList) {
                List<Map<String, Object>> optChildMapList = JsonUtil.getJsonToListMap(JsonUtil.getObjectToString(optMap.get("children")));
                if (fieldList.contains(String.valueOf(optMap.get(value)))) {
                    result.add(String.valueOf(optMap.get(value)));
                }
                if (optChildMapList != null && optChildMapList.size() > 0) {
                    result.addAll(getValueMapSize(optChildMapList, value, fieldList));
                    return result;
                }
            }
            return result;
        }
        return null;
    }

    public static List<String> analysisLabelMap(Map<String, Object> optMap, FieLdsModel fieLdsModel, PropsBeanModel props, List<String> fieldList) {
        List<String> list = new ArrayList<>();
        if (fieLdsModel.getProps() != null && fieLdsModel.getProps().getProps() != null) {
            //判断prop是否有值，是否有取别名
            String label;
            String value;
            if (props != null) {
                label = props.getLabel();
                value = props.getValue();
                String lab = String.valueOf(optMap.get(label));
                String vau = String.valueOf(optMap.get(value));
                if (fieldList.contains(vau)) {
                    list.add(lab);
                }
                if (optMap.containsKey("children")) {
                    List<String> other = getLabelMapSize(JsonUtil.getJsonToListMap(JsonUtil.getObjectToString(optMap.get("children"))), label, value, fieldList);
                    if (other != null) {
                        list.addAll(other);
                    }
                }
            }
        }
        return list;
    }

    public static List<String> analysisValueMap(Map<String, Object> optMap, FieLdsModel fieLdsModel, PropsBeanModel props, List<String> fieldList) {
        List<String> list = new ArrayList<>();
        if (fieLdsModel.getProps() != null && fieLdsModel.getProps().getProps() != null) {
            //判断prop是否有值，是否有取别名
            String value;
            if (props != null) {
                value = props.getValue();
                String vau = String.valueOf(optMap.get(value));
                if (fieldList.contains(vau)) {
                    list.add(vau);
                }
                if (optMap.containsKey("children")) {
                    List<String> other = getValueMapSize(JsonUtil.getJsonToListMap(JsonUtil.getObjectToString(optMap.get("children"))), value, fieldList);
                    if (other != null) {
                        list.addAll(other);
                    }
                }
            }
        }
        return list;
    }

    /**
     * 级联选择框和树形选择单独操作（静态）
     * @param fieLdsModel
     * @param fieldList
     * @param keyJsonMap
     * @return
     */
    public static Map<String, Object> cascaderOperation(FieLdsModel fieLdsModel, List<String> fieldList, Map<String, Object> keyJsonMap) {
        Map<String, Object> cascaderMap = new HashMap<>(16);
        //判断是不是级联选择框
        if (fieLdsModel.getOptions() != null) {
            List<String> cascaderList = new ArrayList<>();
            List<String> keyListLast = new ArrayList<>();
            List<Map<String, Object>> options = JsonUtil.getJsonToListMap(fieLdsModel.getOptions());
            PropsBeanModel props = JsonUtil.getJsonToBean(fieLdsModel.getProps().getProps(), PropsBeanModel.class);
            List<String> filedListAll = new ArrayList<>();
            if (props.getMultiple()) {
                for (String id : fieldList) {
                    List<String> list = JsonUtil.getJsonToList(id, String.class);
                    filedListAll.addAll(list);
                }
            } else {
                filedListAll = fieldList;
            }
            for (Map<String, Object> optMap : options) {
                List<String> labelList = VisualUtils.analysisLabelMap(optMap, fieLdsModel, props, filedListAll);
                List<String> valueList = VisualUtils.analysisValueMap(optMap, fieLdsModel, props, filedListAll);
                if (labelList.size() == filedListAll.size()) {
                    for (int k = 0; k < labelList.size(); k++) {
                        if (filedListAll.get(k).equals(valueList.get(k))) {
                            cascaderList.add(labelList.get(k));
                        }
                    }
                    //级联选择框查询字段转换
                    if (keyJsonMap != null && keyJsonMap.get(fieLdsModel.getVModel()) != null) {
                        List<String> keyList = JsonUtil.getJsonToList(keyJsonMap.get(fieLdsModel.getVModel()), String.class);
                        if (labelList.size() <= keyList.size()) {
                            for (int k = 0; k < labelList.size(); k++) {
                                if (keyList.get(k).equals(valueList.get(k))) {
                                    keyListLast.add(labelList.get(k));
                                }
                            }
                            if (keyListLast.size() == labelList.size()) {
                                keyJsonMap.put(fieLdsModel.getVModel(), keyListLast);

                            }
                        }
                    }
                    cascaderMap.put("keyJsonMap", keyJsonMap);
                    //级联选择框列表转换
                    if (cascaderList.size() == labelList.size()) {
                        cascaderMap.put("value", cascaderList);
                    }
                }
            }
        }
        return cascaderMap;
    }


    /**
     * 树形选择单独操作（静态）
     *
     * @param fieLdsModel
     * @param fieldStr
     * @return
     */
    public static String treeSelectOperation(FieLdsModel fieLdsModel, String fieldStr) {
        List<String> result = new ArrayList<>();
        //判断是不是级联选择框
        if (fieLdsModel.getOptions() != null) {
            List<Map<String, Object>> options = JsonUtil.getJsonToListMap(fieLdsModel.getOptions());
            PropsBeanModel props = JsonUtil.getJsonToBean(fieLdsModel.getProps().getProps(), PropsBeanModel.class);
            String value = props.getValue();
            String label = props.getLabel();
            String[] str = fieldStr.split(",");
            for (String id : str) {
                for (Map<String, Object> optMap : options) {
                    if (optMap.get(value).toString().equals(id)) {
                        result.add(optMap.get(label).toString());
                    } else if (optMap.get("children") != null) {
                        List<Map<String, Object>> anotherOptions = (List<Map<String, Object>>) optMap.get("children");
                        String another = treeSelectGetValue(value, label, anotherOptions, id);
                        if (!id.equals(another)) {
                            result.add(another);
                        }
                    }
                }
            }
        }
        return String.join(",", result);
    }

    /**
     * 获取树形选择值
     *
     * @param value
     * @param label
     * @param anotherOptions
     * @param fieldStr
     * @return
     */
    public static String treeSelectGetValue(String value, String label, List<Map<String, Object>> anotherOptions, String fieldStr) {
        for (Map<String, Object> optMap : anotherOptions) {
            if (optMap.get(value).toString().equals(fieldStr)) {
                return optMap.get(label).toString();
            } else if (optMap.get("children") != null) {
                List<Map<String, Object>> childOptions = (List<Map<String, Object>>) optMap.get("children");
                String another = treeSelectGetValue(value, label, childOptions, fieldStr);
                if (StringUtil.isNotEmpty(another)) {
                    return another;
                }
            }
        }
        return fieldStr;
    }


    /**
     * 得到时间范围的工具
     *
     * @return
     */
    public static List<Map<String,Object>> getRealList(List<VisualColumnSearchVO> searchList, List<VisualdevModelDataEntity> list, TimeControl timeControl) throws ParseException, ParseException {
        List<Map<String,Object>> realList = new ArrayList<>();
        for (VisualdevModelDataEntity entity : list) {
            Map<String, Object> m2 = JsonUtil.stringToMap(entity.getData());
            if (searchList != null && searchList.size() != 0) {
                //添加关键词全匹配计数，全符合条件则添加
                int i = 0;
                for (VisualColumnSearchVO entry1 : searchList) {
                    Object m1value = entry1.getValue() == null ? "" : entry1.getValue();
                    Object m2value = m2.get(entry1.getVModel()) == null ? "" : m2.get(entry1.getVModel());
                    //若两个map中相同key对应的value相等
                    if (!StringUtil.isEmpty(m1value.toString()) && !StringUtil.isEmpty(m2value.toString())) {
                        if (m2value.toString().contains(m1value.toString()) && !DateUtil.isValidDate(m2value.toString()) && !entry1.getConfig().getJnpfKey().contains("date") && !entry1.getConfig().getJnpfKey().contains("numInput")) {
                            m2.put("id", entity.getId());
                            i++;
                        }
                        if (entry1.getConfig().getJnpfKey().contains("numInput")){
                            JSONArray searchArray = (JSONArray)m1value;
                            //数字输入查询框的两个值
                            Integer firstValue = (Integer)searchArray.get(0);
                            Integer secondValue = (Integer)searchArray.get(1);
                            //数据
                            Integer value = Integer.valueOf(String.valueOf(m2value));
                            //条件1,2组合的情况
                            if (firstValue!=null && secondValue ==null){
                                if (value>=firstValue){
                                    m2.put("id", entity.getId());
                                    i++;
                                }
                            }
                            if (firstValue!=null && secondValue!=null){
                                if (value>=firstValue && value<=secondValue){
                                    m2.put("id", entity.getId());
                                    i++;
                                }
                            }
                            if (firstValue==null && secondValue!=null){
                                if (value<=secondValue){
                                    m2.put("id", entity.getId());
                                    i++;
                                }
                            }

                        }
                        if (timeControl != null) {
                            //这里传的是string
                            if (!StringUtil.isEmpty(timeControl.getDate()) && timeControl.getDate().contains(entry1.getVModel())) {
                                String[] keyArray = String.valueOf(m1value).split(",");
                                String startTime = keyArray[0];
                                String endTime= keyArray[1];
                                //判断是时间字符串还是时间戳
                                if (!startTime.contains(":") &&!startTime.contains("-")){
                                    long firstTime = Long.parseLong(startTime);
                                    long lastTime = Long.parseLong(endTime);

                                    //时间戳转string格式
                                    startTime = DateUtil.daFormat(firstTime);
                                    endTime = DateUtil.daFormat(lastTime);
                                }

                                String firstTimeDate = getTimeFormat(startTime);
                                String lastTimeDate = getLastTimeFormat(endTime);
                                String value = getTimeFormat(m2value.toString());
                                //只判断到日期
                                SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                boolean b = DateUtil.isEffectiveDate(sdf.parse(value), sdf.parse(firstTimeDate), sdf.parse(lastTimeDate));
                                if (b){
                                    m2.put("id", entity.getId());
                                    i++;
                                }
                            }
                            if (!StringUtil.isEmpty(timeControl.getTimeRange()) && timeControl.getTimeRange().contains(entry1.getVModel())) {

                                List<String> list1 = JsonUtil.getJsonToList(m1value, String.class);
                                List<String> list2 = JsonUtil.getJsonToList(m2value, String.class);
                                if (list1.size() == 2 && list2.size() == 2) {
                                    list1.add(0, list1.get(0).substring(0, list1.get(0).length() - 1));
                                    list2.add(0, list2.get(0).substring(0, list2.get(0).length() - 1));
                                    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                                    Date dayTimeStart1 = df.parse(list1.get(0));
                                    Date dayTimeEnd1 = df.parse(list1.get(2));
                                    Date dayTimeStart2 = df.parse(list2.get(0));
                                    Date dayTimeEnd2 = df.parse(list2.get(2));
                                    boolean cont = DateUtil.isOverlap(dayTimeStart1, dayTimeEnd1, dayTimeStart2, dayTimeEnd2);
                                    if (cont) {
                                        m2.put("id", entity.getId());
                                        i++;
                                    }
                                }
                            }
                            if (StringUtil.isNotEmpty(timeControl.getTime()) && timeControl.getTime().contains(entry1.getVModel())){
                                JSONArray keyArray = (JSONArray)m1value;
                                Object jsonObj1 = keyArray.get(0);
                                Object  jsonObj2= keyArray.get(1);
                                String firstTime = String.valueOf(jsonObj1);
                                String lastTime =String.valueOf(jsonObj2);

                                firstTime=getTimeFormat(firstTime);
                                lastTime=getLastTimeFormat(lastTime);
                                SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                String value=getTimeFormat(m2value.toString());
                                boolean b = DateUtil.isEffectiveDate(sdf.parse(value), sdf.parse(firstTime), sdf.parse(lastTime));
                                if (b){
                                    m2.put("id", entity.getId());
                                    i++;
                                }
                            }
                            if (!StringUtil.isEmpty(timeControl.getCreateTime()) && timeControl.getCreateTime().contains(entry1.getVModel())){
                                JSONArray keyArray = (JSONArray)m1value;
                                Object jsonObj1 = keyArray.get(0);
                                Object jsonObj2= keyArray.get(1);
                                long firstTime = (long)jsonObj1;
                                long lastTime = (long)jsonObj2;

                                //时间戳转string格式
                                String startTime = DateUtil.daFormat(firstTime);
                                String endTime = DateUtil.daFormat(lastTime);

                                String firstTimeDate = getTimeFormat(startTime.substring(0,10));
                                String lastTimeDate = getLastTimeFormat(endTime.substring(0,10));
                                SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                String format = getTimeFormat(m2value.toString());
                                boolean b = DateUtil.isEffectiveDate(sdf.parse(format),sdf.parse(firstTimeDate),sdf.parse(lastTimeDate));
                                if (b){
                                    m2.put("id", entity.getId());
                                    i++;
                                }
                            }
                            if (!StringUtil.isEmpty(timeControl.getModifyTime()) && timeControl.getModifyTime().contains(entry1.getVModel())){
                                JSONArray keyArray = (JSONArray)m1value;
                                Object jsonObj1 = keyArray.get(0);
                                Object jsonObj2= keyArray.get(1);
                                long firstTime = (long)jsonObj1;
                                long lastTime = (long)jsonObj2;
                                //时间戳转string格式
                                String startTime = DateUtil.daFormat(firstTime);
                                String endTime = DateUtil.daFormat(lastTime);

                                String firstTimeDate = getTimeFormat(startTime.substring(0,10));
                                String lastTimeDate = getLastTimeFormat(endTime.substring(0,10));

                                SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                String format = getTimeFormat(m2value.toString());
                                boolean b = DateUtil.isEffectiveDate(sdf.parse(format),sdf.parse(firstTimeDate),sdf.parse(lastTimeDate));
                                if (b){
                                    m2.put("id", entity.getId());
                                    i++;
                                }
                            }
                            if (!StringUtil.isEmpty(timeControl.getDateRange()) && timeControl.getDateRange().contains(entry1.getVModel())) {
                                List<String> list1 = JsonUtil.getJsonToList(m1value, String.class);
                                List<String> list2 = JsonUtil.getJsonToList(m2value, String.class);
                                if (list1.size() == 2 && list2.size() == 2) {
                                    list1.add(0, list1.get(0).substring(0, list1.get(0).length() - 1));
                                    list2.add(0, list2.get(0).substring(0, list2.get(0).length() - 1));
                                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                                    if (list1.get(0).length() > 10) {
                                        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    }
                                    Date dayTimeStart1 = df.parse(list1.get(0));
                                    Date dayTimeEnd1 = df.parse(list2.get(0));

                                    Date dayTimeStart2 = df.parse(list2.get(0));
                                    Date dayTimeEnd2 = df.parse(list2.get(2));

                                    boolean cont = DateUtil.isOverlap(dayTimeStart1, dayTimeEnd1, dayTimeStart2, dayTimeEnd2);
                                    if (cont) {
                                        m2.put("id", entity.getId());
                                        i++;
                                    }
                                }
                            }
                        }
                    }

                    if (i == searchList.size()) {
                        realList.add(m2);
                    }
                }
            } else {
                m2.put("id", entity.getId());
                realList.add(m2);
            }
        }
        return realList;
    }

    /**
     * String转数组
     *
     * @return
     */
    public static List<VisualdevModelDataEntity> stringToList(List<FieLdsModel> fieLdsModelList, List<VisualdevModelDataEntity> list) {
        for (FieLdsModel fieLdsModel : fieLdsModelList) {
            for (VisualdevModelDataEntity entity : list) {
                Map<String, Object> dataMap = JsonUtil.stringToMap(entity.getData());
                if (JnpfKeyConsts.UPLOADFZ.equals(fieLdsModel.getConfig().getJnpfKey()) || JnpfKeyConsts.UPLOADIMG.equals(fieLdsModel.getConfig().getJnpfKey())) {
                    for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                        if (entry.getKey().equals(fieLdsModel.getVModel())) {
                            entry.setValue(JsonUtil.getJsonToListMap(entry.getValue().toString()));
                        }
                    }
                }
            }
        }
        return list;
    }

    /**
     * 为选择框赋值（静态）
     * @param fieldList
     * @param fieldStr
     * @param fieLdsModel
     * @return
     */
    public static Object setSelect(List<String> fieldList, String fieldStr, FieLdsModel fieLdsModel) {
        //正常多选列表赋值
        if (fieLdsModel.getSlot() != null && fieLdsModel.getSlot().getOptions() != null) {
            String value = fieLdsModel.getConfig().getProps().getValue();
            String label = fieLdsModel.getConfig().getProps().getLabel();
            //模板选项集合
            List<Map<String, Object>> options = JsonUtil.getJsonToListMap(fieLdsModel.getSlot().getOptions());
            if (fieldList != null && fieldList.size() > 0) {
                //新建多选集合
                List<String> moreValue = new ArrayList<>();
                for (String fieStr : fieldList) {
                    for (Map<String, Object> optMap : options) {
                        if (fieStr.equals(optMap.get(value).toString())) {
                            moreValue.add(optMap.get(label).toString());
                        }
                    }
                }
                //将多个选项赋值给列表选项集合
                return moreValue;
            } else {
                for (Map<String, Object> optMap : options) {
                    if (fieLdsModel.getSlot() != null && fieLdsModel.getSlot().getOptions() != null) {
                        if (optMap.get(value) != null && fieldStr.equals(optMap.get(value).toString())) {
                            return optMap.get(label).toString();
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 关联表单字段判断
     * @param value
     * @param type
     * @param configModel
     * @return
     */
    public static Map<String, Object> relaField(String value, String type, ConfigModel configModel) {
        HashSet<String> keyList = new HashSet<>(16);

        Map<String, Object> allKey = new HashMap<>(16);
        switch (type) {
            //单选框
            case JnpfKeyConsts.RADIO:
                //下拉框
            case JnpfKeyConsts.SELECT:
                if (DataTypeConst.DICTIONARY.equals(configModel.getDataType())) {
                    keyList.add(value);
                }
                break;
            //复选框
            case JnpfKeyConsts.CHECKBOX:
                if (DataTypeConst.DICTIONARY.equals(configModel.getDataType())) {
                    //字段数据id
                    List<String> add = VisualUtils.analysisField(value);
                    String addStr = value;
                    if (add.size() > 0) {
                        for (String str : add) {
                            keyList.add(str);
                        }
                    } else {
                        keyList.add(addStr);
                    }
                }
                break;
            //公司
            case JnpfKeyConsts.COMSELECT:
                //部门
            case JnpfKeyConsts.DEPSELECT:
                if (value.contains(",")) {
                    String[] depSelects = value.split(",");
                    for (String depSelect : depSelects) {
                        keyList.add(depSelect);
                    }
                } else {
                    keyList.add(value);
                }
                break;
            //岗位
            case JnpfKeyConsts.POSSELECT:
                if (value.contains(",")) {
                    String[] posSelects = value.split(",");
                    for (String posSelect : posSelects) {
                        keyList.add(posSelect);
                    }
                } else {
                    keyList.add(value);
                }
                break;
            //用户
            case JnpfKeyConsts.USERSELECT:
                if (value.contains(",")) {
                    String[] userSelects = value.split(",");
                    for (String userSelect : userSelects) {
                        keyList.add(userSelect);
                    }
                } else {
                    keyList.add(value);
                }
                break;
            //数据字典
            case JnpfKeyConsts.DICSELECT:
                keyList.add(value);
                break;
            //省市区
            case JnpfKeyConsts.ADDRESS:
                List<String> add = JsonUtil.getJsonToList(value, String.class);
                for (String str : add) {
                    keyList.add(str);
                }
                break;
            default:
        }
        allKey.put(configModel.getJnpfKey(), keyList);
        return allKey;
    }

    /**
     * 关联表单字段赋值
     * @param dataMap
     * @param keyMap
     * @param type
     * @param key
     * @param value
     * @param configModel
     * @param model
     * @return
     * @throws IOException
     */
    public static Map<String, Object> relaFieldValue(Map<String, Object> dataMap, Map<String, Object> keyMap, String type, String key, String value, ConfigModel configModel, FieLdsModel model) throws IOException {
        Map<String, Object> result = new HashMap<>(16);
        switch (type) {
            //单选框
            case JnpfKeyConsts.RADIO:
                //下拉框
            case JnpfKeyConsts.SELECT:
                if (DataTypeConst.DICTIONARY.equals(configModel.getDataType())) {
                    if (keyMap.containsKey(key)) {
                        result.put("value", keyMap.get(key));
                    }
                }
                if (DataTypeConst.STATIC.equals(configModel.getDataType())) {
                    List<Map<String, Object>> modelOpt = JsonUtil.getJsonToListMap(model.getSlot().getOptions());
                    for (Map<String, Object> map : modelOpt) {
                        if (map.get(configModel.getProps().getValue()).toString().equals(value)) {
                            result.put("value", map.get(model.getConfig().getProps().getLabel()).toString());
                        }
                    }
                }
                if (DataTypeConst.DYNAMIC.equals(configModel.getDataType())) {
                    DynamicUtil dynamicUtil = new DynamicUtil();
                    dataMap = dynamicUtil.dynamicKeyData(model, dataMap);
                    result.put("dataMap", dataMap);
                }
                break;
            //复选框
            case JnpfKeyConsts.CHECKBOX:
                if (DataTypeConst.DICTIONARY.equals(configModel.getDataType())) {
                    //字段数据id
                    List<String> add = VisualUtils.analysisField(value);
                    String addStr = value;
                    StringBuilder addName = new StringBuilder();
                    if (add.size() > 0) {
                        for (String str : add) {
                            if (keyMap.containsKey(str)) {
                                addName.append(keyMap.get(str));
                            }
                        }
                    } else {
                        if (keyMap.containsKey(addStr)) {
                            addName.append(keyMap.get(addStr));
                        }
                    }
                    if (addName.length() != 0) {
                        result.put("value", addName);
                    }
                }
                if (DataTypeConst.STATIC.equals(configModel.getDataType())) {
                    if (model.getSlot() != null && model.getSlot().getOptions() != null) {
                        List<Map<String, Object>> modelOpt = JsonUtil.getJsonToListMap(model.getSlot().getOptions());
                        for (Map<String, Object> map : modelOpt) {
                            if (map.get(model.getConfig().getProps().getValue()).toString().equals(value)) {
                                result.put("value", map.get(model.getConfig().getProps().getLabel()));
                            }

                        }
                    }
                }
                if (DataTypeConst.DYNAMIC.equals(configModel.getDataType())) {
                    //获取最新远端数据转换远端数据查询关键词
                    DynamicUtil dynamicUtil = new DynamicUtil();
                    dataMap = dynamicUtil.dynamicKeyData(model, dataMap);
                    result.put("dataMap", dataMap);
                }
                break;
            //公司
            case JnpfKeyConsts.COMSELECT:
                //部门
            case JnpfKeyConsts.DEPSELECT:
                if (value.contains(",")) {
                    String[] depSelects = value.split(",");
                    String[] newDepSelects = new String[depSelects.length];
                    int i = 0;
                    for (String depSelect : depSelects) {
                        if (keyMap.containsKey(depSelect)) {
                            newDepSelects[i] = String.valueOf(keyMap.get(depSelect));
                        }
                        i++;
                    }
                    result.put("value", newDepSelects);
                } else {
                    String str = value;
                    if (keyMap.containsKey(str)) {
                        result.put("value", keyMap.get(str));
                    }
                }
                break;
            //岗位
            case JnpfKeyConsts.POSSELECT:
                if (value.contains(",")) {
                    String[] posSelects = value.split(",");
                    String[] newposSelects = new String[posSelects.length];
                    int i = 0;
                    for (String posSelect : posSelects) {
                        if (keyMap.containsKey(posSelect)) {
                            newposSelects[i] = String.valueOf(keyMap.get(posSelect));
                        }
                        i++;
                    }
                    result.put("value", newposSelects);
                } else {
                    if (keyMap.containsKey(value)) {
                        result.put("value", keyMap.get(value));
                    }
                }
                break;
            //用户
            case JnpfKeyConsts.USERSELECT:
                if (value.contains(",")) {
                    String[] userSelects = value.split(",");
                    String[] newuserSelects = new String[userSelects.length];
                    int i = 0;
                    for (String userSelect : userSelects) {
                        if (keyMap.containsKey(userSelect)) {
                            newuserSelects[i] = String.valueOf(keyMap.get(userSelect));
                        }
                        i++;
                    }
                    result.put("value", newuserSelects);
                } else {
                    if (keyMap.containsKey(value)) {
                        result.put("value", keyMap.get(value));
                    }
                }
                break;
            //数据字典
            case JnpfKeyConsts.DICSELECT:
                if (keyMap.containsKey(value)) {
                    result.put("value", keyMap.get(value));
                }
                break;
            //省市区
            case JnpfKeyConsts.ADDRESS:
                List<String> add = JsonUtil.getJsonToList(value, String.class);
                StringBuilder addName = new StringBuilder();
                for (String str : add) {
                    if (keyMap.containsKey(str)) {
                        addName.append(keyMap.get(str)).append("/");
                    }
                }
                if (addName.length() != 0) {
                    addName.deleteCharAt(addName.length() - 1);
                    result.put("value", addName);
                }
                break;
            //时间范围
            case JnpfKeyConsts.TIMERANGE:
                JSONArray jsonArrayTime = JsonUtil.getJsonToJsonArray(String.valueOf(value));
                jsonArrayTime = DateUtil.addCon(jsonArrayTime, JnpfKeyConsts.TIMERANGE, "HH:mm:ss");
                result.put("value", jsonArrayTime.toJSONString());
                break;
            //日期选择
            case JnpfKeyConsts.DATE:
                DateTimeFormatter ftf = DateTimeFormatter.ofPattern(model.getFormat());
                long time;
                try {
                    time = Long.parseLong(String.valueOf(value));
                    String values = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
                    result.put("value", values);
                } catch (Exception e) {
                    result.put("value", value);
                }
                break;
            //日期范围
            case JnpfKeyConsts.DATERANGE:
                JSONArray jsonArray = JsonUtil.getJsonToJsonArray(String.valueOf(dataMap.get(key)));
                jsonArray = DateUtil.addCon(jsonArray, JnpfKeyConsts.DATERANGE, model.getFormat());
                result.put("value", jsonArray.toJSONString());
                break;
            default:
        }
        return result;
    }

    /**
     * @param mapList
     * @return List<Map < String, Object>>
     * @Date 21:51 2020/11/11
     * @Description 将map中的所有key转化为小写
     */
    public static List<Map<String, Object>> toLowerKeyList(List<Map<String, Object>> mapList) {
        List<Map<String, Object>> newMapList = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            Map<String, Object> resultMap = new HashMap(16);
            Set<String> sets = map.keySet();
            for (String key : sets) {
                resultMap.put(key.toLowerCase(), map.get(key));
            }
            newMapList.add(resultMap);
        }
        return newMapList;
    }


    /**
     * @param map
     * @return java.util.Map<java.lang.String, java.lang.String>
     * @Description 将map中的所有key转化为小写
     */
    public static Map<String, Object> toLowerKey(Map<String, Object> map) {
        Map<String, Object> resultMap = new HashMap<>(16);
        Set<String> sets = map.keySet();
        for (String key : sets) {
            resultMap.put(key.toLowerCase(), map.get(key));
        }
        return resultMap;
    }


    /**
     * @param entity
     * @return
     * @Description 删除F_, 暴力转换
     */
    public static VisualdevEntity delAllfKey(VisualdevEntity entity) {

        if (entity.getTables() != null) {
            String tables = entity.getTables().trim().replaceAll(":\"f_", ":\"");
            entity.setTables(tables);
        }
        if (entity.getColumnData() != null) {
            String columnData = entity.getColumnData().trim().replaceAll("__vModel__:\"f_", "__vModel__:\"");
            entity.setColumnData(columnData);
        }
        if (entity.getFormData() != null) {
            String formData = entity.getFormData().trim();
            entity.setFormData(formData.replaceAll("__vModel__:\"f_", "__vModel__:\""));
        }
        return entity;
    }

    /**
     * @param entity
     * @return
     * @Description 删除模板字段下划线
     */
    public static VisualdevEntity delete(VisualdevEntity entity) {
        //取出列表数据中的查询列表和数据列表
        if (StringUtil.isNotEmpty(entity.getColumnData())) {
            //纯表单 app的搜索不转换
            if (entity.getType() != 5 && !entity.getWebType().equals("1")) {
                Map<String, Object> columnDataMap = JsonUtil.stringToMap(entity.getColumnData());
                List<FieLdsModel> columnfield = JsonUtil.getJsonToList(columnDataMap.get("searchList"), FieLdsModel.class);
                columnDataMap.put("searchList", columnfield);
                entity.setColumnData(JsonUtil.getObjectToString(columnDataMap));
            }
        }
        Map<String, Object> formData = JsonUtil.stringToMap(entity.getFormData());
        List<FieLdsModel> modelList = JsonUtil.getJsonToList(formData.get("fields"), FieLdsModel.class);
        formData.put("fields", modelList);
        entity.setFormData(JsonUtil.getObjectToString(formData));
        return entity;
    }


    /**
     * @param entity
     * @return
     * @Description 针对模板大写转小写
     */
    public static VisualdevEntity changeType(VisualdevEntity entity) {

        List<Map<String, Object>> list = JsonUtil.getJsonToListMap(entity.getTables());
        if (list.size() > 0) {
            for (Map<String, Object> tableModel : list) {
                if (tableModel.get("fields") != null) {
                    List<TableFields> fields = JsonUtil.getJsonToList(JsonUtil.getObjectToString(tableModel.get("fields")), TableFields.class);
                    for (TableFields tableField : fields) {
                        String feildD = tableField.getField();
                        String feildL = tableField.getField().toLowerCase();
                        if (entity.getTables() != null) {
                            entity.setTables(entity.getTables().replaceAll(feildD, feildL));
                        }
                        if (entity.getColumnData() != null) {
                            entity.setColumnData(entity.getColumnData().replaceAll(feildD, feildL));
                        }
                        if (entity.getFormData() != null) {
                            entity.setFormData(entity.getFormData().replaceAll(feildD, feildL));
                        }
                    }
                }
            }
        }
        return entity;
    }

    /**
     * @param keyName
     * @param dataEntityList
     * @return
     */
    public static List<VisualdevModelDataEntity> setDataId(String keyName, List<VisualdevModelDataEntity> dataEntityList) {
        keyName = keyName.toLowerCase();
        for (VisualdevModelDataEntity entity : dataEntityList) {
            Map<String, Object> dataMap = JsonUtil.stringToMap(entity.getData());
            if (dataMap.get(keyName) != null) {
                entity.setId(String.valueOf(dataMap.get(keyName)));
            }
        }
        return dataEntityList;
    }

    /**
     * 获取列表结果查询语句
     *
     * @param keyFlag
     * @param feilds
     * @param mainTable
     * @param pKeyName
     * @param columnData
     * @return
     */
    public static String getListResultSql(Boolean keyFlag, String feilds, String mainTable, String pKeyName, ColumnDataModel columnData) {
        //dataSourceUtil初始化
        init();
        StringBuilder sql = new StringBuilder();
        if (DbTypeUtil.checkDb(dataSourceUtil,DbMysql.DB_ENCODE)) {
            if (keyFlag) {
                sql.append("select " + feilds + " from" + " " + mainTable + " ORDER BY ");
            } else {
                sql.append("select " + pKeyName + "," + feilds + " from" + " " + mainTable + " ORDER BY ");
            }
            if (!StringUtil.isEmpty(columnData.getDefaultSidx())) {
                sql.append(columnData.getDefaultSidx() + " " + columnData.getSort());
            } else {
                sql.append(pKeyName + " " + columnData.getSort());
            }
        } else if (DbTypeUtil.checkDb(dataSourceUtil,DbSqlserver.DB_ENCODE)) {
            if (keyFlag) {
                sql.append("select " + feilds + " from" + " " + mainTable + " ORDER BY ");
            } else {
                sql.append("select " + pKeyName + "," + feilds + " from" + " " + mainTable + " ORDER BY ");
            }
            if (!StringUtil.isEmpty(columnData.getDefaultSidx())) {
                sql.append(columnData.getDefaultSidx() + " " + columnData.getSort());
            } else {
                sql.append(pKeyName + " " + columnData.getSort());
            }
        } else if (DbTypeUtil.checkDb(dataSourceUtil,DbOracle.DB_ENCODE)) {
            if (keyFlag) {
                sql.append("select " + feilds + " from" + " " + mainTable + " ORDER BY ");
            } else {
                sql.append("select " + pKeyName + "," + feilds + " from" + " " + mainTable + " ORDER BY ");
            }
            if (!StringUtil.isEmpty(columnData.getDefaultSidx())) {
                sql.append(columnData.getDefaultSidx() + " " + columnData.getSort());
            } else {
                sql.append(pKeyName + " " + columnData.getSort());
            }

        }else if(DbTypeUtil.checkDb(dataSourceUtil,DbDm.DB_ENCODE)){
            if (keyFlag) {
                sql.append("select " + feilds + " from" + " " + mainTable + " ORDER BY ");
            } else {
                sql.append("select " + pKeyName + "," + feilds + " from" + " " + mainTable + " ORDER BY ");
            }
            if (!StringUtil.isEmpty(columnData.getDefaultSidx())) {
                sql.append(columnData.getDefaultSidx() + " " + columnData.getSort());
            } else {
                sql.append(pKeyName + " " + columnData.getSort());
            }
        }
        return sql.toString();
    }
    /**
     * 多表取主表
     * @param tableMapList
     * @return
     */
    public static Map<String, Object> getMainTable(List<Map<String, Object>> tableMapList){
        for(Map<String, Object> tableMap : tableMapList){
            if (tableMap.get("typeId").equals("1")){
                return tableMap;
            }
        }
        return null;
    }

    /**
     * 转换时间格式
     * @param time
     * @return
     */
    public static String getTimeFormat(String time){
        String result;
        switch (time.length()){
            case 16:
                result=time+":00";
                break;
            case 19:
                result=time;
                break;
            case 21:
                result=time.substring(0,time.length()-2);
                break;
            case 10:
                result=time+" 00:00:00";
                break;
            case 8:
                result="2000-01-01 "+time;
                break;
            default:
                result="";
                break;
        }
        return result;
    }

    public static String getLastTimeFormat(String time){
        String result;
        switch (time.length()){
            case 16:
                result=time+":00";
                break;
            case 19:
                result=time;
                break;
            case 10:
                result=time+" 23:59:59";
                break;
            case 8:
                result="2000-01-01 "+time;
                break;
            default:
                result="";
                break;
        }
        return result;
    }


    /**
     * 审批流提交
     * @param visualdevEntity
     * @param flowTaskId
     * @param formdata
     * @param userInfo
     */
    public static void submitFlowTask(VisualdevEntity visualdevEntity,String flowTaskId,Object formdata,UserInfo userInfo) throws WorkFlowException {
        //审批流
        if (visualdevEntity.getWebType().equals("3")){
            try {
                FlowEngineService flowEngineService = SpringContext.getBean(FlowEngineService.class);
                FlowEngineEntity flowEngineEntity = flowEngineService.getInfo(visualdevEntity.getFlowId());
                FlowTaskService flowTaskService = SpringContext.getBean(FlowTaskService.class);
                FlowTaskEntity flowTaskEntity = flowTaskService.getInfoSubmit(flowTaskId);
                String id = null;
                if (flowTaskEntity != null) {
                    id = flowTaskEntity.getId();
                }
                String flowTitle = userInfo.getUserName()+ visualdevEntity.getFullName();
                String billNo ="#Visual"+ DateUtil.getNow();
                ModelUtil.submit(id,flowEngineEntity.getId(),flowTaskId,flowTitle,1,billNo,formdata,null);
            } catch (WorkFlowException e) {
                throw new WorkFlowException("审批流提交失败!");
            }
        }
    }
}
