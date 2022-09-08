package jnpf.onlinedev.util;

import com.alibaba.fastjson.JSON;
import jnpf.database.model.DbLinkEntity;
import jnpf.base.entity.ProvinceEntity;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.service.DataInterfaceService;
import jnpf.base.service.ProvinceService;
import jnpf.base.service.VisualdevService;
import jnpf.base.util.VisualUtils;
import jnpf.database.exception.DataException;
import jnpf.model.visiual.DataTypeConst;
import jnpf.model.visiual.JnpfKeyConsts;
import jnpf.model.visiual.fields.FieLdsModel;
import jnpf.onlinedev.entity.VisualdevModelDataEntity;
import jnpf.onlinedev.model.VisualdevModelDataInfoVO;
import jnpf.onlinedev.service.VisualdevModelDataService;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.context.SpringContext;
import lombok.Cleanup;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 处理获取单条数据
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021-03-26
 */
public class TableInfoUtil {
    public static VisualdevModelDataInfoVO getTableInfoDataChange(String id,List<FieLdsModel> modelList,List<Map<String, Object>> tableMapList, String mainTable,DbLinkEntity linkEntity,String methods) throws DataException, SQLException, ParseException, IOException {
        @Cleanup Connection conn = VisualUtils.getDataConn(linkEntity);
        //获取主键
        String pKeyName = VisualUtils.getpKey(conn, mainTable);

        StringBuilder mainfeild = new StringBuilder();
        String main = "select * from" + " " + tableMapList.get(0).get("table") + " where " + pKeyName + "='" + id + "'";
        List<Map<String, Object>> mainMap = VisualUtils.getTableDataInfo(main,linkEntity);
        //记录全部主表数据
        if (mainMap != null && mainMap.size() > 0) {
            Map<String, Object> dataMap = mainMap.get(0);
            //转换全大写
            dataMap = VisualUtils.toLowerKey(dataMap);
            //记录列表展示的主表数据
            Map<String, Object> newDataMap = new HashMap<>(16);
            Map<String, Object> newMainDataMap = new HashMap<>(16);
            for (FieLdsModel model : modelList) {
                if ("table".equals(model.getConfig().getJnpfKey())) {
                    StringBuilder feilds = new StringBuilder();
                    List<FieLdsModel> childModelList = JsonUtil.getJsonToList(model.getConfig().getChildren(), FieLdsModel.class);
                    for (FieLdsModel childModel : childModelList) {
                        feilds.append(childModel.getVModel() + ",");
                    }
                    if (childModelList.size() > 0) {
                        feilds.deleteCharAt(feilds.length() - 1);
                    }
                    //子表字段
                    String relationFeild;
                    //主表字段
                    String relationMainFeild;
                    String relationMainFeildValue;
                    //查询子表数据
                    StringBuilder childSql = new StringBuilder();
                    childSql.append("select " + feilds + " from" + " " + model.getConfig().getTableName() + " where 1=1");
                    for (Map<String, Object> tableMap : tableMapList) {
                        if (tableMap.get("table").toString().equals(model.getConfig().getTableName())) {
                            relationFeild = tableMap.get("tableField").toString();
                            relationMainFeild = tableMap.get("relationField").toString();
                            if (dataMap.containsKey(relationMainFeild)) {
                                relationMainFeildValue = dataMap.get(relationMainFeild).toString();
                                childSql.append(" And " + relationFeild + "='" + relationMainFeildValue + "'");
                            }
                            //子表字段全转换成小写
                            List<Map<String, Object>> childList = VisualUtils.getTableDataInfo(childSql.toString(),linkEntity);
                            childList = VisualUtils.toLowerKeyList(childList);
                            newDataMap.put(model.getVModel(), childList);
                        }
                    }
                } else {
                    mainfeild.append(model.getVModel() + ",");
                }
            }
            if (modelList.size() > 0) {
                mainfeild.deleteCharAt(mainfeild.length() - 1);
            }
            for (FieLdsModel fieLdsModel : modelList) {
                for (Map.Entry<String, Object> entryMap : dataMap.entrySet()) {
                    if (entryMap.getKey().equals(fieLdsModel.getVModel())) {
                        if (mainfeild.toString().contains(entryMap.getKey())) {
                            if (JnpfKeyConsts.UPLOADFZ.equals(fieLdsModel.getConfig().getJnpfKey()) || JnpfKeyConsts.UPLOADIMG.equals(fieLdsModel.getConfig().getJnpfKey())) {
                                List<Map<String, Object>> map = JsonUtil.getJsonToListMap(String.valueOf(entryMap.getValue()));
                                newMainDataMap.put(entryMap.getKey(), map);
                            }else if (JnpfKeyConsts.ADDRESS.equals(fieLdsModel.getConfig().getJnpfKey())) {
                                List<String> user = JsonUtil.getJsonToList(String.valueOf(entryMap.getValue()),String.class);
                                ProvinceService provinceService = SpringContext.getBean(ProvinceService.class);
                                List<ProvinceEntity> provinceEntities = provinceService.infoList(user);
                                List<String> data = new ArrayList<>();
                                for (ProvinceEntity provinceEntity : provinceEntities){
                                    String fullName = provinceEntity.getFullName();
                                    data.add(fullName);
                                }
                                entryMap.setValue(String.join(",",data));
                                newMainDataMap.put(entryMap.getKey(), entryMap.getValue());

                            }else if (JnpfKeyConsts.CHECKBOX.equals(fieLdsModel.getConfig().getJnpfKey())) {
                                List<String> list = JsonUtil.getJsonToList(String.valueOf(entryMap.getValue()), String.class);
                                newMainDataMap.put(entryMap.getKey(), list);
                            } else if (JnpfKeyConsts.CREATETIME.equals(fieLdsModel.getConfig().getJnpfKey()) || JnpfKeyConsts.MODIFYTIME.equals(fieLdsModel.getConfig().getJnpfKey())) {
                                newMainDataMap.put(entryMap.getKey(), String.valueOf(entryMap.getValue()));
                            } else if (fieLdsModel.getConfig().getJnpfKey().contains("date") && entryMap.getValue() != null) {
                                if (entryMap.getValue().toString().contains(",")) {
                                    List<String> list = JsonUtil.getJsonToList(String.valueOf(entryMap.getValue()), String.class);
                                    List<String> newList = new ArrayList<>();
                                    SimpleDateFormat sdf = new SimpleDateFormat(fieLdsModel.getFormat());
                                    for (String dateStr : list) {
                                        Long s = sdf.parse(dateStr).getTime();
                                        newList.add(s.toString());
                                    }
                                    newMainDataMap.put(entryMap.getKey(), newList);
                                } else {
                                    SimpleDateFormat sdf = new SimpleDateFormat(fieLdsModel.getFormat());
                                    Long s = sdf.parse(entryMap.getValue().toString()).getTime();
                                    newMainDataMap.put(entryMap.getKey(), s);
                                }
                            }//关联表单数据转换
                            else if (JnpfKeyConsts.RELATIONFORM.equals(fieLdsModel.getConfig().getJnpfKey())){
                                //取关联表单数据
                                VisualdevService visualdevService = SpringContext.getBean(VisualdevService.class);
                                VisualdevEntity entity = visualdevService.getInfo(fieLdsModel.getModelId());
                                List<Map<String, Object>> tables = JsonUtil.getJsonToListMap(entity.getTables());
                                if (tables.size()>0){
                                    //取关联主表
                                    String relationMainTable = VisualUtils.getMainTable(tables).get("table").toString();
                                    newMainDataMap.put(entryMap.getKey()+"_id",entryMap.getValue());
                                    String relationField = fieLdsModel.getRelationField();
                                    //取关联主键
                                    String pkeyName =VisualUtils.getpKey(conn,relationMainTable);
                                    String sql = "select " + relationField + " from " +relationMainTable + " where " + pkeyName +"='"+entryMap.getValue()+"'";
                                    List<Map<String, Object>> list = VisualUtils.getTableDataInfo(sql,linkEntity);
                                    if (list.size()>0){
                                        entryMap.setValue(list.get(0).get(relationField));
                                    }
                                }else {
                                    VisualdevModelDataService visualdevModelDataService = SpringContext.getBean(VisualdevModelDataService.class);
                                    newMainDataMap.put(entryMap.getKey()+"_id",entryMap.getValue());
                                    if (dataMap.get(fieLdsModel.getVModel())!=null){
                                        VisualdevModelDataInfoVO vo = visualdevModelDataService.infoDataChange(String.valueOf(dataMap.get(fieLdsModel.getVModel())), entity);
                                        if (vo!=null) {
                                            Map<String, Object> data = JsonUtil.stringToMap(vo.getData());
                                            entryMap.setValue(data.get(fieLdsModel.getRelationField()));
                                            }
                                        }
                                    }
                                    newMainDataMap.put(entryMap.getKey(), entryMap.getValue());
                            }//弹窗
                            else if (JnpfKeyConsts.POPUPSELECT.equals(fieLdsModel.getConfig().getJnpfKey())){
                                DataInterfaceService dataInterfaceService = SpringContext.getBean(DataInterfaceService.class);
                                Object data = dataInterfaceService.infoToId(fieLdsModel.getInterfaceId()).getData();
                                List<Map<String, Object>> mapList =(List<Map<String, Object>>) data;
                                for (Map<String,Object> map :mapList){
                                    if (map.get(fieLdsModel.getPropsValue()).equals(entryMap.getValue())){
                                        //列表默认取第一个字段的值
                                        String s = fieLdsModel.getColumnOptions().get(0).getValue();
                                        entryMap.setValue(map.get(s));
                                    }
                                }
                                newMainDataMap.put(entryMap.getKey(),entryMap.getValue());
                            }
                            else {
                                newMainDataMap.put(entryMap.getKey(), entryMap.getValue());
                            }
                        }
                    }
                }
            }
            //添加主表数据
            newDataMap.put("main", JSON.toJSONString(newMainDataMap));
            int t = 0;
            //转换主表和子表数据
            for (FieLdsModel fieLdsModel : modelList) {
                for (Map.Entry<String, Object> entryMap : newDataMap.entrySet()) {
                    //转换为真实数据
                    if (entryMap.getKey().equals(fieLdsModel.getVModel()) || "main".equals(entryMap.getKey())) {
                        List<VisualdevModelDataEntity> list = new ArrayList<>();
                        //将查询的关键字json转成map
                        Map<String, Object> keyJsonMap = new HashMap<>(16);
                        List<FieLdsModel> formDatas;
                        if ("main".equals(entryMap.getKey()) && t == 0) {
                            formDatas = modelList;
                            VisualdevModelDataEntity entity = new VisualdevModelDataEntity();
                            entity.setData(entryMap.getValue().toString());
                            list.add(entity);
                            t++;
                            //将关键字查询传输的id转换成名称
                            Map<String, Object> keyAndList;
                            if (methods.equals("list")){
                                keyAndList = KeyDataUtil.getKeyData(formDatas, keyJsonMap, list, id,0);
                            }else{
                                keyAndList = KeyDataUtil.getKeyData(formDatas, keyJsonMap, list, id,1);
                            }

                            //替换静态数据模板选项值
                            keyAndList = ListDataHandler.getRealData(formDatas, keyJsonMap, JsonUtil.getJsonToList(keyAndList.get(DataTypeConst.LIST), VisualdevModelDataEntity.class));
                            //系统自动生成字段转换
                            list = VisualUtils.stringToList(formDatas, JsonUtil.getJsonToList(keyAndList.get(DataTypeConst.LIST), VisualdevModelDataEntity.class));
                            //字符串转数组
                            list = AutoFeildsUtil.autoFeildsList(formDatas, list);
                            entryMap.setValue(list);
                        } else if (entryMap.getKey().equals(fieLdsModel.getVModel())) {
                            formDatas = JsonUtil.getJsonToList(fieLdsModel.getConfig().getChildren(), FieLdsModel.class);
                            List<Map<String, Object>> childMapList = (List<Map<String, Object>>) entryMap.getValue();
                            for (Map<String, Object> objectMap : childMapList) {
                                VisualdevModelDataEntity entity = new VisualdevModelDataEntity();
                                entity.setData(JsonUtilEx.getObjectToString(objectMap));
                                list.add(entity);
                            }
                            Map<String, Object> keyAndList;
                            //将关键字查询传输的id转换成名称
                            if (methods.equals("list")){
                                keyAndList = KeyDataUtil.getKeyData(formDatas, keyJsonMap, list, id,0);
                            }else{
                                keyAndList = KeyDataUtil.getKeyData(formDatas, keyJsonMap, list, id,1);
                            }
                            //替换静态数据模板选项值
                            keyAndList = ListDataHandler.getRealData(formDatas, keyJsonMap, JsonUtil.getJsonToList(keyAndList.get(DataTypeConst.LIST), VisualdevModelDataEntity.class));
                            //系统自动生成字段转换
                            list = VisualUtils.stringToList(formDatas, JsonUtil.getJsonToList(keyAndList.get(DataTypeConst.LIST), VisualdevModelDataEntity.class));
                            //字符串转数组
                            list = AutoFeildsUtil.autoFeildsList(formDatas, list);
                            entryMap.setValue(list);
                        }
                    }
                }
            }
            List<VisualdevModelDataEntity> mainList = (List<VisualdevModelDataEntity>) newDataMap.get("main");

            Map<String, Object> realDataMap = JsonUtil.stringToMap(mainList.get(0).getData());
            newDataMap.remove("main");

            for (Map.Entry<String, Object> entryMap : newDataMap.entrySet()) {
                List<Map<String,Object>> childMapList = new ArrayList<>();
                List<VisualdevModelDataEntity> list =(List<VisualdevModelDataEntity>)entryMap.getValue();
                for (VisualdevModelDataEntity vo:list){
                    Map<String, Object> map = JsonUtil.stringToMap(vo.getData());
                    childMapList.add(map);
                }
                entryMap.setValue(childMapList);
            }
            realDataMap.putAll(newDataMap);
            VisualdevModelDataInfoVO vo = new VisualdevModelDataInfoVO();
            String data = JsonUtilEx.getObjectToString(realDataMap);
            vo.setData(data);
            vo.setId(dataMap.get(pKeyName.toLowerCase()).toString());
            return vo;
        }
        return null;
    }
    public static VisualdevModelDataInfoVO getTableInfo(String pKeyName,StringBuilder mainfeild,List<FieLdsModel> modelList,List<Map<String, Object>> mainMap, List<Map<String, Object>> tableMapList,DbLinkEntity linkEntity) throws DataException, SQLException, ParseException, IOException {
        if (mainMap != null & mainMap.size() > 0) {
            //记录全部主表数据
            Map<String, Object> dataMap = mainMap.get(0);
            //转换全小写
            dataMap = VisualUtils.toLowerKey(dataMap);
            //记录列表展示的主表数据
            Map<String, Object> newDataMap = new HashMap<>(16);
            for (FieLdsModel model : modelList) {
                if (JnpfKeyConsts.CHILD_TABLE.equals(model.getConfig().getJnpfKey())) {
                    StringBuilder feilds = new StringBuilder();
                    List<FieLdsModel> childModelList = JsonUtil.getJsonToList(model.getConfig().getChildren(), FieLdsModel.class);
                    for (FieLdsModel childModel : childModelList) {
                        feilds.append(childModel.getVModel() + ",");
                    }
                    if (childModelList.size() > 0) {
                        feilds.deleteCharAt(feilds.length() - 1);
                    }
                    //子表字段
                    String relationFeild = "";
                    //主表字段
                    String relationMainFeild = "";
                    String relationMainFeildValue = "";
                    //查询子表数据
                    StringBuilder childSql = new StringBuilder();
                    childSql.append("select " + feilds + " from" + " " + model.getConfig().getTableName() + " where 1=1");
                    for (Map<String, Object> tableMap : tableMapList) {
                        if (tableMap.get("table").toString().equals(model.getConfig().getTableName())) {
                            relationFeild = tableMap.get("tableField").toString();
                            relationMainFeild = tableMap.get("relationField").toString();
                            if (dataMap.containsKey(relationMainFeild)) {
                                relationMainFeildValue = dataMap.get(relationMainFeild).toString();
                                childSql.append(" And " + relationFeild + "='" + relationMainFeildValue + "'");
                            }
                            //子表字段全转换成小写
                            List<Map<String, Object>> childList = VisualUtils.getTableDataInfo(childSql.toString(),linkEntity);
                            childList = VisualUtils.toLowerKeyList(childList);
                            newDataMap.put(model.getVModel(), childList);
                        }
                    }
                } else {
                    mainfeild.append(model.getVModel() + ",");
                }
            }
            if (modelList.size() > 0) {
                mainfeild.deleteCharAt(mainfeild.length() - 1);
            }
            //主表数据和子表分开转换
            //1.转换子表
            for (FieLdsModel fieLdsModel : modelList) {
                for (Map.Entry<String, Object> entryMap : newDataMap.entrySet()) {
                    if (JnpfKeyConsts.CHILD_TABLE.equals(fieLdsModel.getConfig().getJnpfKey()) && entryMap.getKey().equals(fieLdsModel.getVModel())) {
                        List<FieLdsModel> childFormList = JsonUtil.getJsonToList(fieLdsModel.getConfig().getChildren(), FieLdsModel.class);
                        List<Map<String, Object>> childDataMap = (List<Map<String, Object>>) entryMap.getValue();
                        newDataMap.put(fieLdsModel.getVModel(), VisualUtils.swapTableDataInfoList(childFormList, childDataMap));
                    }
                }
            }
            //2.去除模板里的子表字段,转换主表数据
            List<FieLdsModel> mainFormList = modelList.stream().filter(t -> !JnpfKeyConsts.CHILD_TABLE.equals(t.getConfig().getJnpfKey())).collect(Collectors.toList());
            newDataMap.putAll(VisualUtils.swapTableDataInfoOne(mainFormList, dataMap));

            String data = AutoFeildsUtil.autoFeilds(modelList, JsonUtilEx.getObjectToString(newDataMap));
            VisualdevModelDataInfoVO vo = new VisualdevModelDataInfoVO();
            vo.setData(data);
            vo.setId(dataMap.get(pKeyName.toLowerCase()).toString());
            return vo;
        }
        return null;
    }

