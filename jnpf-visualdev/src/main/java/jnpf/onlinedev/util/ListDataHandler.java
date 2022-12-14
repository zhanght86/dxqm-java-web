package jnpf.onlinedev.util;

import com.alibaba.fastjson.JSON;
import jnpf.database.model.DbLinkEntity;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.util.DynamicUtil;
import jnpf.base.util.VisualUtils;
import jnpf.database.exception.DataException;
import jnpf.model.visiual.FormDataModel;
import jnpf.model.visiual.JnpfKeyConsts;
import jnpf.model.visiual.TimeControl;
import jnpf.model.visiual.fields.FieLdsModel;
import jnpf.model.visiual.fields.props.PropsBeanModel;
import jnpf.onlinedev.entity.VisualdevModelDataEntity;
import jnpf.model.visiual.DataTypeConst;
import jnpf.onlinedev.model.OnlineDevListModel.VisualColumnSearchVO;
import jnpf.onlinedev.model.VisualdevModelDataInfoVO;
import jnpf.util.CacheKeyUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RedisUtil;
import jnpf.util.StringUtil;
import jnpf.util.context.SpringContext;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 在线开发列表数据处理
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021-03-15
 */
public class ListDataHandler {

    /**
     * 在线开发数据处理
     * @param keyJsonMap
     * @param visualdevEntity
     * @param list
     * @param realList
     * @return
     * @throws SQLException
     * @throws DataException
     * @throws ParseException
     * @throws IOException
     */
    public static List<Map<String, Object>> swapListData(Map<String, Object> keyJsonMap, VisualdevEntity visualdevEntity, List<VisualdevModelDataEntity> list, List<Map<String, Object>> realList, DbLinkEntity linkEntity) throws SQLException, DataException, ParseException, IOException {
        FormDataModel formDataModel = JsonUtil.getJsonToBean(visualdevEntity.getFormData(), FormDataModel.class);
        List<FieLdsModel> formData = JsonUtil.getJsonToList(JsonUtil.getJsonToJsonArray(formDataModel.getFields()), FieLdsModel.class);
        //去除模板多级控件
        formData = VisualUtils.deleteMore(formData);
        //将关键字查询传输的id转换成名称
        Map<String, Object> keyAndList = KeyDataUtil.getKeyData(formData, keyJsonMap, list, visualdevEntity.getId(),0);
        //保存需要转换的时间字段
        TimeControl timeControl = (TimeControl) keyAndList.get(DataTypeConst.TIME_CONTROL);

        //替换静态数据模板选项值
        keyAndList = getRealData(formData, (Map<String, Object>) keyAndList.get(DataTypeConst.KEY_JSON_MAP), JsonUtil.getJsonToList(keyAndList.get(DataTypeConst.LIST), VisualdevModelDataEntity.class));
        //系统自动生成字段转换
        list = VisualUtils.stringToList(formData, JsonUtil.getJsonToList(keyAndList.get(DataTypeConst.LIST), VisualdevModelDataEntity.class));
        //字符串转数组
        list = AutoFeildsUtil.autoFeildsList(formData, list);
        keyJsonMap = JsonUtil.entityToMap(keyAndList.get(DataTypeConst.KEY_JSON_MAP));

        //获取各个字段对应的搜索类型
        Map<String, Object> columnDataMap = JsonUtil.stringToMap(visualdevEntity.getColumnData());
        List<VisualColumnSearchVO> searchVOList = JsonUtil.getJsonToList(columnDataMap.get("searchList"), VisualColumnSearchVO.class);
        //取出关键字字段
        List<VisualColumnSearchVO> voList = null;
        if (keyJsonMap!=null) {
            List<String> keyJsonList = new ArrayList<>(keyJsonMap.keySet());
            voList = searchVOList.parallelStream().filter(vo -> keyJsonList.contains(vo.getVModel())).collect(Collectors.toList());
            for (VisualColumnSearchVO vo : voList) {
                keyJsonMap.entrySet().stream().forEach(key -> {
                    if (key.getKey().equals(vo.getVModel())) {
                        vo.setValue(key.getValue());
                    }
                });
            }
        }
        //关键字过滤
        realList = VisualUtils.getRealList(voList, list, timeControl);

        List<Map<String, Object>> tableMapList = JsonUtil.getJsonToListMap(visualdevEntity.getTables());
        if (tableMapList.size()>0){
            //取主表
            String mainTable = VisualUtils.getMainTable(tableMapList).get("table").toString();
            //数据转换
            List<Map<String,Object>> realMapList = new ArrayList<>();
            for (Map<String,Object> dataMap : realList){
                String id=null;
                if (dataMap.get("id")!=null){
                    id=String.valueOf(dataMap.get("id"));
                }else if (dataMap.get("f_id")!=null){
                    id=String.valueOf(dataMap.get("f_id"));
                }
                VisualdevModelDataInfoVO tableInfoDataChange = TableInfoUtil.getTableInfoDataChange(id, formData, tableMapList, mainTable,linkEntity,"list");
                Map<String, Object> map = JsonUtil.stringToMap(tableInfoDataChange.getData());
                map.put("id",id);
                realMapList.add(map);
            }
            return realMapList;
        }else {
            return TableInfoUtil.NoTableSwapData(realList,formData);
        }
    }


