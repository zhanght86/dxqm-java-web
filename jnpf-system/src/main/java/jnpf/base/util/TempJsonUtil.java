//package jnpf.util;
//
//import jnpf.base.UserInfo;
//import jnpf.base.service.BillRuleService;
//import jnpf.base.service.DataInterfaceService;
//import jnpf.base.service.DictionaryDataService;
//import jnpf.base.service.DictionaryTypeService;
//import jnpf.config.ConfigValueUtil;
//import jnpf.entity.system.base.DictionaryDataEntity;
//import jnpf.entity.system.base.DictionaryTypeEntity;
//import jnpf.entity.system.permission.OrganizeEntity;
//import jnpf.entity.system.permission.PositionEntity;
//import jnpf.entity.system.permission.UserEntity;
//import jnpf.database.exception.DataException;
//import jnpf.model.system.permission.user.UserAllModel;
//import jnpf.model.visualdev.visualdevmodeldata.FormDataModel;
//import jnpf.model.visualdev.visualdevmodeldata.fields.FieLdsModel;
//import jnpf.model.visualdev.visualdevmodeldata.fields.config.ConfigModel;
//import jnpf.permission.service.OrganizeService;
//import jnpf.permission.service.PositionService;
//import jnpf.permission.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Component
//public class TempJsonUtil {
//
//    @Autowired
//    private UserService userService;
//    @Autowired
//    private BillRuleService billRuleService;
//    @Autowired
//    private PositionService positionService;
//    @Autowired
//    private UserProvider userProvider;
//    @Autowired
//    private ConfigValueUtil configValueUtil;
//    @Autowired
//    private DataSourceUtil dataSourceUtil;
//    @Autowired
//    private OrganizeService organizeService;
//    @Autowired
//    private DictionaryTypeService dictionaryTypeService;
//    @Autowired
//    private DictionaryDataService dictionaryDataService;
//    @Autowired
//    private DataInterfaceService dataInterfaceService;
//    @Autowired
//    private VisualdevModelDataService visualdevModelDataService;
//    @Autowired
//    private VisualdevService visualdevService;
//
//    //--------------------------??????----------------------------------------------
//
//    /**
//     * ??????????????????
//     *
//     * @param visualdevEntity ??????
//     * @param tableModelList  ?????????
//     * @param keyJsonMap      ????????????
//     */
//    public List<Map<String, Object>> listTable(VisualdevEntity visualdevEntity, List<FlowTableModel> tableModelList, Map<String, Object> keyJsonMap) throws DataException {
//        Map<String, Object> columnData = JSONUtil.StringToMap(visualdevEntity.getColumnData());
//        List<IndexGridField6Model> modelList = JSONUtil.getJsonToList(columnData.get("columnList").toString(), IndexGridField6Model.class);
//        String mainTable = tableModelList.stream().filter(t -> "1".equals(t.getTypeId())).findFirst().get().getTable();
//        StringBuilder feilds = new StringBuilder();
//        for (IndexGridField6Model model : modelList) {
//            feilds.append(model.getProp() + ",");
//        }
//        if (modelList.size() > 0) {
//            feilds.append("F_ID");
//        }
//        StringBuilder sql = new StringBuilder();
//        sql.append("select " + feilds + " from" + " " + mainTable);
//        if (!StringUtil.isEmpty(columnData.get("defaultSidx").toString())) {
//            sql.append(" order by " + columnData.get("defaultSidx").toString() + " " + columnData.get("sort").toString());
//        } else {
//            sql.append(" order by F_Id " + columnData.get("sort").toString());
//        }
//        List<VisualdevModelDataEntity> list = getTableDataList(sql.toString());
//        List<Map<String, Object>> result = getList(list, visualdevEntity.getFormData(), keyJsonMap, true);
//        return result;
//    }
//
//    /**
//     * ??????????????????
//     *
//     * @param list            ??????
//     * @param visualdevEntity ??????
//     * @param keyJsonMap      ????????????
//     * @return
//     */
//    public List<Map<String, Object>> list(VisualdevEntity visualdevEntity, List<VisualdevModelDataEntity> list, Map<String, Object> keyJsonMap) {
//        List<Map<String, Object>> result = getList(list, visualdevEntity.getFormData(), keyJsonMap, false);
//        return result;
//    }
//
//    /**
//     * ??????????????????
//     *
//     * @param list         ?????????list
//     * @param formDataJson formTempJson
//     * @param keyJsonMap   ????????????
//     * @return
//     */
//    private List<Map<String, Object>> getList(List<VisualdevModelDataEntity> list, String formDataJson, Map<String, Object> keyJsonMap, boolean typeKey) {
//        List<UserAllModel> userAll = userService.getAll();
//        Map<String, String> jnpfKey = new HashMap<>(16);
//        Map<String, Object> keyList = new HashMap<>(16);
//        //formTempJson
//        FormDataModel formDataModels = JSONUtil.getJsonToBean(formDataJson, FormDataModel.class);
//        List<FieLdsModel> fieLdsModelList = JSONUtil.getJsonToList(formDataModels.getFields(), FieLdsModel.class);
//        tempListJson(fieLdsModelList, jnpfKey, keyList, typeKey);
//        //????????????
//        List<Map<String, Object>> realList = new ArrayList<>();
//        boolean flag = keyJsonMap != null ? true : false;
//        for (VisualdevModelDataEntity dataEntity : list) {
//            Map<String, Object> formData = JSONUtil.StringToMap(dataEntity.getData());
//            //???????????????????????????
//            boolean iskey = false;
//            Map<String, Object> resulMap = new HashMap<>(16);
//            if (formData != null) {
//                for (String key : formData.keySet()) {
//                    String modelKey = jnpfKey.get(key);
//                    if (typeKey) {
//                        modelKey = jnpfKey.get(key.toUpperCase());
//                    }
//                    Object formValue = formData.get(key);
//                    List<String> fullName = new ArrayList<>();
//                    if (!iskey && flag) {
//                        for (String jsonKey : keyJsonMap.keySet()) {
//                            if (jsonKey.equals(key)) {
//                                iskey = String.valueOf(formValue).contains(String.valueOf(keyJsonMap.get(jsonKey)));
//                            }
//                        }
//                    }
//                    if ("select".equals(modelKey) || "radio".equals(modelKey)) {
//                        List<OptinModels> optionList = (List<OptinModels>) keyList.get(key);
//                        OptinModels model = optionList.stream().filter(t -> t.getId().equals(formValue)).findFirst().orElse(null);
//                        if (model != null) {
//                            formData.put(key, model.getFullName());
//                        }
//                    }
//                    if ("createUser".equals(modelKey) || "modifyUser".equals(modelKey)) {
//                        UserAllModel userModel = userAll.stream().filter(t -> t.getId().equals(formValue)).findFirst().orElse(null);
//                        if (userModel != null) {
//                            formData.put(key, userModel.getRealName());
//                        }
//                    }
//                    if ("checkbox".equals(modelKey)) {
//                        if (formValue != null) {
//                            List<String> checkbox = new ArrayList<>();
//                            if (formValue instanceof String) {
//                                checkbox = JSONUtil.getJsonToList(String.valueOf(formValue), String.class);
//                            } else {
//                                checkbox = (List<String>) formValue;
//                            }
//                            if (checkbox != null) {
//                                List<OptinModels> optionList = (List<OptinModels>) keyList.get(key);
//                                for (String checkValue : checkbox) {
//                                    OptinModels model = optionList.stream().filter(t -> t.getId().equals(checkValue)).findFirst().orElse(null);
//                                    if (model != null) {
//                                        fullName.add(model.getFullName());
//                                    }
//                                }
//                                formData.put(key, String.join("", fullName));
//                            }
//                        }
//                    }
//                    //???????????????
//                    if ("comSelect".equals(modelKey) || "depSelect".equals(modelKey) || "currDept".equals(modelKey)) {
//                        List<OrganizeEntity> optionList = (List<OrganizeEntity>) keyList.get(key);
//                        OrganizeEntity organize = optionList.stream().filter(t -> t.getId().equals(formValue)).findFirst().orElse(null);
//                        if (organize != null) {
//                            formData.put(key, organize.getFullName());
//                        }
//                    }
//                    //??????
//                    if ("userSelect".equals(modelKey)) {
//                        List<UserEntity> userList = (List<UserEntity>) keyList.get(key);
//                        UserEntity user = userList.stream().filter(t -> t.getId().equals(formValue)).findFirst().orElse(null);
//                        if (user != null) {
//                            formData.put(key, user.getRealName());
//                        }
//                    }
//                    //??????
//                    if ("posSelect".equals(modelKey) || "currPosition".equals(modelKey)) {
//                        List<PositionEntity> positionList = (List<PositionEntity>) keyList.get(key);
//                        PositionEntity position = positionList.stream().filter(t -> t.getId().equals(formValue)).findFirst().orElse(null);
//                        if (position != null) {
//                            formData.put(key, position.getFullName());
//                        }
//                    }
//                    //??????
//                    if ("dicSelect".equals(modelKey)) {
//                        List<DictionaryTypeEntity> dictypeList = (List<DictionaryTypeEntity>) keyList.get(key);
//                        DictionaryTypeEntity dic = dictypeList.stream().filter(t -> t.getId().equals(formValue)).findFirst().orElse(null);
//                        if (dic != null) {
//                            formData.put(key, dic.getFullName());
//                        }
//                    }
//                    //??????
//                    if ("date".equals(modelKey)) {
//                        Object valuse = formData.get(key);
//                        formData.put(key, "");
//                        if (StringUtil.isNotEmpty(String.valueOf(valuse)) && valuse != null) {
//                            if (valuse instanceof String) {
//                                String value = String.valueOf(valuse);
//                                if (StringUtil.isNotEmpty(value)) {
//                                    String dateValue = "1970-01-01";
//                                    if (value.length() > 10) {
//                                        dateValue = DateUtil.daFormat(DateUtil.StringToDate(value));
//                                    } else {
//                                        dateValue = DateUtil.daFormat(DateUtil.StringToDate(value + " 00:00:00"));
//                                    }
//                                    if (!"1970-01-01".equals(dateValue)) {
//                                        formData.put(key, dateValue);
//                                    }
//                                }
//                            } else if (valuse instanceof Long) {
//                                Long value = Long.valueOf(String.valueOf(valuse));
//                                String dateValue = DateUtil.daFormat(value);
//                                formData.put(key, dateValue);
//                            }
//                        }
//                    }
//                    resulMap = formData;
//                }
//            }
//            resulMap.put("id", dataEntity.getId());
//            if (flag) {
//                if (iskey) {
//                    realList.add(resulMap);
//                }
//            } else {
//                realList.add(resulMap);
//            }
//        }
//        return realList;
//    }
//
//    /**
//     * ????????????json
//     *
//     * @param list    ??????json
//     * @param jnpfKey ?????????jnpfkey
//     * @param keyList ??????????????????list
//     */
//    private void tempListJson(List<FieLdsModel> list, Map<String, String> jnpfKey, Map<String, Object> keyList, boolean typeKey) {
//        List<DictionaryDataEntity> dicDatayList = dictionaryDataService.getList();
//        List<OrganizeEntity> organizeList = organizeService.getList();
//        List<UserEntity> userList = userService.getList();
//        List<PositionEntity> positionList = positionService.getList();
//        List<DictionaryTypeEntity> dictypeList = dictionaryTypeService.getList();
//        for (FieLdsModel fieLdsModel : list) {
//            String model = fieLdsModel.getVModel();
//            if (typeKey) {
//                model = model.toUpperCase();
//            }
//            ConfigModel config = fieLdsModel.getConfig();
//            String key = config.getJnpfKey();
//            jnpfKey.put(model, key);
//            if ("select".equals(key) || "checkbox".equals(key) || "radio".equals(key)) {
//                String type = config.getDataType();
//                List<OptinModels> optionslList = new ArrayList<>();
//                String fullName = config.getProps().getLabel();
//                String value = config.getProps().getValue();
//                if ("dictionary".equals(type)) {
//                    String dictionaryType = config.getDictionaryType();
//                    List<DictionaryDataEntity> dicList = dicDatayList.stream().filter(t -> t.getDictionaryTypeId().equals(dictionaryType)).collect(Collectors.toList());
//                    for (DictionaryDataEntity dataEntity : dicList) {
//                        OptinModels optionsModel = new OptinModels();
//                        optionsModel.setId(dataEntity.getId());
//                        optionsModel.setFullName(dataEntity.getFullName());
//                        optionslList.add(optionsModel);
//                    }
//                } else if ("static".equals(type)) {
//                    String optionsJson = "[]";
//                    if (fieLdsModel.getSlot() != null) {
//                        //??????json
//                        optionsJson = fieLdsModel.getSlot().getOptions();
//                    } else if (config.getOptions() != null) {
//                        //??????json
//                        optionsJson = config.getOptions();
//                    }
//                    List<Map<String, Object>> staticList = JSONUtil.getJsonToListMap(optionsJson);
//                    for (Map<String, Object> options : staticList) {
//                        OptinModels optionsModel = new OptinModels();
//                        optionsModel.setId(String.valueOf(options.get(value)));
//                        optionsModel.setFullName(String.valueOf(options.get(fullName)));
//                        optionslList.add(optionsModel);
//                    }
//                } else if ("dynamic".equals(type)) {
//                    String propsUrl = config.getPropsUrl();
//                    //??????????????????
//                    Object object = dataInterfaceService.infoToId(propsUrl);
//                    Map<String, Object> dynamicMap = JSONUtil.EntityToMap(object);
//                    if (dynamicMap.get("data") != null) {
//                        List<Map<String, Object>> dataList = JSONUtil.getJsonToListMap(dynamicMap.get("data").toString());
//                        for (Map<String, Object> options : dataList) {
//                            OptinModels optionsModel = new OptinModels();
//                            optionsModel.setId(String.valueOf(options.get(value)));
//                            optionsModel.setFullName(String.valueOf(options.get(fullName)));
//                            optionslList.add(optionsModel);
//                        }
//                    }
//                }
//                keyList.put(model, optionslList);
//            }
//            //???????????????
//            if ("comSelect".equals(key) || "depSelect".equals(key) || "currDept".equals(key)) {
//                keyList.put(model, organizeList);
//            }
//            //??????
//            if ("userSelect".equals(key)) {
//                keyList.put(model, userList);
//            }
//            //??????
//            if ("posSelect".equals(key) || "currPosition".equals(key)) {
//                keyList.put(model, positionList);
//            }
//            //????????????
//            if ("dicSelect".equals(key)) {
//                keyList.put(model, dictypeList);
//            }
//        }
//    }
//
//    //---------------------------??????--------------------------------------------
//    public void delete(String id, List<FlowTableModel> tableModelList) throws DataException {
//        String mastTableName = tableModelList.stream().filter(t -> "1".equals(t.getTypeId())).findFirst().get().getTable();
//        StringBuffer delMast = new StringBuffer("delete from " + mastTableName + " where F_ID='" + id + "';");
//        List<FlowTableModel> childTable = tableModelList.stream().filter(t -> !"1".equals(t.getTypeId())).collect(Collectors.toList());
//        StringBuffer delTable = new StringBuffer();
//        for (FlowTableModel table : childTable) {
//            String tableName = table.getTable();
//            String mastFile = table.getRelationField();
//            delTable.append("delete from " + tableName + " where " + mastFile + "='" + id + "';");
//        }
//        StringBuffer delete = new StringBuffer();
//        delete.append(delMast);
//        if (delTable.length() > 0) {
//            delete.append(delTable);
//        }
//        opaTableDataInfo(delete.toString());
//    }
//
//    //---------------------------??????-----------------------------------------------
//
//    /**
//     * ????????????
//     *
//     * @param id             ??????
//     * @param list           ??????json
//     * @param tableModelList ?????????
//     * @return
//     */
//    public Map<String, Object> info(String id, List<FieLdsModel> list, List<FlowTableModel> tableModelList) throws DataException {
//        //???????????????jnpfkey
//        Map<String, String> mast = new HashMap<>(16);
//        //???????????????jnpfkey
//        Map<String, Map<String, String>> table = new HashMap<>(16);
//        //???????????????????????????
//        Map<String, String> tableModel = new HashMap<>(16);
//        //????????????
//        tempTableJson(list, mast, table, tableModel, 1);
//        //????????????
//        Map<String, Object> result = new HashMap<>(16);
//        infoTableData(id, tableModelList, mast, tableModel, table, result);
//        return result;
//    }
//
//    /**
//     * ????????????
//     *
//     * @param dataMap ??????
//     * @param list    ??????json
//     * @return
//     */
//    public Map<String, Object> info(Map<String, Object> dataMap, List<FieLdsModel> list) throws DataException {
//        //???????????????jnpfkey
//        Map<String, String> mast = new HashMap<>(16);
//        //???????????????jnpfkey
//        Map<String, Map<String, String>> table = new HashMap<>(16);
//        tempNoTableJson(list, mast, table);
//        //????????????
//        Map<String, Object> result = new HashMap<>(16);
//        infoData(dataMap, mast, table, result);
//        return result;
//    }
//
//    //----------------------------??????--------------------------------------------
//
//    /**
//     * ????????????
//     *
//     * @param formId         ??????
//     * @param dataMap        ??????
//     * @param list           ??????json
//     * @param tableModelList ?????????
//     */
//    public void update(String formId, Map<String, Object> dataMap, List<FieLdsModel> list, List<FlowTableModel> tableModelList) throws DataException {
//        String mastTableName = tableModelList.stream().filter(t -> "1".equals(t.getTypeId())).findFirst().get().getTable();
//        //???????????????jnpfkey
//        Map<String, String> mast = new HashMap<>(16);
//        //???????????????jnpfkey
//        Map<String, Map<String, String>> table = new HashMap<>(16);
//        //???????????????????????????
//        Map<String, String> tableModel = new HashMap<>(16);
//        tempTableJson(list, mast, table, tableModel, 0);
//        int type = 1;
//        //?????????sql??????
//        mastTable(mastTableName, formId, dataMap, mast, type);
//        //?????????sql??????
//        if (tableModel.size() > 0) {
//            childTable(tableModelList, tableModel, table, dataMap, type, formId);
//        }
//        //?????????sql??????
////        String resultSql = resultSql(mastTableName, formId, dataMap, tableModelList, mast, table, tableModel, type);
////        opaTableDataInfo(resultSql);
//    }
//
//    /**
//     * ????????????
//     *
//     * @param dataMap ??????
//     * @param list    ??????json
//     */
//    public void update(Map<String, Object> dataMap, List<FieLdsModel> list) throws DataException {
//        //???????????????jnpfkey
//        Map<String, String> mast = new HashMap<>(16);
//        //???????????????jnpfkey
//        Map<String, Map<String, String>> table = new HashMap<>(16);
//        //????????????
//        tempNoTableJson(list, mast, table);
//        for (String key : dataMap.keySet()) {
//            String jnpfkey = mast.get(key);
//            if (jnpfkey != null) {
//                Object value = dataMap.get(key);
//                Object dataValue = assigments(jnpfkey, value, 1, mast, key, 1);
//                dataMap.put(key, dataValue);
//            } else {
//                Map<String, String> childJnpfKey = table.get(key);
//                List<Map<String, Object>> childList = (List<Map<String, Object>>) dataMap.get(key);
//                for (Map<String, Object> childMap : childList) {
//                    for (String child : childMap.keySet()) {
//                        String childkey = childJnpfKey.get(child);
//                        Object childValue = childMap.get(child);
//                        Object dataValue = assigments(childkey, childValue, 1, childJnpfKey, childkey, 1);
//                        childMap.put(child, dataValue);
//                    }
//                }
//            }
//        }
//    }
//
//    //--------------------------??????------------------------------------------------
//
//    /**
//     * ????????????
//     *
//     * @param formId         ??????
//     * @param dataMap        ??????
//     * @param list           ??????json
//     * @param tableModelList ?????????
//     */
//    public void create(String formId, Map<String, Object> dataMap, List<FieLdsModel> list, List<FlowTableModel> tableModelList) throws DataException {
//        String mastTableName = tableModelList.stream().filter(t -> "1".equals(t.getTypeId())).findFirst().get().getTable();
//        //???????????????jnpfkey
//        Map<String, String> mast = new HashMap<>(16);
//        //???????????????jnpfkey
//        Map<String, Map<String, String>> table = new HashMap<>(16);
//        //???????????????????????????
//        Map<String, String> tableModel = new HashMap<>(16);
//        tempTableJson(list, mast, table, tableModel, 0);
//        int type = 0;
//        //?????????sql??????
//        mastTable(mastTableName, formId, dataMap, mast, type);
//        //?????????sql??????
//        if (tableModel.size() > 0) {
//            childTable(tableModelList, tableModel, table, dataMap, type, formId);
//        }
//        //?????????sql??????
////        String resultSql = resultSql(mastTableName, formId, dataMap, tableModelList, mast, table, tableModel, type);
////        opaTableDataInfo(resultSql);
//    }
//
//    /**
//     * ????????????
//     *
//     * @param dataMap ??????
//     * @param list    ??????json
//     */
//    public void create(Map<String, Object> dataMap, List<FieLdsModel> list) throws DataException {
//        //???????????????jnpfkey
//        Map<String, String> mast = new HashMap<>(16);
//        //???????????????jnpfkey
//        Map<String, Map<String, String>> table = new HashMap<>(16);
//        //????????????
//        tempNoTableJson(list, mast, table);
//        for (String key : dataMap.keySet()) {
//            String jnpfkey = mast.get(key);
//            if (jnpfkey != null) {
//                Object value = dataMap.get(key);
//                Object dataValue = assigments(jnpfkey, value, 0, mast, key, 1);
//                dataMap.put(key, dataValue);
//            } else {
//                Map<String, String> childJnpfKey = table.get(key);
//                List<Map<String, Object>> childList = (List<Map<String, Object>>) dataMap.get(key);
//                for (Map<String, Object> childMap : childList) {
//                    for (String child : childMap.keySet()) {
//                        String childkey = childJnpfKey.get(child);
//                        Object childValue = childMap.get(child);
//                        Object dataValue = assigments(childkey, childValue, 0, childJnpfKey, childkey, 1);
//                        childMap.put(child, dataValue);
//                    }
//                }
//            }
//        }
//    }
//
//    //---------------------------??????????????????-------------------------------------------------------
//
//    /**
//     * ??????????????????
//     *
//     * @param list      ????????????
//     * @param mastTable ??????????????????
//     * @param table     ????????????
//     */
//    private void tempNoTableJson(List<FieLdsModel> list, Map<String, String> mastTable, Map<String, Map<String, String>> table) {
//        for (FieLdsModel fieLdsModel : list) {
//            String jnpfkey = fieLdsModel.getConfig().getJnpfKey();
//            String model = fieLdsModel.getVModel();
//            if (!"table".equals(jnpfkey)) {
//                mastTable.put(model, jnpfkey);
//                if ("billRule".equals(jnpfkey)) {
//                    String mastRule = fieLdsModel.getConfig().getRule();
//                    mastTable.put(model + "_rule", mastRule);
//                }
//            } else {
//                tableTempChildJson(model, fieLdsModel.getConfig().getChildren(), table, false);
//            }
//        }
//    }
//
//    /**
//     * ????????????????????????
//     *
//     * @param list       ????????????
//     * @param mastTable  ??????????????????
//     * @param table      ????????????
//     * @param tableModel ?????????????????????
//     * @param type       0.??????????????? 1.??????
//     */
//    private void tempTableJson(List<FieLdsModel> list, Map<String, String> mastTable, Map<String, Map<String, String>> table, Map<String, String> tableModel, int type) {
//        for (FieLdsModel fieLdsModel : list) {
//            String jnpfkey = fieLdsModel.getConfig().getJnpfKey();
//            String model = fieLdsModel.getVModel();
//            if (!"table".equals(jnpfkey)) {
//                model = model.toUpperCase();
//                mastTable.put(model, jnpfkey);
//                if ("billRule".equals(jnpfkey)) {
//                    String mastRule = fieLdsModel.getConfig().getRule();
//                    mastTable.put(model + "_rule", mastRule);
//                }
//            } else {
//                String tableName = fieLdsModel.getConfig().getTableName();
//                if (type == 0) {
//                    tableModel.put(model, tableName);
//                } else {
//                    tableModel.put(tableName, model);
//                }
//                tableTempChildJson(tableName, fieLdsModel.getConfig().getChildren(), table, true);
//            }
//        }
//    }
//
//    /**
//     * ????????????????????????
//     *
//     * @param tableName ????????????
//     * @param childlist ?????????????????????
//     * @param table     ??????????????????????????????????????????
//     */
//    private void tableTempChildJson(String tableName, List<FieLdsModel> childlist, Map<String, Map<String, String>> table, boolean tableType) {
//        Map<String, String> childField = new HashMap<>(16);
//        for (FieLdsModel fieLdsModel : childlist) {
//            String jnpfkey = fieLdsModel.getConfig().getJnpfKey();
//            String model = fieLdsModel.getVModel();
//            if (tableType) {
//                model = model.toUpperCase();
//            }
//            childField.put(model, jnpfkey);
//            if ("billRule".equals(jnpfkey)) {
//                String mastRule = fieLdsModel.getConfig().getRule();
//                childField.put(model + "_rule", mastRule);
//            }
//        }
//        table.put(tableName, childField);
//    }
//
//    /**
//     * ????????????
//     *
//     * @param id            ??????
//     * @param list          ????????????
//     * @param mast          ???????????????jnpfkey
//     * @param tableModelKey ??????????????????model??????
//     * @param table         ???????????????jnpfkey
//     * @param result        ????????????
//     */
//    private void infoTableData(String id, List<FlowTableModel> list, Map<String, String> mast, Map<String, String> tableModelKey, Map<String, Map<String, String>> table, Map<String, Object> result) throws DataException {
//        String mastTableName = list.stream().filter(t -> "1".equals(t.getTypeId())).findFirst().get().getTable();
//        StringBuffer mastInfo = new StringBuffer("select * from " + mastTableName + " where F_ID='" + id + "'");
//        Map<String, Object> mastData = getMast(mastInfo.toString());
//        List<FlowTableModel> tableListAll = list.stream().filter(t -> !"1".equals(t.getTypeId())).collect(Collectors.toList());
//        //????????????
//        for (String key : mast.keySet()) {
//            String jnpfkey = mast.get(key);
//            Object dataValue = mastData.get(key.toUpperCase());
//            Object value = assigments(jnpfkey, dataValue, 2, mast, key, 0);
//            if (dataValue != null) {
//                result.put(key.toUpperCase(), value);
//            }
//        }
//        //????????????
//        for (FlowTableModel tableModel : tableListAll) {
//            String tableName = tableModel.getTable();
//            StringBuffer tableInfo = new StringBuffer("select * from " + tableName + " where " + tableModel.getTableField() + "='" + id + "'");
//            List<Map<String, Object>> tableList = getTableList(tableInfo.toString());
//            List<Map<String, Object>> tableResult = new ArrayList<>();
//            Map<String, String> tableJnpf = table.get(tableName);
//            for (Map<String, Object> tableInfoModel : tableList) {
//                Map<String, Object> tablValue = new HashMap<>(16);
//                for (String key : tableJnpf.keySet()) {
//                    String childkey = tableJnpf.get(key);
//                    Object childValue = tableInfoModel.get(key.toUpperCase());
//                    Object value = assigments(childkey, childValue, 2, tableJnpf, key, 0);
//                    if (childValue != null) {
//                        tablValue.put(key.toUpperCase(), value);
//                    }
//                }
//                tableResult.add(tablValue);
//            }
//            if (tableModelKey.get(tableName) != null) {
//                result.put(tableModelKey.get(tableName), tableResult);
//            }
//        }
//    }
//
//    /**
//     * ????????????
//     *
//     * @param dataMap ????????????
//     * @param mast    ???????????????jnpfkey
//     * @param table   ???????????????jnpfkey
//     * @param result  ????????????
//     */
//    private void infoData(Map<String, Object> dataMap, Map<String, String> mast, Map<String, Map<String, String>> table, Map<String, Object> result) throws DataException {
//        for (String key : dataMap.keySet()) {
//            String jnpfkey = mast.get(key);
//            if (jnpfkey != null) {
//                Object value = dataMap.get(key);
//                Object dataValue = assigments(jnpfkey, value, 2, mast, key, 1);
//                result.put(key, dataValue);
//            } else {
//                Map<String, String> childJnpfKey = table.get(key);
//                List<Map<String, Object>> childList = (List<Map<String, Object>>) dataMap.get(key);
//                for (Map<String, Object> childMap : childList) {
//                    for (String child : childMap.keySet()) {
//                        String childkey = childJnpfKey.get(child);
//                        Object childValue = childMap.get(child);
//                        Object dataValue = assigments(childkey, childValue, 2, childJnpfKey, child, 1);
//                        childMap.put(child, dataValue);
//                    }
//                }
//                result.put(key, childList);
//            }
//        }
//    }
//    //--------------------------------------???????????????sql??????------------------------------------------
//
//    /**
//     * ?????????sql??????
//     *
//     * @param mastTableName ??????
//     * @param formId        ?????????
//     * @param dataMap       ????????????
//     * @param mast          ?????????jnpfkey
//     * @param type          ????????????????????????
//     * @throws DataException
//     */
//    public void mastTable(String mastTableName, String formId, Map<String, Object> dataMap, Map<String, String> mast, int type) throws DataException {
//        Connection conn = getTableConn();
//        StringBuffer delMast = new StringBuffer("delete from " + mastTableName + " where F_ID=?");
//        StringBuffer mastSql = new StringBuffer("INSERT INTO " + mastTableName);
//        StringBuffer mastfile = new StringBuffer("(F_ID,");
//        StringBuffer mastFileValue = new StringBuffer("(?,");
//        Map<String, Object> pstMastValue = new LinkedHashMap<>();
//        for (String key : dataMap.keySet()) {
//            if (mast.get(key) != null) {
//                String jnpfkey = mast.get(key);
//                Object dataValue = dataMap.get(key);
//                Object mastValue = assigments(jnpfkey, dataValue, type, mast, key, 0);
//                mastfile.append(key + ",");
//                //??????oracle???????????????
//                String file = "?,";
//                if (dataSourceUtil.getDataType().toLowerCase().contains("oracle") && "date".equals(jnpfkey)) {
//                    if (String.valueOf(mastValue).length() < 11) {
//                        mastValue = mastValue + " 00:00:00";
//                    }
//                    file = "to_date(?,'yyyy-mm-dd HH24:mi:ss'),";
//                }
//                pstMastValue.put(key, mastValue);
//                mastFileValue.append(file);
//            }
//        }
//        //??????????????????
//        mastfile = mastfile.deleteCharAt(mastfile.length() - 1).append(")");
//        mastFileValue = mastFileValue.deleteCharAt(mastFileValue.length() - 1).append(")");
//        mastSql.append(mastfile + " VALUES " + mastFileValue);
//        try {
//            conn.setAutoCommit(false);
//            if (type == 1) {
//                PreparedStatement delete = conn.prepareStatement(delMast.toString());
//                delete.setObject(1, formId);
//                delete.addBatch();
//                delete.executeBatch();
//                conn.commit();
//                delete.close();
//                conn.close();
//            }
//            PreparedStatement save = conn.prepareStatement(mastSql.toString());
//            int num = 1;
//            save.setObject(num, formId);
//            num++;
//            for (String pstKey : pstMastValue.keySet()) {
//                //????????????jnpfkey?????????
//                String jnpfkey = mast.get(pstKey);
//                Object mastvalue = pstMastValue.get(pstKey);
//                Object MastValue = assigments(jnpfkey, mastvalue, type, mast, pstKey, 0);
//                save.setObject(num, MastValue);
//                num++;
//            }
//            save.addBatch();
//            save.executeBatch();
//            conn.commit();
//            save.close();
//            conn.close();
//        } catch (SQLException e) {
//            throw new DataException("????????????:" + e.getMessage());
//        }
//    }
//
//    /**
//     * ?????????sql??????
//     *
//     * @param tableModelList ?????????table
//     * @param tableModel     ?????????????????????
//     * @param table          ?????????jnpfkey
//     * @param dataMap        ????????????
//     * @param type           ????????????????????????
//     * @param formId         ?????????
//     */
//    public void childTable(List<FlowTableModel> tableModelList, Map<String, String> tableModel, Map<String, Map<String, String>> table, Map<String, Object> dataMap, int type, String formId) throws DataException {
//        Connection conn = getTableConn();
//        try {
//            for (String tableName : tableModel.keySet()) {
//                conn.setAutoCommit(false);
//                String childTable = tableModel.get(tableName);
//                String mastFile = tableModelList.stream().filter(t -> t.getTable().equals(childTable)).findFirst().get().getTableField();
//                Map<String, String> tableJnpfKey = table.get(childTable);
//                List<Map<String, Object>> tableList = (List<Map<String, Object>>) dataMap.get(tableName);
//                StringBuffer delSql = new StringBuffer("delete from " + childTable + " where " + mastFile + "=?");
//                StringBuffer saveSql = new StringBuffer("INSERT INTO " + childTable + "(" + mastFile + ",F_ID,");
//                StringBuffer tablefile = new StringBuffer();
//                StringBuffer tablefileValue = new StringBuffer("(?,?,");
//                Map<String, String> fileAll = new LinkedHashMap<>();
//                for (String tableFile : tableJnpfKey.keySet()) {
//                    fileAll.put(tableFile, null);
//                    tablefile.append(tableFile + ",");
//                    //??????oracle???????????????
//                    String file = "?,";
//                    if (dataSourceUtil.getDataType().toLowerCase().contains("oracle") && "date".equals(tableJnpfKey.get(tableFile))) {
//                        file = "to_date(?,'yyyy-mm-dd HH24:mi:ss'),";
//                    }
//                    tablefileValue.append(file);
//                }
//                if (type == 1) {
//                    PreparedStatement delete = conn.prepareStatement(delSql.toString());
//                    delete.setObject(1, formId);
//                    delete.addBatch();
//                    delete.executeBatch();
//                    delete.commit();
//                    delete.close();
//                    conn.close();
//                }
//                if (tablefile.length() > 0) {
//                    tablefile = tablefile.deleteCharAt(tablefile.length() - 1).append(")");
//                    tablefileValue = tablefileValue.deleteCharAt(tablefileValue.length() - 1).append(")");
//                    saveSql.append(tablefile + " VALUES " + tablefileValue);
//                    PreparedStatement statement = conn.prepareStatement(saveSql.toString());
//                    for (Map<String, Object> child : tableList) {
//                        int num = 1;
//                        statement.setObject(num, formId);
//                        num++;
//                        statement.setObject(num, RandomUtil.uuId());
//                        num++;
//                        for (String childkey : fileAll.keySet()) {
//                            //????????????jnpfkey?????????
//                            String childjnpfkey = tableJnpfKey.get(childkey);
//                            Object childvalue = child.get(childkey);
//                            Object chilValue = assigments(childjnpfkey, childvalue, type, tableJnpfKey, childkey, 0);
//                            statement.setObject(num, chilValue);
//                            num++;
//                        }
//                        statement.addBatch();
//                    }
//                    statement.executeBatch();
//                }
//                conn.commit();
//            }
//            statement.close();
//            conn.close();
//        } catch (Exception e) {
//            throw new DataException("????????????:" + e.getMessage());
//        }
//    }
//
//    /**
//     * ????????????sql??????
//     *
//     * @param mastTableName ??????
//     * @param formId        ?????????
//     * @param dataMap       ????????????
//     * @param mast          ?????????jnpfkey
//     * @param table         ?????????jnpfkey
//     * @param tableModel    ?????????????????????
//     * @param type          ????????????????????????
//     * @return
//     */
//    private String resultSql(String mastTableName, String formId, Map<String, Object> dataMap, List<FlowTableModel> tableModelList, Map<String, String> mast, Map<String, Map<String, String>> table, Map<String, String> tableModel, int type) throws DataException {
//        //????????????
//        StringBuffer delMast = new StringBuffer("delete from " + mastTableName + " where F_ID='" + formId + "';");
//        StringBuffer deletetable = new StringBuffer();
//        //??????????????????
//        StringBuffer mastSql = new StringBuffer("INSERT INTO " + mastTableName + " ");
//        StringBuffer mastfile = new StringBuffer("(F_ID,");
//        StringBuffer mastvalue = new StringBuffer("('" + formId + "',");
//        //??????????????????
//        StringBuffer tableSql = new StringBuffer();
//        for (String key : dataMap.keySet()) {
//            if (mast.get(key) != null) {
//                String jnpfkey = mast.get(key);
//                Object dataValue = dataMap.get(key);
//                mastfile.append(key + ",");
//                Object mastValue = assigments(jnpfkey, dataValue, type, mast, key, 0);
//                if (mastValue != null) {
//                    mastvalue.append("'" + mastValue + "',");
//                } else {
//                    mastvalue.append(mastValue + ",");
//                }
//                dataMap.put(key, mastValue);
//            } else {
//                //??????table??????
//                String tableName = tableModel.get(key);
//                String mastFile = tableModelList.stream().filter(t -> t.getTable().equals(tableName)).findFirst().get().getTableField();
//                Map<String, String> tableJnpfKey = table.get(tableName);
//                List<Map<String, Object>> tableList = (List<Map<String, Object>>) dataMap.get(key);
//                //????????????
//                StringBuffer tableFiled = new StringBuffer("F_ID,");
//                //????????????
//                StringBuffer tablevalue = new StringBuffer();
//                int num = 0;
//                for (Map<String, Object> child : tableList) {
//                    StringBuffer value = new StringBuffer();
//                    StringBuffer filed = new StringBuffer();
//                    for (String childkey : child.keySet()) {
//                        //????????????jnpfkey?????????
//                        String childjnpfkey = tableJnpfKey.get(childkey);
//                        Object childvalue = child.get(childkey);
//                        Object chilValue = assigments(childjnpfkey, childvalue, type, tableJnpfKey, childkey, 0);
//                        if (chilValue != null) {
//                            value.append("'" + chilValue + "',");
//                        } else {
//                            value.append(chilValue + ",");
//                        }
//                        child.put(childkey, chilValue);
//                        //????????????
//                        filed.append(childkey + ",");
//                    }
//                    //??????????????????
//                    if (num == 0) {
//                        tableFiled.append(filed);
//                        num++;
//                    }
//                    if (value.length() > 0) {
//                        value = value.deleteCharAt(value.length() - 1);
//                        tablevalue.append("('" + RandomUtil.uuId() + "'," + value + ",'" + formId + "'),");
//                    }
//                }
//                if (tablevalue.length() > 0) {
//                    tableFiled.append(mastFile);
//                    tablevalue = tablevalue.deleteCharAt(tablevalue.length() - 1);
//                    tableSql.append("INSERT INTO " + tableName + "(" + tableFiled + ") VALUES " + tablevalue + ";");
//                }
//                deletetable.append("delete from " + tableName + " where " + mastFile + "='" + formId + "';");
//            }
//        }
//        //??????????????????
//        mastfile = mastfile.deleteCharAt(mastfile.length() - 1).append(")");
//        mastvalue = mastvalue.deleteCharAt(mastvalue.length() - 1).append(")");
//        mastSql.append(mastfile + " value " + mastvalue + ";");
//        //????????????
//        StringBuffer resultSql = new StringBuffer();
//        if (type == 0) {
//            resultSql.append(mastSql.toString() + tableSql);
//        } else {
//            resultSql.append(delMast + mastSql.toString() + deletetable + tableSql);
//        }
//        return resultSql.toString();
//    }
//
//    /**
//     * ????????????????????????
//     *
//     * @param jnpfkey   ????????????
//     * @param dataValue ?????????
//     * @param type      0.?????? 1.?????? 2.??????
//     * @param tableType 0.?????? 1.??????
//     */
//    private Object assigments(String jnpfkey, Object dataValue, int type, Map<String, String> mast, String rule, int tableType) throws DataException {
//        UserInfo userInfo = userProvider.get();
//        List<UserAllModel> userAll = userService.getAll();
//        Object value = dataValue;
//        switch (jnpfkey) {
//            case "billRule":
//                if (type == 0) {
//                    String ruleNo = billRuleService.GetBillNumber(mast.get(rule + "_rule"), false);
//                    value = ruleNo;
//                }
//                break;
//            case "createUser":
//                if (type == 0) {
//                    value = userInfo.getUserId();
//                }
//                if (type == 1) {
//                    String userValue = String.valueOf(value);
//                    UserAllModel model = userAll.stream().filter(t -> t.getRealName().equals(userValue)).findFirst().orElse(null);
//                    value = "";
//                    if (model != null) {
//                        value = model.getId();
//                    }
//                }
//                if (type == 2) {
//                    String userValue = String.valueOf(value);
//                    UserAllModel model = userAll.stream().filter(t -> t.getId().equals(userValue)).findFirst().orElse(null);
//                    value = "";
//                    if (model != null) {
//                        value = model.getRealName();
//                    }
//                }
//                break;
//            case "createTime":
//                if (type == 0) {
//                    value = DateUtil.getNow();
//                }
//                if (type == 2) {
//                    value = DateUtil.StringToDate(String.valueOf(value));
//                }
//                break;
//            case "modifyUser":
//                if (type == 0) {
//                    value = "";
//                }
//                if (type == 1) {
//                    value = userInfo.getUserId();
//                }
//                if (type == 2) {
//                    String userValue = String.valueOf(value);
//                    UserAllModel model = userAll.stream().filter(t -> t.getId().equals(userValue)).findFirst().orElse(null);
//                    value = "";
//                    if (model != null) {
//                        value = model.getRealName();
//                    }
//                }
//                break;
//            case "modifyTime":
//                if (type == 1) {
//                    value = DateUtil.getNow();
//                }
//                if (type == 0) {
//                    value = null;
//                }
//                if (type == 2) {
//                    if (value == null) {
//                        value = "";
//                    }
//                }
//                break;
//            case "currDept":
//                if (type == 0) {
//                    value = userInfo.getOrganizeId();
//                }
//                if (type == 1) {
//                    String depName = String.valueOf(value);
//                    UserAllModel model = userAll.stream().filter(t -> t.getOrganize().equals(depName)).findFirst().orElse(null);
//                    value = "";
//                    if (model != null) {
//                        value = model.getOrganize();
//                    }
//                }
//                if (type == 2) {
//                    UserAllModel model = userAll.stream().filter(t -> t.getId().equals(userInfo.getUserId())).findFirst().orElse(null);
//                    value = "";
//                    if (model != null) {
//                        value = model.getOrganize();
//                    }
//                }
//                break;
//            case "currCompany":
//                if (type == 0) {
//                    UserEntity userEntity = userService.getInfo(userInfo.getUserId());
//                    OrganizeEntity info = organizeService.getInfo(userEntity.getId());
//                    value = info.getParentId();
//                }
//                if (type == 1) {
//                    String company = String.valueOf(value);
//                    UserAllModel model = userAll.stream().filter(t -> t.getOrganize().equals(company)).findFirst().orElse(null);
//                    value = "";
//                    if (model != null) {
//                        value = model.getOrganize();
//                    }
//                }
//                if (type == 2) {
//                    UserEntity userEntity = userService.getInfo(userInfo.getUserId());
//                    OrganizeEntity info = organizeService.getInfo(userEntity.getId());
//                    value = info.getParentId();
//                }
//                break;
//            case "currPosition":
//                UserEntity userEntity = userService.getInfo(userInfo.getUserId());
//                PositionEntity positionEntity = positionService.getInfo(userEntity.getPositionId().split(",")[0]);
//                if (type == 0) {
//                    value = positionEntity != null ? positionEntity.getId() : "";
//                }
//                if (type == 2) {
//                    value = positionEntity != null ? positionEntity.getFullName() : "";
//                }
//                break;
//            case "uploadFz":
//                if (tableType == 0) {
//                    if (type == 2) {
//                        value = JSONUtil.getJsonToListMap(String.valueOf(value));
//                    } else {
//                        value = String.valueOf(value);
//                    }
//                }
//                break;
//            case "uploadImg":
//                if (tableType == 0) {
//                    if (type == 2) {
//                        value = JSONUtil.getJsonToListMap(String.valueOf(value));
//                    } else {
//                        value = String.valueOf(value);
//                    }
//                }
//                break;
//            case "checkbox":
//                if (tableType == 0) {
//                    if (type == 2) {
//                        value = JSONUtil.getJsonToList(String.valueOf(value), String.class);
//                    } else {
//                        value = String.valueOf(value);
//                    }
//                }
//                break;
//            case "cascader":
//                if (tableType == 0) {
//                    if (type == 2) {
//                        value = JSONUtil.getJsonToList(String.valueOf(value), String.class);
//                    } else {
//                        value = String.valueOf(value);
//                    }
//                }
//                break;
//            case "dateRange":
//                if (tableType == 0) {
//                    if (type == 2) {
//                        value = JSONUtil.getJsonToList(String.valueOf(value), String.class);
//                    } else {
//                        value = String.valueOf(value);
//                    }
//                }
//                break;
//            case "timeRange":
//                if (tableType == 0) {
//                    if (type == 2) {
//                        value = JSONUtil.getJsonToList(String.valueOf(value), String.class);
//                    } else {
//                        value = String.valueOf(value);
//                    }
//                }
//                break;
//            case "date":
//                if (tableType == 0 && value != null) {
//                    if (type == 0 || type == 1) {
//                        if (value instanceof Long) {
//                            value = DateUtil.daFormat(new Date(Long.valueOf(String.valueOf(value))));
//                        }
//                    }
//                    if (type == 2) {
//                        value = DateUtil.StringToDate(String.valueOf(value)).getTime();
//                    }
//                }
//                break;
//            default:
//                if (value != null && !"".equals(value)) {
//                    value = String.valueOf(value);
//                }else{
//                    value = null;
//                }
//                break;
//        }
//        return value;
//    }
//
//
//    //-------------------------?????????????????????--------------------------------------------
//
//    /**
//     * ?????????????????????
//     *
//     * @param sql sql??????
//     * @return
//     * @throws DataException
//     */
//    private List<Map<String, Object>> getTableList(String sql) throws DataException {
//        Connection conn = getTableConn();
//        ResultSet rs = JdbcUtil.query(conn, sql);
//        List<Map<String, Object>> dataList = JdbcUtil.convertListString(rs);
//        List<Map<String, Object>> resultList = new ArrayList<>();
//        for (Map<String, Object> data : dataList) {
//            Map<String, Object> objectMap = new HashMap<>(16);
//            for (String key : data.keySet()) {
//                objectMap.put(key.toUpperCase(), data.get(key));
//            }
//            resultList.add(objectMap);
//        }
//        return resultList;
//    }
//
//    /**
//     * ??????????????????
//     *
//     * @param sql sql??????
//     * @return
//     * @throws DataException
//     */
//    private Map<String, Object> getMast(String sql) {
//        Connection conn = getTableConn();
//        ResultSet rs = JdbcUtil.query(conn, sql);
//        Map<String, Object> mast = JdbcUtil.convertMapString(rs);
//        Map<String, Object> result = new HashMap<>(16);
//        for (String key : mast.keySet()) {
//            result.put(key.toUpperCase(), mast.get(key));
//        }
//        return result;
//    }
//
//    //??????????????????????????????
//    private Connection getTableConn() {
//        dataSourceUtil = SpringContext.getBean(DataSourceUtil.class);
//        String TenId = "";
//        if (!Boolean.parseBoolean(configValueUtil.getMultiTenancy())) {
//            TenId = dataSourceUtil.getDbName();
//        } else {
//            TenId = userProvider.get().getTenantDbConnectionString();
//        }
//        Connection conn = JdbcUtil.getConn(dataSourceUtil.getUserName(), dataSourceUtil.getPassword(), dataSourceUtil.getUrl().replace("{dbName}", TenId));
//        return conn;
//    }
//
//    /**
//     * ????????????????????????????????????
//     *
//     * @param sql sql??????
//     * @return
//     * @throws DataException
//     */
//    private void opaTableDataInfo(String sql) throws DataException {
//        Connection conn = getTableConn();
//        JdbcUtil.custom(conn, sql);
//    }
//
//    /**
//     * ????????????????????????
//     *
//     * @param sql sql??????
//     */
//    private List<VisualdevModelDataEntity> getTableDataList(String sql) throws DataException {
//        List<VisualdevModelDataEntity> list = new ArrayList<>();
//        Connection conn = getTableConn();
//        ResultSet rs = JdbcUtil.query(conn, sql);
//        List<Map<String, Object>> dataList = JdbcUtil.convertList(rs);
//        for (Map<String, Object> dataMap : dataList) {
//            VisualdevModelDataEntity dataEntity = new VisualdevModelDataEntity();
//            Map<String, Object> objectMap = new HashMap<>(16);
//            for (String key : dataMap.keySet()) {
//                objectMap.put(key.toUpperCase(), dataMap.get(key));
//            }
//            dataEntity.setData(JSONUtil.getObjectToStringDateFormat(objectMap, "yyyy-MM-dd HH:mm:ss"));
//            if (dataMap.containsKey("F_ID")) {
//                dataEntity.setId(dataMap.get("F_ID").toString());
//            } else if (dataMap.containsKey("F_Id")) {
//                dataEntity.setId(dataMap.get("F_Id").toString());
//            }
//            list.add(dataEntity);
//        }
//        return list;
//    }
//
//
//    //---------------------------------------------????????????------------------------------------------------------------------
//
//    /**
//     * ??????app???????????????slot
//     *
//     * @param formData    ???????????????json
//     * @param tableVmodel ?????????????????????
//     * @param type        0.????????? 1.app
//     */
//    private void tempJonsMast(List<FieLdsModel> formData, Map<String, String> tableVmodel, List<String> mastModel, Map<String, List<String>> tableModel, int type) {
//        for (FieLdsModel model : formData) {
//            ConfigModel config = model.getConfig();
//            String mastVmodel = model.getVModel();
//            mastModel.add(mastVmodel);
//            if (type == 1) {
//                if (JnpfKeyConsts.RADIO.equals(config.getJnpfKey()) || JnpfKeyConsts.SELECT.equals(config.getJnpfKey()) || JnpfKeyConsts.CHECKBOX.equals(config.getJnpfKey())) {
//                    if (DataTypeConst.STATIC.equals(config.getDataType())) {
//                        SlotModel slotModel = new SlotModel();
//                        slotModel.setOptions(config.getOptions());
//                        model.setSlot(slotModel);
//                    }
//                }
//            }
//            if ("table".equals(config.getJnpfKey())) {
//                List<FieLdsModel> children = config.getChildren();
//                String tableName = config.getTableName();
//                String vmodel = model.getVModel();
//                tableVmodel.put(tableName, vmodel);
//                tempJonsChild(children, tableModel, tableName, type);
//            }
//        }
//    }
//
//    /**
//     * ????????????
//     *
//     * @param children   ?????????json
//     * @param tableModel ???????????????
//     * @param type       0.????????? 1.app
//     * @param tableName  ?????????
//     */
//    private void tempJonsChild(List<FieLdsModel> children, Map<String, List<String>> tableModel, String tableName, int type) {
//        List<String> tableModelAll = new ArrayList<>();
//        for (FieLdsModel model : children) {
//            ConfigModel config = model.getConfig();
//            String vmodel = model.getVModel();
//            tableModelAll.add(vmodel);
//            if (type == 1) {
//                if (JnpfKeyConsts.RADIO.equals(config.getJnpfKey()) || JnpfKeyConsts.SELECT.equals(config.getJnpfKey()) || JnpfKeyConsts.CHECKBOX.equals(config.getJnpfKey())) {
//                    if (DataTypeConst.STATIC.equals(config.getDataType())) {
//                        SlotModel slotModel = new SlotModel();
//                        slotModel.setOptions(config.getOptions());
//                        model.setSlot(slotModel);
//                    }
//                }
//            }
//        }
//        tableModel.put(tableName, tableModelAll);
//    }
//
//    //?????????????????????
//    public Map<String, Object> tableInfo(String id, List<FieLdsModel> formData, List<FlowTableModel> list, int type) throws DataException {
//        //????????????
//        List<String> mastModel = new ArrayList<>();
//        //????????????
//        Map<String, List<String>> childModel = new HashMap<>(16);
//        //?????????model
//        Map<String, String> tableVmodel = new HashMap<>(16);
//        //????????????
//        tempJonsMast(formData, tableVmodel, mastModel, childModel, type);
//        //??????????????????
//        String mastTableName = list.stream().filter(t -> "1".equals(t.getTypeId())).findFirst().get().getTable();
//        StringBuffer mastInfo = new StringBuffer("select F_ID," + String.join(",", mastModel) + " from " + mastTableName + " where F_ID='" + id + "'");
//        Map<String, Object> data = getMast(mastInfo.toString());
//        //??????????????????
//        List<FlowTableModel> childList = list.stream().filter(t -> !"1".equals(t.getTypeId())).collect(Collectors.toList());
//        for (FlowTableModel tableModel : childList) {
//            String tableName = tableModel.getRelationTable();
//            String mastField = tableModel.getRelationField();
//            List<String> childModelAll = childModel.get(tableName);
//            StringBuffer childInfo = new StringBuffer("select F_ID," + String.join(",", childModelAll) + " from " + tableName + " where " + mastField + "='" + id + "'");
//            List<Map<String, Object>> childData = getTableList(childInfo.toString());
//            String childVmodel = tableVmodel.get(tableName);
//            data.put(childVmodel, childData);
//        }
//        return data;
//    }
//
//    //?????????????????????
//    public Map<String, Object> resultData(String id, List<FieLdsModel> fieLdsList, Map<String, Object> data, int type) {
//        //???????????????
//        Map<String, String> mastKey = new HashMap<>(16);
//        Map<String, List<OptinModels>> mastList = new HashMap<>(16);
//        //???????????????
//        Map<String, Map<String, String>> tableKey = new HashMap<>(16);
//        Map<String, Map<String, List<OptinModels>>> tableList = new HashMap<>(16);
//        //?????????????????????
//        Map<String, Map<String, String>> relation = new HashMap<>(16);
//        jnpfKey(fieLdsList, mastKey, mastList, tableKey, tableList, relation);
//        Map<String, Object> result = data(data, mastKey, mastList, tableKey, tableList, relation);
//        return result;
//    }
//
//    //?????????id????????????
//    private Map<String, Object> data(Map<String, Object> data, Map<String, String> mastKey, Map<String, List<OptinModels>> mastList, Map<String, Map<String, String>> tableKey, Map<String, Map<String, List<OptinModels>>> tableList, Map<String, Map<String, String>> relation) {
//        Map<String, Object> result = new HashMap<>(16);
//        //???????????????
//        List<OrganizeEntity> organizeList = organizeService.getList();
//        //??????
//        List<PositionEntity> positionList = positionService.getList();
//        //??????
//        List<UserEntity> userList = userService.getList();
//        //????????????
//        List<DictionaryDataEntity> dicDataList = dictionaryDataService.getList();
//        for (String key : data.keySet()) {
//            if (mastKey.get(key) != null) {
//                String jnpfKey = mastKey.get(key);
//                Object value = data.get(key);
//                String dataValue = String.valueOf(value);
//                if (JnpfKeyConsts.SELECT.equals(jnpfKey) || JnpfKeyConsts.CHECKBOX.equals(jnpfKey) || JnpfKeyConsts.RADIO.equals(jnpfKey)) {
//                    List<OptinModels> list = mastList.get(key);
//                    if (list.size() > 0) {
//                        if (JnpfKeyConsts.CHECKBOX.equals(jnpfKey)) {
//                            List<String> checkList = (List<String>) value;
//                            List<String> checkData = new ArrayList<>();
//                            if (checkList != null) {
//                                for (String chedk : checkList) {
//                                    OptinModels optinModels = list.stream().filter(t -> t.getId().equals(chedk)).findFirst().orElse(null);
//                                    if (optinModels != null) {
//                                        checkData.add(optinModels.getFullName());
//                                    } else {
//                                        checkData.add(chedk);
//                                    }
//                                }
//                            }
//                            value = String.join(",", checkData);
//                        } else {
//                            OptinModels optinModels = list.stream().filter(t -> t.getId().equals(dataValue)).findFirst().orElse(null);
//                            if (optinModels != null) {
//                                value = optinModels.getFullName();
//                            }
//                        }
//                    }
//                }
//                //???????????????
//                if ("comSelect".equals(key) || "depSelect".equals(key) || "currDept".equals(key) || "currCompany".equals(key)) {
//                    OrganizeEntity organizeEntity = organizeList.stream().filter(t -> t.getId().equals(dataValue)).findFirst().orElse(null);
//                    if (organizeEntity != null) {
//                        value = organizeEntity.getFullName();
//                    }
//                }
//                //??????
//                if ("userSelect".equals(key)) {
//                    UserEntity userEntity = userList.stream().filter(t -> t.getId().equals(dataValue)).findFirst().orElse(null);
//                    if (userEntity != null) {
//                        value = userEntity.getRealName();
//                    }
//                }
//                //??????
//                if ("posSelect".equals(key) || "currPosition".equals(key)) {
//                    PositionEntity positionEntity = positionList.stream().filter(t -> t.getId().equals(dataValue)).findFirst().orElse(null);
//                    if (positionEntity != null) {
//                        value = positionEntity.getFullName();
//                    }
//                }
//                if ("relationForm".equals(key)) {
//                    Map<String, String> relationMap = relation.get(key);
//                    String modelId = relationMap.get("modelId");
//                    String relaField = relationMap.get("relaField");
//                    VisualdevEntity visualdevEntity = visualdevService.getInfo(modelId);
//                    //formTempJson
//                    FormDataModel formData = JSONUtil.getJsonToBean(visualdevEntity.getFormData(), FormDataModel.class);
//                    List<FieLdsModel> list = JSONUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
//                    //?????????
//                    List<FlowTableModel> tableModelList = JSONUtil.getJsonToList(visualdevEntity.getTables(), FlowTableModel.class);
//                    FieLdsModel fieLdsModel = list.stream().filter(t -> t.getVModel().equals(relaField)).findFirst().get();
//                    Object objectData = "";
//                    if (tableModelList.size() > 0) {
//                        String table = tableModelList.stream().filter(t -> "1".equals(t.getTypeId())).findFirst().get().getTable();
//                        String sql = "select * from " + table + " where F_ID" + dataValue;
//                        Map<String, Object> relationData = getMast(sql);
//                        objectData = relationData.get(relaField);
//                    } else {
//                        VisualdevModelDataEntity info = visualdevModelDataService.getInfo(dataValue);
//                        Map<String, Object> relationData = getMast(info.getData());
//                        objectData = relationData.get(relaField);
//                    }
//                    value = relation(objectData, fieLdsModel, organizeList, positionList, userList);
//                }
//                result.put(key, value);
//            } else {
//                List<Map<String, Object>> childListAll = (List<Map<String, Object>>) data.get(key);
//                List<Map<String, Object>> childResultList = new ArrayList<>();
//                for (Map<String, Object> childList : childListAll) {
//                    Map<String, String> childKeyAll = tableKey.get(key);
//                    Map<String, Object> resultData = new HashMap<>(16);
//                    for (String childKey : childList.keySet()) {
//                        String jnpfKey = childKeyAll.get(childKey);
//                        Object value = childList.get(childKey);
//                        String dataValue = String.valueOf(value);
//                        if (JnpfKeyConsts.SELECT.equals(jnpfKey) || JnpfKeyConsts.CHECKBOX.equals(jnpfKey) || JnpfKeyConsts.RADIO.equals(jnpfKey)) {
//                            List<OptinModels> list = tableList.get(key).get(childKey);
//                            if (list.size() > 0) {
//                                OptinModels optinModels = list.stream().filter(t -> t.getId().equals(dataValue)).findFirst().orElse(null);
//                                if (optinModels != null) {
//                                    value = optinModels.getFullName();
//                                }
//                            } else {
//                                DictionaryDataEntity dictionaryDataEntity = dicDataList.stream().filter(t -> t.getId().equals(dataValue)).findFirst().orElse(null);
//                                if (dictionaryDataEntity != null) {
//                                    value = dictionaryDataEntity.getFullName();
//                                }
//                            }
//                        }
//                        //???????????????
//                        if ("comSelect".equals(key) || "depSelect".equals(key) || "currDept".equals(key) || "currCompany".equals(key)) {
//                            OrganizeEntity organizeEntity = organizeList.stream().filter(t -> t.getId().equals(dataValue)).findFirst().orElse(null);
//                            if (organizeEntity != null) {
//                                value = organizeEntity.getFullName();
//                            }
//                        }
//                        //??????
//                        if ("userSelect".equals(key)) {
//                            UserEntity userEntity = userList.stream().filter(t -> t.getId().equals(dataValue)).findFirst().orElse(null);
//                            if (userEntity != null) {
//                                value = userEntity.getRealName();
//                            }
//                        }
//                        //??????
//                        if ("posSelect".equals(key) || "currPosition".equals(key)) {
//                            PositionEntity positionEntity = positionList.stream().filter(t -> t.getId().equals(dataValue)).findFirst().orElse(null);
//                            if (positionEntity != null) {
//                                value = positionEntity.getFullName();
//                            }
//                        }
//                        resultData.put(childKey, value);
//                    }
//                    childResultList.add(resultData);
//                }
//                result.put(key, childResultList);
//            }
//        }
//        return result;
//    }
//
//    //????????????jnpfkey
//    private void jnpfKey(List<FieLdsModel> fieLdsList, Map<String, String> mastKey, Map<String, List<OptinModels>> mastList, Map<String, Map<String, String>> tableKey, Map<String, Map<String, List<OptinModels>>> tableList, Map<String, Map<String, String>> relation) {
//        for (FieLdsModel field : fieLdsList) {
//            String jnpfkey = field.getConfig().getJnpfKey();
//            String model = field.getVModel();
//            if (!"table".equals(jnpfkey)) {
//                if (!"JNPFText".equals(jnpfkey) && !"divider".equals(jnpfkey)) {
//                    mastKey.put(model, jnpfkey);
//                    List<OptinModels> optinModelsList = new ArrayList<>();
//                    keyList(jnpfkey, field, optinModelsList);
//                    mastList.put(model, optinModelsList);
//                }
//                if ("relationForm".equals(jnpfkey)) {
//                    String modelId = field.getModelId();
//                    String relaField = field.getRelationField();
//                    Map<String, String> form = new HashMap<>(16);
//                    form.put("modelId", modelId);
//                    form.put("relaField", relaField);
//                    relation.put(model, form);
//                }
//            } else {
//                childJnpfKey(field, tableKey, tableList);
//            }
//        }
//    }
//
//    //??????????????????
//    private Object relation(Object value, FieLdsModel fieLdsModel, List<OrganizeEntity> organizeList, List<PositionEntity> positionList, List<UserEntity> userList) {
//        String key = fieLdsModel.getConfig().getJnpfKey();
//        String dataValue = String.valueOf(value);
//        //???????????????
//        if ("comSelect".equals(key) || "depSelect".equals(key) || "currDept".equals(key) || "currCompany".equals(key)) {
//            OrganizeEntity organizeEntity = organizeList.stream().filter(t -> t.getId().equals(dataValue)).findFirst().orElse(null);
//            if (organizeEntity != null) {
//                value = organizeEntity.getFullName();
//            }
//        }
//        //??????
//        if ("userSelect".equals(key)) {
//            UserEntity userEntity = userList.stream().filter(t -> t.getId().equals(dataValue)).findFirst().orElse(null);
//            if (userEntity != null) {
//                value = userEntity.getRealName();
//            }
//        }
//        //??????
//        if ("posSelect".equals(key) || "currPosition".equals(key)) {
//            PositionEntity positionEntity = positionList.stream().filter(t -> t.getId().equals(dataValue)).findFirst().orElse(null);
//            if (positionEntity != null) {
//                value = positionEntity.getFullName();
//            }
//        }
//        if (JnpfKeyConsts.SELECT.equals(key) || JnpfKeyConsts.CHECKBOX.equals(key) || JnpfKeyConsts.RADIO.equals(key)) {
//            List<OptinModels> list = new ArrayList<>();
//            keyList(key, fieLdsModel, list);
//            if (list.size() > 0) {
//                if (JnpfKeyConsts.CHECKBOX.equals(key)) {
//                    List<String> checkList = (List<String>) value;
//                    List<String> checkData = new ArrayList<>();
//                    if (checkList != null) {
//                        for (String chedk : checkList) {
//                            OptinModels optinModels = list.stream().filter(t -> t.getId().equals(chedk)).findFirst().orElse(null);
//                            if (optinModels != null) {
//                                checkData.add(optinModels.getFullName());
//                            } else {
//                                checkData.add(chedk);
//                            }
//                        }
//                    }
//                    value = String.join(",", checkData);
//                } else {
//                    OptinModels optinModels = list.stream().filter(t -> t.getId().equals(dataValue)).findFirst().orElse(null);
//                    if (optinModels != null) {
//                        value = optinModels.getFullName();
//                    }
//                }
//            }
//        }
//        return value;
//    }
//
//    //????????????jnpfkey
//    private void childJnpfKey(FieLdsModel fieLdsModel, Map<String, Map<String, String>> tableKey, Map<String, Map<String, List<OptinModels>>> tableList) {
//        String vmodel = fieLdsModel.getVModel();
//        List<FieLdsModel> fieLdsList = fieLdsModel.getConfig().getChildren();
//        Map<String, String> tablJnpfKey = new HashMap<>(16);
//        Map<String, List<OptinModels>> tableOptionList = new HashMap<>(16);
//        for (FieLdsModel field : fieLdsList) {
//            String jnpfkey = field.getConfig().getJnpfKey();
//            String model = field.getVModel();
//            tablJnpfKey.put(model, jnpfkey);
//            List<OptinModels> optinModelsList = new ArrayList<>();
//            keyList(jnpfkey, field, optinModelsList);
//            tableOptionList.put(model, optinModelsList);
//        }
//        tableKey.put(vmodel, tablJnpfKey);
//        tableList.put(vmodel, tableOptionList);
//    }
//
//    //?????????????????????????????????
//    private void keyList(String jnpfKey, FieLdsModel fieLdsModel, List<OptinModels> optionslList) {
//        ConfigModel config = fieLdsModel.getConfig();
//        SlotModel slotModel = fieLdsModel.getSlot();
//        if (JnpfKeyConsts.SELECT.equals(jnpfKey) || JnpfKeyConsts.CHECKBOX.equals(jnpfKey) || JnpfKeyConsts.RADIO.equals(jnpfKey)) {
//            String type = config.getDataType();
//            ConfigPropsModel configPropsModel = JSONUtil.getJsonToBean(config.getProps(), ConfigPropsModel.class);
//            String fullName = configPropsModel.getLabel();
//            String value = configPropsModel.getValue();
//            String optionsAll = slotModel.getOptions();
//            if (DataTypeConst.STATIC.equals(type)) {
//                List<Map<String, Object>> staticList = JSONUtil.getJsonToListMap(optionsAll);
//                for (Map<String, Object> options : staticList) {
//                    OptinModels optionsModel = new OptinModels();
//                    optionsModel.setId(String.valueOf(options.get(value)));
//                    optionsModel.setFullName(String.valueOf(options.get(fullName)));
//                    optionslList.add(optionsModel);
//                }
//            } else if (DataTypeConst.DYNAMIC.equals(type)) {
//                String dynId = config.getPropsUrl();
//                //??????????????????
//                Map<String, Object> dynamicMap = new HashMap<>(16);
//                if (dynamicMap.get("data") != null) {
//                    List<Map<String, Object>> dataList = JSONUtil.getJsonToListMap(dynamicMap.get("data").toString());
//                    for (Map<String, Object> options : dataList) {
//                        OptinModels optionsModel = new OptinModels();
//                        optionsModel.setId(String.valueOf(options.get(value)));
//                        optionsModel.setFullName(String.valueOf(options.get(fullName)));
//                        optionslList.add(optionsModel);
//                    }
//                }
//            }
//        }
//    }
//
//
//}