    public static List<VisualdevModelDataEntity> NoTableInfo(List<FieLdsModel> modelList,List<VisualdevModelDataEntity> list,String dataAll){
        Map<String,Object> dataMapAll =  JsonUtil.stringToMap(dataAll);
        Map<String, Object> newMainDataMap = new HashMap<>(16);
        for (VisualdevModelDataEntity dataEntity : list){
            Map<String, Object> dataMap = JsonUtil.stringToMap(dataEntity.getData());
            for (FieLdsModel fieLdsModel : modelList) {
                if (fieLdsModel.getConfig().getJnpfKey().equals(JnpfKeyConsts.ADDRESS)){
                    String ids=String.valueOf(dataMap.get(fieLdsModel.getVModel()));
                    List<String> idList = JsonUtil.getJsonToList(ids, String.class);
                    ProvinceService provinceService = SpringContext.getBean(ProvinceService.class);
                    List<ProvinceEntity> provinceEntities = provinceService.infoList(idList);
                    List<String> data = new ArrayList<>();
                    for (ProvinceEntity provinceEntity : provinceEntities){
                        String fullName = provinceEntity.getFullName();
                        data.add(fullName);
                    }
                    newMainDataMap.put(fieLdsModel.getVModel(),String.join(",",data) );
                }else if (fieLdsModel.getConfig().getJnpfKey().equals(JnpfKeyConsts.RELATIONFORM)){
                    newMainDataMap.put(fieLdsModel.getVModel()+"_id",String.valueOf(dataMapAll.get(fieLdsModel.getVModel())));
                }
            }
            dataMap.putAll(newMainDataMap);
            dataEntity.setData(JsonUtil.getObjectToString(dataMap));
        }
        return list;
    }