    /**
     * 替换列表的选项值
     * @param formData
     * @param keyJsonMap
     * @param list
     * @return
     * @throws IOException
     */
    public static Map<String, Object> getRealData(List<FieLdsModel> formData, Map<String, Object> keyJsonMap, List<VisualdevModelDataEntity> list) throws IOException {
        RedisUtil redisUtil = SpringContext.getBean(RedisUtil.class);
        CacheKeyUtil cacheKeyUtil=SpringContext.getBean(CacheKeyUtil.class);

        for (FieLdsModel formModel : formData) {
            if (cacheKeyUtil.getDynamic().equals(formModel.getConfig().getDataType())) {
                redisUtil.remove(cacheKeyUtil.getDynamic() + formModel.getConfig().getPropsUrl());
            }
        }
        //存储远端数据的字段数据
        Map<String, FieLdsModel> dynamicDataMap = new HashMap<>(16);
        //添加远端数据
        for (FieLdsModel fieLdsModel : formData) {
            String type = fieLdsModel.getConfig().getDataType();
            String dynamicId = fieLdsModel.getConfig().getPropsUrl();
            if (DataTypeConst.DYNAMIC.equals(type) && StringUtil.isNotEmpty(dynamicId)) {
                dynamicDataMap.put(dynamicId, fieLdsModel);
            }
        }
        for (Map.Entry<String, FieLdsModel> entry : dynamicDataMap.entrySet()) {
            DynamicUtil dynamicUtil = new DynamicUtil();
            entry.setValue(dynamicUtil.dynamicData(entry.getValue()));
        }

        for (FieLdsModel fieLdsModel : formData) {
            for (VisualdevModelDataEntity entity : list) {
                //真实数据
                Map<String, Object> dataMap = JsonUtil.stringToMap(entity.getData());
                for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                    String type = fieLdsModel.getConfig().getDataType();
                    if (DataTypeConst.DICTIONARY.equals(type) && entry.getValue() != null) {
                        if (entry.getKey().equals(fieLdsModel.getVModel())) {
                            //字段数据id
                            List<String> fieldList = VisualUtils.analysisField(String.valueOf(entry.getValue()));
                            String fieldStr = String.valueOf(entry.getValue());

                            if (JnpfKeyConsts.CASCADER.equals(fieLdsModel.getConfig().getJnpfKey())) {
                                //为级联选择框的列表和查询字段赋值
                                Map<String, Object> cascaderMap = VisualUtils.cascaderOperation(fieLdsModel, fieldList, keyJsonMap);
                                if (cascaderMap.get(DataTypeConst.KEY_JSON_MAP) != null) {
                                    keyJsonMap = JsonUtil.entityToMap(cascaderMap.get(DataTypeConst.KEY_JSON_MAP));
                                }
                                if (cascaderMap.get(DataTypeConst.VALUE) != null) {
                                    entry.setValue(cascaderMap.get(DataTypeConst.VALUE));
                                }
                            }
                            if (JnpfKeyConsts.TREESELECT.equals(fieLdsModel.getConfig().getJnpfKey())) {
                                //给字段数据字段赋值
                                String value = VisualUtils.treeSelectOperation(fieLdsModel, fieldStr);
                                if (StringUtil.isNotEmpty(value)) {
                                    entry.setValue(value);
                                    if(keyJsonMap!=null&&keyJsonMap.containsValue(fieldStr)){
                                        keyJsonMap.put(fieLdsModel.getVModel(),value);
                                    }
                                }
                            }
                            //模板选项集合
                            else if (fieLdsModel.getSlot() != null && StringUtil.isNotEmpty(fieLdsModel.getSlot().getOptions())) {
                                List<Map<String, Object>> options = JsonUtil.getJsonToListMap(fieLdsModel.getSlot().getOptions());
                                PropsBeanModel props = JsonUtil.getJsonToBean(fieLdsModel.getConfig().getProps(), PropsBeanModel.class);
                                //转换
                                String value = VisualUtils.setDicValue(fieldList, fieldStr, props, options, fieLdsModel);
                                if (value != null) {
                                    entry.setValue(value);
                                    if(keyJsonMap!=null&&keyJsonMap.containsValue(fieldStr)){
                                        keyJsonMap.put(fieLdsModel.getVModel(),value);
                                    }
                                }
                            }
                        }
                    } else if (DataTypeConst.STATIC.equals(type) && entry.getValue() != null) {
                        if (entry.getKey().equals(fieLdsModel.getVModel())) {
                            //字段数据id
                            List<String> fieldList = VisualUtils.analysisField(String.valueOf(entry.getValue()));
                            String fieldStr = String.valueOf(entry.getValue());

                            if (JnpfKeyConsts.CASCADER.equals(fieLdsModel.getConfig().getJnpfKey())) {
                                //为级联选择框的列表和查询字段赋值
                                Map<String, Object> cascaderMap = VisualUtils.cascaderOperation(fieLdsModel, fieldList, keyJsonMap);
                                if (cascaderMap.get(DataTypeConst.KEY_JSON_MAP) != null) {
                                    keyJsonMap = JsonUtil.entityToMap(cascaderMap.get(DataTypeConst.KEY_JSON_MAP));
                                }
                                if (cascaderMap.get(DataTypeConst.VALUE) != null) {
                                    entry.setValue(cascaderMap.get(DataTypeConst.VALUE));
                                }
                            } else if (JnpfKeyConsts.TREESELECT.equals(fieLdsModel.getConfig().getJnpfKey())) {
                                String value = VisualUtils.treeSelectOperation(fieLdsModel, fieldStr);
                                if (StringUtil.isNotEmpty(value)) {
                                    entry.setValue(value);
                                    if(keyJsonMap!=null&&keyJsonMap.containsValue(fieldStr)){
                                        keyJsonMap.put(fieLdsModel.getVModel(),value);
                                    }
                                }
                            }

                            //正常多选列表赋值
                            Object selectObj = VisualUtils.setSelect(fieldList, fieldStr, fieLdsModel);
                            if (selectObj != null) {
                                entry.setValue(selectObj);
                                if(keyJsonMap!=null&&keyJsonMap.containsValue(fieldStr)){
                                    keyJsonMap.put(fieLdsModel.getVModel(),selectObj);
                                }
                            }
                        }
                    } else if (DataTypeConst.DYNAMIC.equals(type) && entry.getValue() != null) {
                        String dynamicId = fieLdsModel.getConfig().getPropsUrl();
                        if (dynamicDataMap.containsKey(dynamicId)) {
                            fieLdsModel = dynamicDataMap.get(dynamicId);
                            if (entry.getKey().equals(fieLdsModel.getVModel())) {
                                //字段数据id
                                List<String> fieldList = VisualUtils.analysisField(String.valueOf(entry.getValue()));
                                String fieldStr = String.valueOf(entry.getValue());

                                //为级联选择框的列表和查询字段赋值
                                Map<String, Object> cascaderMap = VisualUtils.cascaderOperation(fieLdsModel, fieldList, keyJsonMap);
                                if (cascaderMap.get(DataTypeConst.KEY_JSON_MAP) != null) {
                                    keyJsonMap = JsonUtil.entityToMap(cascaderMap.get(DataTypeConst.KEY_JSON_MAP));
                                }
                                if (cascaderMap.get(DataTypeConst.VALUE) != null) {
                                    entry.setValue(cascaderMap.get(DataTypeConst.VALUE));
                                }
                                if (JnpfKeyConsts.TREESELECT.equals(fieLdsModel.getConfig().getJnpfKey())) {
                                    //给字段数据字段赋值
                                    String value = VisualUtils.treeSelectOperation(fieLdsModel, fieldStr);
                                    if (StringUtil.isNotEmpty(value)) {
                                        entry.setValue(value);
                                        if(keyJsonMap!=null&&keyJsonMap.containsValue(fieldStr)){
                                            keyJsonMap.put(fieLdsModel.getVModel(),value);
                                        }
                                    }
                                }
                                //正常多选列表赋值
                                Object selectObj = VisualUtils.setSelect(fieldList, fieldStr, fieLdsModel);
                                if (selectObj != null) {
                                    entry.setValue(selectObj);
                                    if(keyJsonMap!=null&&keyJsonMap.containsValue(fieldStr)){
                                        keyJsonMap.put(fieLdsModel.getVModel(),selectObj);
                                    }
                                }
                            }
                        }
                    }
                }
                entity.setData(JSON.toJSON(dataMap).toString());
            }
        }
        Map<String, Object> map = new HashMap<>(16);
        map.put(DataTypeConst.LIST, list);
        map.put(DataTypeConst.KEY_JSON_MAP, keyJsonMap);
        return map;
    }


}
