package jnpf.onlinedev.util;

import jnpf.base.ActionResult;
import jnpf.database.model.DataSourceUtil;
import jnpf.database.model.DbLinkEntity;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.util.VisualUtils;
import jnpf.database.exception.DataException;
import jnpf.database.source.impl.DbOracle;
import jnpf.database.util.DbTypeUtil;
import jnpf.model.visiual.TableFields;
import jnpf.model.visiual.TableModel;
import jnpf.model.visiual.fields.FieLdsModel;
import jnpf.util.*;
import jnpf.util.context.SpringContext;
import lombok.Cleanup;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 处理在线开发新增更新操作
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年3月22日10:58:29
 */
public class OnlineDevDbUtil {


    private static DataSourceUtil dataSourceUtil = SpringContext.getBean(DataSourceUtil.class);


    public static void insertTable(VisualdevEntity visualdevEntity, List<FieLdsModel> fieLdsModelList , Map<String, Object> allDataMap , Map<String, Object> newMainDataMap, DbLinkEntity linkEntity,String mainId) throws SQLException {

        List<FieLdsModel> modelList = fieLdsModelList;

        boolean isOracle = DbTypeUtil.checkDb(dataSourceUtil, DbOracle.DB_ENCODE);
        if (linkEntity != null) {
            isOracle = linkEntity.getDbType().toLowerCase().contains("oracle");
        }

        //取主表
        List<Map<String, Object>> tableMapList = JsonUtil.getJsonToListMap(visualdevEntity.getTables());
        String mainTable = String.valueOf(tableMapList.get(0).get("table"));

        @Cleanup Connection conn = VisualUtils.getDataConn(linkEntity);
        //获取主键
        String pKeyName = VisualUtils.getpKey(conn, mainTable);

        //主表字段集合
        StringBuilder mainFelid = new StringBuilder();
        List<String> mainFelidList = new ArrayList<>();
        //主表查询语句
        StringBuilder mainSql = new StringBuilder();
        StringBuilder allAddSql = new StringBuilder();
        for (FieLdsModel model : modelList) {
            if ("table".equals(model.getConfig().getJnpfKey())) {
                //遍历所有数据寻找子表
                // 返回所有的entry实体
                Iterator<Map.Entry<String, Object>> iterator = newMainDataMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Object> next1 = iterator.next();
                    String key = next1.getKey();
                    //判断子表是否有数据
                    if (key.equals(model.getVModel()) && next1.getValue() != null) {
                        StringBuilder feilds = new StringBuilder();
                        List<FieLdsModel> childModelList = JsonUtil.getJsonToList(model.getConfig().getChildren(), FieLdsModel.class);

                        for (Map<String, Object> tableMap : tableMapList) {
                            if (tableMap.get("table").toString().equals(model.getConfig().getTableName())) {
                                for (FieLdsModel model1 : childModelList) {
                                    feilds.append(model1.getVModel() + ",");
                                }
                            }
                        }
                        if (childModelList.size() > 0) {
                            feilds.deleteCharAt(feilds.length() - 1);
                        }
                        //查询子表数据sql
                        StringBuilder childSql = new StringBuilder();

                        String childTableName = model.getConfig().getTableName();
                        //获取主键
                        String childpKeyName = VisualUtils.getpKey(conn, childTableName);
                        for (Map<String, Object> tableMap : tableMapList) {
                            if (tableMap.get("table").toString().equals(model.getConfig().getTableName())) {
                                childSql.append(VisualUtils.getInsertSql(childTableName, feilds.toString(), String.valueOf(tableMap.get("tableField")), childpKeyName, mainId));
                            }
                        }
                        String baseSql = childSql.toString().split("VALUES")[1];
                        String headerSql = childSql.toString().split("VALUES")[0] + " VALUES";

                        childSql = new StringBuilder();
                        childSql.append(headerSql);
                        //tableMap Tables()
                        for (Map<String, Object> tableMap : tableMapList) {
                            if (tableMap.get("table").toString().equals(childTableName)) {
                                //添加主表查询字段
                                List<Map<String, Object>> childList = (List<Map<String, Object>>) allDataMap.get(key);
                                //记录子表关联的主键名称
                                String relaMainKey = String.valueOf(tableMap.get("tableField"));

                                //循环子表数据
                                for (Map<String, Object> childMap : childList) {
                                    childSql.append(VisualUtils.getRealSql(baseSql, mainId));
                                    //循环子表模型
                                    for (FieLdsModel model1 : childModelList) {
                                        List<TableFields> tableFieldList = JsonUtil.getJsonToList(tableMap.get("fields"), TableFields.class);
                                        for (TableFields childTableFields : tableFieldList) {
                                            if (childMap.get(model1.getVModel()) != null && childTableFields.getField().equals(model1.getVModel()) && ("date").equals(model1.getConfig().getJnpfKey())) {
                                                String dateTime = String.valueOf(childMap.get(model1.getVModel()));
                                                DateTimeFormatter ftf = DateTimeFormatter.ofPattern(model1.getFormat());
                                                long time = Long.parseLong(dateTime);
                                                String value = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
                                                childMap.put(model1.getVModel(), value);
                                            }
                                        }

                                        //判断字段值是否为关联主键以及是否为空
                                        if (childMap.containsKey(model1.getVModel()) && relaMainKey.equals(model1.getVModel())) {
                                            childSql.append(mainId + ",");
                                        } else if (!childMap.containsKey(model1.getVModel())) {
                                            childSql.append(null + ",");
                                        } else {
                                            if (DbTypeUtil.checkDb(dataSourceUtil,DbOracle.DB_ENCODE)) {
                                                if ("date".equals(model1.getConfig().getJnpfKey())
                                                        || "createTime".equals(model1.getConfig().getJnpfKey())
                                                        || "modifyTime".equals(model1.getConfig().getJnpfKey())) {
                                                    String dateValue = String.valueOf(childMap.get(model1.getVModel()));
                                                    if (dateValue.length()!=0){
                                                        if (dateValue.length() < 11) {
                                                            dateValue = dateValue + " 00:00:00";
                                                        }
                                                        childSql.append("to_date('" + dateValue + "','yyyy-mm-dd HH24:mi:ss'),");
                                                    }else {
                                                        childSql.append(null + ",");
                                                    }
                                                } else {
                                                    String childValue = String.valueOf(childMap.get(model1.getVModel()));
                                                    if (!"null".equals(childValue) && !"".equals(childValue)) {
                                                        childSql.append("'" + childValue + "',");
                                                    } else {
                                                        childSql.append(null + ",");
                                                    }
                                                }
                                            } else {
                                                String childValue = String.valueOf(childMap.get(model1.getVModel()));
                                                if (!"null".equals(childValue) && !"".equals(childValue)) {
                                                    childSql.append("'" + childValue + "',");
                                                } else {
                                                    childSql.append(null + ",");
                                                }
                                            }
                                        }
                                    }
                                    childSql.deleteCharAt(childSql.length() - 1);
                                    childSql.append("),");
                                }
                                childSql.deleteCharAt(childSql.length() - 1);
                                String childSqlx = childSql.toString();
                                childSqlx = childSqlx.replaceAll(",'\\)", ")");
                                allAddSql.append(childSqlx + ";");
                                //清除子表字段
                                allDataMap.remove(key);
                            }
                        }
                    }
                }
            } else {
                //添加主表查询字段
                if (tableMapList.size() > 0) {
                    List<TableFields> tableFieldList = JsonUtil.getJsonToList(VisualUtils.getMainTable(tableMapList).get("fields"), TableFields.class);
                    for (TableFields tableFields : tableFieldList) {
                        if (allDataMap.get(model.getVModel()) != null && tableFields.getField().equals(model.getVModel()) && model.getConfig().getJnpfKey().contains("date")) {
                            String dateTime = String.valueOf(allDataMap.get(model.getVModel()));
                            if (dateTime.contains(",")) {
                                List<String> dateList = JsonUtil.getJsonToList(allDataMap.get(model.getVModel()), String.class);
                                List<String> newDateList = new ArrayList<>();
                                for (String dateStr : dateList) {
                                    DateTimeFormatter ftf = DateTimeFormatter.ofPattern(model.getFormat());
                                    long time = Long.parseLong(dateStr);
                                    String value = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
                                    newDateList.add(value);
                                }
                                allDataMap.put(model.getVModel(), JsonUtilEx.getObjectToString(newDateList));
                            } else {
                                DateTimeFormatter ftf = DateTimeFormatter.ofPattern(model.getFormat());
                                long time;
                                try {
                                    time = Long.parseLong(dateTime);
                                    String value = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
                                    allDataMap.put(model.getVModel(), value);
                                } catch (Exception e) {
                                    allDataMap.put(model.getVModel(), dateTime);
                                }
                            }
                        }
                    }
                }
                mainFelid.append(model.getVModel() + ",");
                mainFelidList.add(model.getVModel());
            }
        }
        mainFelid.deleteCharAt(mainFelid.length() - 1);
        if (VisualUtils.existKey(mainFelid.toString().toLowerCase().trim(), pKeyName.toLowerCase())) {
            mainSql.append("INSERT INTO " + mainTable + "(" + mainFelid.toString() + ")" + "VALUES(");
        } else {
            mainSql.append("INSERT INTO " + mainTable + "(" + pKeyName + "," + mainFelid.toString() + ")" + "VALUES('" + mainId + "',");
        }
        //调整字段与值的顺序
        for (FieLdsModel mainModel : modelList) {
            for (String mainStr : mainFelidList) {
                if (mainStr.equals(mainModel.getVModel())) {
                    //判断字段值是否为空
                    if (!allDataMap.containsKey(mainModel.getVModel())) {
                        mainSql.append(null + ",");
                    } else {
                        if (allDataMap.get(mainModel.getVModel()) != null) {
                            if (isOracle) {
                                if ("date".equals(mainModel.getConfig().getJnpfKey())
                                        || "createTime".equals(mainModel.getConfig().getJnpfKey())
                                        || "modifyTime".equals(mainModel.getConfig().getJnpfKey())) {
                                    String dateValue =String.valueOf(allDataMap.get(mainModel.getVModel()));
                                    if (dateValue.length()==0){
                                        mainSql.append(null + ",");
                                    }else {
                                        if (dateValue.length() < 11) {
                                            dateValue = dateValue + " 00:00:00";
                                        }
                                        mainSql.append("to_date('" + dateValue + "','yyyy-mm-dd HH24:mi:ss'),");
                                    }
                                } else {
                                    String mainValue = String.valueOf(allDataMap.get(mainModel.getVModel()));
                                    if (!"null".equals(mainValue) && !"".equals(mainValue)) {
                                        mainSql.append("'" + allDataMap.get(mainModel.getVModel()) + "',");
                                    } else {
                                        mainSql.append(null + ",");
                                    }
                                }
                            } else {
                                String mainValue = String.valueOf(allDataMap.get(mainModel.getVModel()));
                                if (!"null".equals(mainValue) && !"".equals(mainValue)) {
                                    mainSql.append("'" + allDataMap.get(mainModel.getVModel()) + "',");
                                } else {
                                    mainSql.append(null + ",");
                                }
                            }
                        } else {
                            mainSql.append(null + ",");
                        }

                    }
                }
            }
        }
        mainSql.deleteCharAt(mainSql.length() - 1);
        mainSql.append(")");
        VisualJDBCUtils.custom(linkEntity,mainSql.toString());
        if (allAddSql!=null && !String.valueOf(allAddSql).equals("")){
            VisualUtils.opaTableDataInfo(allAddSql.toString(),linkEntity);
        }
    }

    /**
     * 删除有表单条数据
     * @param id
     * @param visualdevEntity
     * @return
     * @throws SQLException
     * @throws DataException
     */
    public static boolean deleteTable(String id,VisualdevEntity visualdevEntity,DbLinkEntity linkEntity) throws SQLException, DataException {
        List<Map<String, Object>> tableMapList = JsonUtil.getJsonToListMap(visualdevEntity.getTables());

        String mainTable = String.valueOf(tableMapList.get(0).get("table"));
        @Cleanup Connection conn = VisualUtils.getDataConn(linkEntity);
        //获取主键
        String pKeyName = VisualUtils.getpKey(conn, mainTable);
        //循环表
        String delMain = "DELETE FROM " + mainTable + " WHERE " + pKeyName + "='" + id + "'";
        StringBuilder allDelSql = new StringBuilder();
        allDelSql.append(delMain + ";");

        String queryMain = "SELECT * FROM" + " " + tableMapList.get(0).get("table") + " WHERE " + pKeyName + "='" + id + "'";
        List<Map<String, Object>> mainMapList = VisualUtils.getTableDataInfo(queryMain,linkEntity);
        mainMapList = VisualUtils.toLowerKeyList(mainMapList);
        if (mainMapList.size() > 0) {
            if (tableMapList.size() > 1) {
                //去除主表
                tableMapList.remove(0);
                for (Map<String, Object> tableMap : tableMapList) {
                    //主表字段
                    String relationField = tableMap.get("relationField").toString();
                    String relationFieldValue = mainMapList.get(0).get(relationField).toString();
                    //子表字段
                    String tableField = tableMap.get("tableField").toString();
                    String childSql = "DELETE FROM " + tableMap.get("table") + " WHERE " + tableField + "='" + relationFieldValue + "'";
                    allDelSql.append(childSql + ";");
                }
            }
            VisualUtils.opaTableDataInfo(allDelSql.toString(),linkEntity);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 删除有表多条数据
     * @param idList
     * @param visualdevEntity
     * @return
     * @throws SQLException
     * @throws DataException
     */
    public static ActionResult deleteTables(List<String> idList, VisualdevEntity visualdevEntity, DbLinkEntity linkEntity) throws SQLException, DataException {
        List<Map<String, Object>> tableList = JsonUtil.getJsonToListMap(visualdevEntity.getTables());
        //对表集合进行排序
        List<Map<String, Object>> tableMapList = new ArrayList<>();
        for (Map<String,Object> tableMap:tableList){
            if (tableMap.get("typeId").equals("1")){
                tableMapList.add(0,tableMap);
            }else {
                tableMapList.add(tableMap);
            }
        }
        //判断数据源
        @Cleanup Connection conn = VisualUtils.getDataConn(linkEntity);
        //取主表
        String mainTable = String.valueOf(tableMapList.get(0).get("table"));
        //获取主键
        String pKeyName = VisualUtils.getpKey(conn, mainTable);
        List<String> idslist= new ArrayList<>();
        //查询数据是否存在
        for(String id:idList){
            String selectSql= "SELECT * FROM " + mainTable + " WHERE " + pKeyName + "='" + id + "'";
            List<Map<String, Object>> mainMapList = VisualUtils.getTableDataInfo(selectSql,linkEntity);
            if (mainMapList.size()>0){
                //是否存在子表
                if(tableMapList.size()>1){
                    //去除主表
                    tableMapList.remove(0);
                    for (Map<String,Object> tableMap:tableMapList){
                        //主表字段
                        String relationField = String.valueOf(tableMap.get("relationField"));
                        String relationFieldValue =String.valueOf(mainMapList.get(0).get(relationField));
                        //子表字段
                        String tableField = String.valueOf(tableMap.get("tableField"));
                        String childSql = "DELETE FROM " + tableMap.get("table") + " WHERE " + tableField + "='" + relationFieldValue + "'";
                        //删除子表数据
                        VisualUtils.opaTableDataInfo(childSql,linkEntity);
                    }
                }
                idslist.add(id);
            }
        }
        if (idslist.size()==0){
            return ActionResult.fail("删除失败,数据不存在");
        }
        StringBuilder fieldsSql=new StringBuilder();
        for(String id:idslist){
            fieldsSql.append("'"+id+"'"+",");
        }
        fieldsSql.deleteCharAt(fieldsSql.length()-1);

        //删除主表数据
        String delMain = "DELETE FROM " + mainTable + " WHERE " + pKeyName + " in "+ " (" + fieldsSql + ")";
        VisualUtils.opaTableDataInfo(delMain,linkEntity);
        return ActionResult.success("删除成功");
    }

    public static boolean updateTables(String id, Map<String, Object> allDataMap,List<FieLdsModel> fieLdsModelList , DbLinkEntity linkEntity,VisualdevEntity visualdevEntity) throws SQLException, DataException {
        //数据源
        @Cleanup Connection conn = VisualUtils.getDataConn(linkEntity);

        boolean isOracle = DbTypeUtil.checkDb(dataSourceUtil,DbOracle.DB_ENCODE);
        if (linkEntity != null) {
            isOracle = linkEntity.getDbType().toLowerCase().contains("oracle");
        }

        //所用表集合
        List<TableModel> tableList = JsonUtil.getJsonToList(visualdevEntity.getTables(), TableModel.class);

        //对表集合进行排序
        List<TableModel> tableMapList = new ArrayList<>();
        for (TableModel tableModel:tableList){
            if (tableModel.getTypeId().equals("1")){
                tableMapList.add(0,tableModel);
            }else {
                tableMapList.add(tableModel);
            }
        }
        //取主表
        String mainTable = String.valueOf(tableMapList.get(0).getTable());

        //获取主表主键
        String pKeyName = VisualUtils.getpKey(conn, mainTable);

        //判断数据是否存在
        String queryMain = "SELECT * FROM" + " " + mainTable + " WHERE " + pKeyName + "='" + id + "'";
        List<Map<String, Object>> mainMapList = VisualUtils.getTableDataInfo(queryMain,linkEntity);
        if (mainMapList.size()<1){
            return false;
        }
        //主表字段
        Map<String, Object> mainFieldMap =  new HashMap<>(16);
        //子表字段
        Map<String,Object> childFileMap = new HashMap<>(16);
        //有子表的情况
        if (tableList.size()>1){
            //取出设计子表里的vmodel
            ArrayList<String> childFiledKey = new ArrayList<>();
            for (FieLdsModel fieLdsModel :fieLdsModelList){
                if (fieLdsModel.getConfig().getJnpfKey().equals("table")){
                    childFiledKey.add(fieLdsModel.getVModel());
                }
            }
            //主表字段 子表字段区分装填(数据)
            for (Map.Entry entry : allDataMap.entrySet()){
               if (childFiledKey.contains(entry.getKey())){
                   childFileMap.put(entry.getKey().toString(),entry.getValue());

               }
            }
            for (Map.Entry entry : allDataMap.entrySet()){
                if (!childFiledKey.contains(entry.getKey())){
                   mainFieldMap.put(entry.getKey().toString(),entry.getValue());
                }
            }
        }else {
            for (Map.Entry entry : allDataMap.entrySet()){
                mainFieldMap.put(entry.getKey().toString(),entry.getValue());
            }
        }

        //更新主表sql语句
        StringBuilder mainTableSql = new StringBuilder();
        StringBuilder fieldSql = new StringBuilder();
        for (Map.Entry entry :mainFieldMap.entrySet()){
            if (entry.getValue()==null || String.valueOf(entry.getValue()).equals("")){
                fieldSql.append(entry.getKey()+" = " + "null"+",");
            }else {
                fieldSql.append(entry.getKey()+" = " +"'"+ entry.getValue()+"',");
            }

        }
        if (StringUtil.isNotEmpty(fieldSql)){
            fieldSql.deleteCharAt(fieldSql.lastIndexOf(","));
        }

        //处理oracle下主表的时间类型数据
        if (isOracle){
            //去除子表下的model
            List<FieLdsModel> fieLdsModels=new ArrayList<>();
            for (FieLdsModel fieLdsModel:fieLdsModelList){
                if (fieLdsModel.getConfig().getTableName()==null){
                    fieLdsModels.add(fieLdsModel);
                }
            }
            fieldSql=DateInOracle(allDataMap,fieLdsModels);
        }
        mainTableSql.append("UPDATE "+mainTable+" SET "+fieldSql+" WHERE " + pKeyName + "='" + id + "'");

        VisualJDBCUtils.custom(linkEntity,mainTableSql.toString());
        //更新子表sql语句
        StringBuilder allDelSql = new StringBuilder();
        if (childFileMap.size()>0){
            //转换成带表名的数据格式
            Map<String,Object> childFieldWithTable = new HashMap<>(16);

            for (Map.Entry entry : childFileMap.entrySet()){
                //控件所对应的子表名
                for (FieLdsModel fieLdsModel :fieLdsModelList){
                    if (fieLdsModel.getConfig().getJnpfKey().equals("table")){
                       if (fieLdsModel.getVModel().equals(entry.getKey())){
                           childFieldWithTable.put(fieLdsModel.getConfig().getTableName(),entry.getValue());
                       }
                    }
                }
            }

            //去除主表
            tableList.remove(0);
            //删除子表数据
            for (TableModel model :tableList){
                //主表字段
                String relationField = model.getRelationField();
                Map<String,Object> relationMap =new HashMap<>();
                //key统一转成小写
                for (Map.Entry entry:mainMapList.get(0).entrySet()){
                    relationMap.put(String.valueOf(entry.getKey()).toLowerCase(),entry.getValue());
                }
                String relationFieldValue = String.valueOf(relationMap.get(relationField));

                //子表字段
                String tableField = model.getTableField();
                String childDeleteSql = "DELETE FROM " + model.getTable() + " where " + tableField + "='" + relationFieldValue + "'";
                allDelSql.append(childDeleteSql);

                if (StringUtil.isNotEmpty(allDelSql)){
                    VisualJDBCUtils.custom(linkEntity,allDelSql.toString());
                }
                
                allDelSql=new StringBuilder();
            }


            //插入子表数据
            StringBuilder childFieldSql = new StringBuilder();
            StringBuilder childValueSql = new StringBuilder();
            for (Map.Entry entry : childFieldWithTable.entrySet()){
                for (TableModel model :tableList){
                    if (model.getTable().equals(entry.getKey())){
                        //子表外键是否在语句中
                        if (!String.valueOf(childFieldSql).contains(model.getTableField())){
                            String relationField = model.getRelationField();
                            //key统一转成小写
                            Map<String,Object> relationMap =new HashMap<>();
                            for (Map.Entry entry1:mainMapList.get(0).entrySet()){
                                relationMap.put(String.valueOf(entry1.getKey()).toLowerCase(),entry1.getValue());
                            }
                            String relationFieldValue = String.valueOf(relationMap.get(relationField));
                            childFieldSql.append(model.getTableField()+",");
                            childValueSql.append("'"+relationFieldValue+"',");
                        }
                        //判断子表主键是否在语句中
                        String childPkeyName =VisualUtils.getpKey(conn, model.getTable());
                        if(!VisualUtils.existKey(childFieldSql.toString().toLowerCase().trim(), childPkeyName.toLowerCase())){
                            childFieldSql.append(childPkeyName+",");
                        }
                        childFieldSql.deleteCharAt(childFieldSql.lastIndexOf(","));
                        childValueSql.deleteCharAt(childValueSql.lastIndexOf(","));

                        List<Map<String,Object>>  childDataList = (List<Map<String,Object>>)entry.getValue();
                        //此条数据下的子表数据量
                        for (Map<String,Object> valueMap: childDataList){
                            StringBuilder childFieldDataSql =new StringBuilder();
                            StringBuilder childValueDataSql=new StringBuilder();
                            //此条数据的字段
                            for (Map.Entry entry1 :valueMap.entrySet()){
                                childFieldDataSql.append(entry1.getKey().toString()+",")  ;
                                childValueDataSql.append("'"+String.valueOf(entry1.getValue()) +"',");
                            }
                            if (isOracle){
                                for (FieLdsModel fieLdsModel :fieLdsModelList){
                                    if (fieLdsModel.getConfig().getJnpfKey().equals("table")){
                                        if (fieLdsModel.getConfig().getTableName().equals(entry.getKey())){
                                            List<FieLdsModel> childFieldsModelList= fieLdsModel.getConfig().getChildren();
                                            childValueDataSql=ChildDataInOracle(valueMap,childFieldsModelList);
                                        }
                                    }
                                }
                            }
                            String allInsertSql="INSERT INTO " + model.getTable() + "(" +childFieldDataSql + childFieldSql.toString() + ") " + " VALUES ("+childValueDataSql + childValueSql.toString()+",'"+RandomUtil.uuId()+"'" +")";
                            if(!VisualUtils.existKey(childFieldSql.toString().toLowerCase().trim(), childPkeyName.toLowerCase())){
                                allInsertSql="INSERT INTO " + model.getTable() + "(" +childFieldDataSql + childFieldSql.toString() + ") " + " VALUES ("+childValueDataSql + childValueSql.toString() +")";
                            }
                            if (StringUtil.isNotEmpty(allInsertSql)){
                                VisualJDBCUtils.custom(linkEntity,allInsertSql);
                            }
                        }
                    }
                }
                childFieldSql=new StringBuilder();
                childValueSql=new StringBuilder();
            }
            }
        return true;
    }

    public static Map<String, Object> swapTimeType(Map<String, Object> allDataMap, List<FieLdsModel> fieLdsModelList){
        for (FieLdsModel fieLdsModel :fieLdsModelList){
            for (Map.Entry entry:allDataMap.entrySet()){
               if (fieLdsModel.getVModel().equals(entry.getKey()) && entry.getValue()!=null){
                   if (StringUtil.isNotEmpty(fieLdsModel.getFormat())){
                       if (!fieLdsModel.getFormat().equals("HH:mm:ss")){
                           SimpleDateFormat sdf=new SimpleDateFormat(fieLdsModel.getFormat());
                           String sd = sdf.format(entry.getValue());
                           entry.setValue(sd);
                       }
                   }
                   //处理子表的时间类型数据
                   if (fieLdsModel.getConfig().getJnpfKey().equals("table")){
                       List<FieLdsModel> fieLdsModels = fieLdsModel.getConfig().getChildren();
                       List<Map<String,Object>> entryList = (List<Map<String,Object>>)entry.getValue();
                       for (FieLdsModel model:fieLdsModels){
                           for (Map<String,Object> dataMap:entryList){
                               for (Map.Entry childEntry:dataMap.entrySet()){
                                   if (childEntry.getKey().equals(model.getVModel())&& childEntry.getValue()!=null){
                                       if (StringUtil.isNotEmpty(model.getFormat())) {
                                           if (!model.getFormat().equals("HH:mm:ss")) {
                                               SimpleDateFormat sdf = new SimpleDateFormat(model.getFormat());
                                               String sd = sdf.format(childEntry.getValue());
                                               childEntry.setValue(sd);
                                           }
                                       }
                                   }
                               }
                           }
                       }
                   }
               }
            }
        }
        return allDataMap;
    }

    /**
     * 转换oracle中时间类型的sql语句
     * @param allDataMap
     * @param fieLdsModelList
     * @return
     */
    public static StringBuilder DateInOracle(Map<String, Object> allDataMap,List<FieLdsModel> fieLdsModelList){

        StringBuilder fieldSql =new StringBuilder();
        for (FieLdsModel fieLdsModel :fieLdsModelList){
           for (Map.Entry entry :allDataMap.entrySet()){
               if (entry.getKey().equals(fieLdsModel.getVModel())){
                   if (entry.getValue()!=null){
                   if (fieLdsModel.getConfig().getJnpfKey().equals("date")){
                       if (fieLdsModel.getType().equals("date")){
                           fieldSql.append(entry.getKey() + " = " + "TO_DATE('"+entry.getValue()+"', 'yyyy-mm-dd'),");
                       }else if (fieLdsModel.getType().equals("datetime")){
                           fieldSql.append(entry.getKey() + " = " + "TO_DATE('"+entry.getValue()+"', 'yyyy-mm-dd HH24:mi:ss'),");
                       }
                   }else if (fieLdsModel.getConfig().getJnpfKey().equals("createTime") ||fieLdsModel.getConfig().getJnpfKey().equals("modifyTime")){
                       fieldSql.append(entry.getKey() + " = " + "TO_DATE('"+entry.getValue()+"', 'yyyy-mm-dd HH24:mi:ss'),");
                   }
                   else {
                       fieldSql.append(entry.getKey() + " = '" + entry.getValue()+"',");
                   }
               }else {
                       fieldSql.append(entry.getKey() + " = " + entry.getValue()+",");
                   }
           }
           }
        }
        fieldSql= fieldSql.deleteCharAt(fieldSql.lastIndexOf(","));
        return fieldSql;
    }

    public static StringBuilder ChildDataInOracle(Map<String, Object> allDataMap,List<FieLdsModel> fieLdsModelList){
        StringBuilder fieldSql =new StringBuilder();
        for (FieLdsModel fieLdsModel :fieLdsModelList){
            for (Map.Entry entry :allDataMap.entrySet()){
                if (entry.getKey().equals(fieLdsModel.getVModel())){
                    if (fieLdsModel.getConfig().getJnpfKey().equals("date")){
                        if (fieLdsModel.getType().equals("date")){
                            fieldSql.append("TO_DATE('"+entry.getValue()+"', 'yyyy-mm-dd'),");
                        }else if (fieLdsModel.getType().equals("datetime")){
                            fieldSql.append("TO_DATE('"+entry.getValue()+"', 'yyyy-mm-dd HH24:mi:ss'),");
                        }
                    }else {
                        fieldSql.append("'"+ entry.getValue()+"',");
                    }
                }
            }
        }
        return fieldSql;
    }
}