    public static List<Map<String,Object>> NoTableSwapData(List<Map<String,Object>> dataList, List<FieLdsModel> modelList){
        List<Map<String, Object>> dataArrayList= new ArrayList<>();
        for (Map<String,Object> controlsMap : dataList ){
            for ( FieLdsModel fieLdsModel : modelList){
                if (fieLdsModel.getConfig().getJnpfKey().equals(JnpfKeyConsts.ADDRESS)){
                    String ids=String.valueOf (controlsMap.get(fieLdsModel.getVModel()));
                    List<String> idList = JsonUtil.getJsonToList(ids, String.class);
                    ProvinceService provinceService = SpringContext.getBean(ProvinceService.class);
                    List<ProvinceEntity> provinceEntities = provinceService.infoList(idList);
                    List<String> data = new ArrayList<>();
                    for (ProvinceEntity provinceEntity : provinceEntities){
                        String fullName = provinceEntity.getFullName();
                        data.add(fullName);
                    }
                    controlsMap.put(fieLdsModel.getVModel(),String.join(",",data) );
                }else if (fieLdsModel.getConfig().getJnpfKey().equals(JnpfKeyConsts.RELATIONFORM)){
                    controlsMap.put(fieLdsModel.getVModel()+"_id",String.valueOf(controlsMap.get(fieLdsModel.getVModel())));
                }else if (fieLdsModel.getConfig().getJnpfKey().equals(JnpfKeyConsts.POPUPSELECT)){
                    DataInterfaceService dataInterfaceService = SpringContext.getBean(DataInterfaceService.class);
                    Object data = dataInterfaceService.infoToId(fieLdsModel.getInterfaceId()).getData();
                    List<Map<String, Object>> mapList =(List<Map<String, Object>>) data;
                    for (Map<String,Object> map :mapList){
                        for (Map.Entry entry :controlsMap.entrySet()){
                            if (map.get(fieLdsModel.getPropsValue()).equals(entry.getValue())){
                                //列表默认取第一个字段的值
                                String s = fieLdsModel.getColumnOptions().get(0).getValue();
                                entry.setValue(map.get(s));
                            }
                        }
                    }
                }
            }
            dataArrayList.add(controlsMap);
        }
        return dataArrayList;
    }

}
