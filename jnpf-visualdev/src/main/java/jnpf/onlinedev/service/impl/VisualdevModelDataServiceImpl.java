package jnpf.onlinedev.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.ActionResult;
import jnpf.database.model.DbLinkEntity;
import jnpf.base.service.DblinkService;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.enums.FlowStatusEnum;
import jnpf.engine.service.FlowTaskService;
import jnpf.exception.WorkFlowException;
import jnpf.model.visiual.*;
import jnpf.onlinedev.model.*;
import jnpf.onlinedev.util.*;
import jnpf.util.JsonUtilEx;
import jnpf.base.util.VisualUtils;
import jnpf.base.entity.VisualdevEntity;
import jnpf.onlinedev.entity.VisualdevModelDataEntity;
import jnpf.database.exception.DataException;
import jnpf.base.model.template6.ColumnListField;
import jnpf.model.visiual.fields.FieLdsModel;
import jnpf.onlinedev.mapper.VisualdevModelDataMapper;
import jnpf.onlinedev.service.VisualdevModelDataService;
import jnpf.util.*;
import jnpf.util.JsonUtil;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021/3/16
 */
@Service
public class VisualdevModelDataServiceImpl extends ServiceImpl<VisualdevModelDataMapper, VisualdevModelDataEntity> implements VisualdevModelDataService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private DblinkService dblinkService;
    @Autowired
    private FlowTaskService flowTaskService;
    @Override
    public List<VisualdevModelDataEntity> getList(String modelId) {
        QueryWrapper<VisualdevModelDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(VisualdevModelDataEntity::getVisualDevId, modelId);
        return this.list(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getListResult(VisualdevEntity visualdevEntity, PaginationModel paginationModel) throws IOException, ParseException, DataException, SQLException {
        List<Map<String, Object>> realList = new ArrayList<>();
        List<VisualdevModelDataEntity> list = null;
        DbLinkEntity linkEntity = dblinkService.getInfo(visualdevEntity.getDbLinkId());
        if (!StringUtil.isEmpty(visualdevEntity.getTables()) && !OnlineDevData.TABLE_CONST.equals(visualdevEntity.getTables())) {
            ColumnDataModel columnData = JsonUtil.getJsonToBean(visualdevEntity.getColumnData(), ColumnDataModel.class);
            List<ColumnListField> modelList = JsonUtil.getJsonToList(columnData.getColumnList(), ColumnListField.class);
            List<Map<String, Object>> mapList = JsonUtil.getJsonToListMap(visualdevEntity.getTables());
            String mainTable = VisualUtils.getMainTable(mapList).get("table").toString();

            list = KeyDataUtil.getHasTableList(list, mainTable, modelList, columnData,linkEntity);
        } else {
            list = this.getList(visualdevEntity.getId());
        }

        //关键字过滤
        if (list.size() > 0) {
            if (StringUtil.isNotEmpty(paginationModel.getSidx())) {
                //排序处理
                    list.sort((o1, o2) -> {
                        Map<String, Object> i1=JsonUtil.stringToMap(o1.getData());
                        Map<String, Object> i2=JsonUtil.stringToMap(o2.getData());
                        String s1=String.valueOf(i1.get(paginationModel.getSidx()));
                        String s2=String.valueOf(i2.get(paginationModel.getSidx()));
                        if ("desc".equalsIgnoreCase(paginationModel.getSort())) {
                            return s2.compareTo(s1);
                        } else  {
                            return s1.compareTo(s2);
                        }
                    });
            }
            //将查询的关键字json转成map
            Map<String, Object> keyJsonMap = JsonUtil.stringToMap(paginationModel.getJson());
            //查询条件中的空数组去除
            if (StringUtil.isNotEmpty(paginationModel.getJson())){
                for (Map.Entry entry:keyJsonMap.entrySet()){
                    if (entry.getValue().toString().equals("[]")){
                        keyJsonMap.remove(entry.getKey());
                    }
                }
            }

            //数据处理
            realList = ListDataHandler.swapListData(keyJsonMap, visualdevEntity, list, realList,linkEntity);
            //分页处理
            paginationModel.setTotal(realList.size());
            realList = PageUtil.getListPage((int) paginationModel.getCurrentPage(), (int) paginationModel.getPageSize(), realList);


            //添加流程状态
            if(visualdevEntity.getWebType()!=null){
                if (visualdevEntity.getWebType().equals("3")){
                    for (Map<String,Object> map :realList) {
                        FlowTaskEntity taskEntity = flowTaskService.getInfoSubmit(map.get("id").toString());
                        if (taskEntity==null){
                            map.put("flowState","");
                        }else {
                            map.put("flowState",taskEntity.getStatus());
                        }
                    }
                }
            }
            //判断数据是否分组
            ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(visualdevEntity.getColumnData(), ColumnDataModel.class);
            if (OnlineDevData.TYPE_THREE_COLUMNDATA.equals(columnDataModel.getType())) {
                return KeyDataUtil.changeGroupDataList(columnDataModel, realList);
            }

            return realList;

        }
        return realList;
    }

    @Override
    public List<Map<String, Object>> getListResultAll(VisualdevEntity visualdevEntity) throws IOException, ParseException, DataException, SQLException {
        List<Map<String, Object>> realList = new ArrayList<>();
        List<VisualdevModelDataEntity> list = null;
        DbLinkEntity linkEntity = dblinkService.getInfo(visualdevEntity.getDbLinkId());
        if (!StringUtil.isEmpty(visualdevEntity.getTables()) && !OnlineDevData.TABLE_CONST.equals(visualdevEntity.getTables())) {
            ColumnDataModel columnData = JsonUtil.getJsonToBean(visualdevEntity.getColumnData(), ColumnDataModel.class);
            List<ColumnListField> modelList = JsonUtil.getJsonToList(columnData.getColumnList(), ColumnListField.class);
            List<Map<String, Object>> mapList = JsonUtil.getJsonToListMap(visualdevEntity.getTables());
            String mainTable = VisualUtils.getMainTable(mapList).get("table").toString();

            list = KeyDataUtil.getHasTableList(list, mainTable, modelList, columnData,linkEntity);
        } else {
            list = this.getList(visualdevEntity.getId());
        }
        //关键字过滤
        if (list.size() > 0) {
            //将查询的关键字json转成map
            Map<String, Object> keyJsonMap = new HashMap<>();
            //数据处理
            realList = ListDataHandler.swapListData(keyJsonMap, visualdevEntity, list, realList,linkEntity);
        }
        return realList;
    }

    @Override
    public List<Map<String, Object>> exportData(String[] keys, PaginationModelExport paginationModelExport, VisualdevEntity visualdevEntity) throws IOException, ParseException, SQLException, DataException {
        List<VisualdevModelDataEntity> list = null;
        List<Map<String, Object>> realList = new ArrayList<>();
        DbLinkEntity linkEntity = dblinkService.getInfo(visualdevEntity.getDbLinkId());
        if (!StringUtil.isEmpty(visualdevEntity.getTables()) && !OnlineDevData.TABLE_CONST.equals(visualdevEntity.getTables())) {
            ColumnDataModel columnData = JsonUtil.getJsonToBean(visualdevEntity.getColumnData(), ColumnDataModel.class);
            List<ColumnListField> modelList = JsonUtil.getJsonToList(columnData.getColumnList(), ColumnListField.class);
            List<Map<String, Object>> mapList = JsonUtil.getJsonToListMap(visualdevEntity.getTables());
            String mainTable = VisualUtils.getMainTable(mapList).get("table").toString();
            list = KeyDataUtil.getHasTableList(list, mainTable, modelList, columnData,linkEntity);
        } else {
            list = this.getList(visualdevEntity.getId());
        }
        //删除多余字段
        list = VisualUtils.deleteKey(list, keys);
        //关键字过滤
        if (list.size() > 0) {
            //将查询的关键字json转成map
            Map<String, Object> keyJsonMap = JsonUtil.stringToMap(paginationModelExport.getJson());
            //数据处理
            realList = ListDataHandler.swapListData(keyJsonMap, visualdevEntity, list, realList,linkEntity);
            //判断分页
            if (!"1".equals(paginationModelExport.getDataType())) {
                paginationModelExport.setTotal(realList.size());
                realList = PageUtil.getListPage((int) paginationModelExport.getCurrentPage(), (int) paginationModelExport.getPageSize(), realList);
            }
            return realList;
        }
        return realList;
    }


    @Override
    public VisualdevModelDataEntity getInfo(String id) {
        QueryWrapper<VisualdevModelDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(VisualdevModelDataEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<VisualdevModelDataEntity> getModel(String id) {
        QueryWrapper<VisualdevModelDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(VisualdevModelDataEntity::getVisualDevId, id);
        return this.list(queryWrapper);
    }

    @Override
    public VisualdevModelDataInfoVO infoDataChange(String id, VisualdevEntity visualdevEntity) throws IOException, ParseException, DataException, SQLException {
        Map<String, Object> formData = JsonUtil.stringToMap(visualdevEntity.getFormData());
        List<FieLdsModel> modelList = JsonUtil.getJsonToList(formData.get("fields").toString(), FieLdsModel.class);
        //去除模板多级控件
        modelList = VisualUtils.deleteMore(modelList);

        QueryWrapper<VisualdevModelDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(VisualdevModelDataEntity::getId, id);
        VisualdevModelDataEntity visualdevModelDataEntity = this.getOne(queryWrapper);
        if (visualdevModelDataEntity != null) {
            Map<String, Object> newDataMap = JsonUtil.stringToMap(visualdevModelDataEntity.getData());
            Map<String, Object> newDataMapOperate = new HashMap<>(16);
            newDataMapOperate.putAll(newDataMap);
            Map<String, Object> newMainDataMap = new HashMap<>(16);
            for (Map.Entry<String, Object> entryMap : newDataMapOperate.entrySet()) {
                String key = entryMap.getKey();
                if (!key.contains("table")) {
                    newMainDataMap.put(key, entryMap.getValue());
                    newDataMap.remove(key);
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
                            Map<String, Object> keyAndList = KeyDataUtil.getKeyData(formDatas, keyJsonMap, list, visualdevEntity.getId(),1);

                            //替换静态数据模板选项值
                            keyAndList = ListDataHandler.getRealData(formDatas, keyJsonMap, JsonUtil.getJsonToList(keyAndList.get(DataTypeConst.LIST), VisualdevModelDataEntity.class));
                            //系统自动生成字段转换
                            list = VisualUtils.stringToList(formDatas, JsonUtil.getJsonToList(keyAndList.get(DataTypeConst.LIST), VisualdevModelDataEntity.class));
                            //字符串转数组
                            list = AutoFeildsUtil.autoFeildsList(formDatas, list);
                            list = TableInfoUtil.NoTableInfo(modelList,list,visualdevModelDataEntity.getData());
                            entryMap.setValue(list);
                        } else if (entryMap.getKey().equals(fieLdsModel.getVModel())) {
                            formDatas = JsonUtil.getJsonToList(fieLdsModel.getConfig().getChildren(), FieLdsModel.class);
                            List<Map<String, Object>> childMapList = (List<Map<String, Object>>) entryMap.getValue();
                            for (Map<String, Object> objectMap : childMapList) {
                                VisualdevModelDataEntity entity = new VisualdevModelDataEntity();
                                entity.setData(JsonUtilEx.getObjectToString(objectMap));
                                list.add(entity);
                            }
                            //将关键字查询传输的id转换成名称
                            Map<String, Object> keyAndList = KeyDataUtil.getKeyData(formDatas, keyJsonMap, list, visualdevEntity.getId(),0);

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
                List<VisualdevModelDataEntity> childList = (List<VisualdevModelDataEntity>) entryMap.getValue();
                List<Map<String,Object>> array = new ArrayList<>();
                for (VisualdevModelDataEntity childEntity : childList) {
                    array.add(JsonUtil.stringToMap(childEntity.getData()));
                }
                entryMap.setValue(array);
            }
            realDataMap.putAll(newDataMap);
            String tmp1=JsonUtilEx.getObjectToString(realDataMap);
            VisualdevModelDataInfoVO vo = new VisualdevModelDataInfoVO();
            vo.setData(tmp1);
            vo.setId(visualdevModelDataEntity.getId());
            return vo;
        }
        return null;
    }

    /**
     * 获取单条信息时，主表将时间字符串转时间戳，子表将字符串类型的数组时间戳转成Json数组时间戳
     *
     * @param id
     * @param visualdevEntity
     * @return
     * @throws DataException
     * @throws ParseException
     */
    @Override
    public VisualdevModelDataInfoVO tableInfo(String id, VisualdevEntity visualdevEntity) throws DataException, SQLException, ParseException, IOException {
        Map<String, Object> formData = JsonUtil.stringToMap(visualdevEntity.getFormData());
        List<FieLdsModel> modelList = JsonUtil.getJsonToList(formData.get("fields").toString(), FieLdsModel.class);

        //去除模板多级控件
        modelList = VisualUtils.deleteMore(modelList);

        List<Map<String, Object>> tableMapList = JsonUtil.getJsonToListMap(visualdevEntity.getTables());
        String mainTable = VisualUtils.getMainTable(tableMapList).get("table").toString();
        DbLinkEntity linkEntity = dblinkService.getInfo(visualdevEntity.getDbLinkId());
        @Cleanup Connection conn = VisualUtils.getDataConn(linkEntity);
        //获取主键
        String pKeyName = VisualUtils.getpKey(conn, mainTable);

        StringBuilder mainfeild = new StringBuilder();
        String main = "select * from" + " " + tableMapList.get(0).get("table") + " where " + pKeyName + "='" + id + "'";
        List<Map<String, Object>> mainMap = VisualUtils.getTableDataInfo(main,linkEntity);
        return TableInfoUtil.getTableInfo(pKeyName,mainfeild,modelList,mainMap,tableMapList,linkEntity);
    }


    /**
     * 获取单条信息时，主表将时间字符串转时间戳，子表将字符串类型的数组时间戳转成Json数组时间戳(真实数据转换)
     *
     * @param id
     * @param visualdevEntity
     * @return
     * @throws DataException
     * @throws ParseException
     */
    @Override
    public VisualdevModelDataInfoVO tableInfoDataChange(String id, VisualdevEntity visualdevEntity) throws DataException, IOException, SQLException, ParseException {
        Map<String, Object> formData = JsonUtil.stringToMap(visualdevEntity.getFormData());

        List<FieLdsModel> modelList = JsonUtil.getJsonToList(formData.get("fields").toString(), FieLdsModel.class);
        //去除模板多级控件
        modelList = VisualUtils.deleteMore(modelList);
        List<Map<String, Object>> tableMapList = JsonUtil.getJsonToListMap(visualdevEntity.getTables());
        String mainTable = tableMapList.get(0).get("table").toString();
        DbLinkEntity linkEntity = dblinkService.getInfo(visualdevEntity.getDbLinkId());
        return TableInfoUtil.getTableInfoDataChange(id,modelList,tableMapList,mainTable,linkEntity,"");
    }

    @Override
    public ActionResult visualCreate(VisualdevEntity visualdevEntity, Map<String, Object> dataMap,VisualdevModelDataCrForm visualdevModelDataCrForm,String mainId) throws WorkFlowException {
        if (!StringUtil.isEmpty(visualdevEntity.getTables()) && !OnlineDevData.TABLE_CONST.equals(visualdevEntity.getTables())) {
            if (visualdevEntity.getWebType()!=null && visualdevEntity.getWebType().equals("3")){
                //流程提交
                if (visualdevModelDataCrForm.getStatus().equals(FlowStatusEnum.submit.getMessage())){
                    VisualUtils.submitFlowTask(visualdevEntity,mainId,dataMap,userProvider.get());
                    return ActionResult.success("提交成功，请耐心等待");
                }else {
                    return ActionResult.success("保存成功");
                }
            }else {
                return ActionResult.success("新建成功");
            }
        }else {
            VisualdevModelDataEntity entity = new VisualdevModelDataEntity();
            entity.setData(JsonUtilEx.getObjectToString(dataMap));
            entity.setVisualDevId(visualdevEntity.getId());
            entity.setId(mainId);
            entity.setSortcode(RandomUtil.parses());
            entity.setCreatortime(new Date());
            entity.setCreatoruserid(userProvider.get().getUserId());
            entity.setEnabledmark(1);
            this.save(entity);
            if (visualdevEntity.getWebType()!=null && visualdevEntity.getWebType().equals("3")){
                //流程提交
                if (visualdevModelDataCrForm.getStatus().equals(FlowStatusEnum.submit.getMessage())){
                    VisualUtils.submitFlowTask(visualdevEntity,mainId,dataMap,userProvider.get());
                    return ActionResult.success("提交成功，请耐心等待");
                }else {
                    return ActionResult.success("保存成功");
                }
            }else {
                return ActionResult.success("新建成功");
            }
        }
    }

    @Override
    public ActionResult visualUpdate(String id, VisualdevEntity visualdevEntity, Map<String,Object> map,VisualdevModelDataUpForm visualdevModelDataUpForm) throws WorkFlowException {
        if (StringUtil.isEmpty(visualdevEntity.getTables()) || OnlineDevData.TABLE_CONST.equals(visualdevEntity.getTables())) {
            VisualdevModelDataEntity entity = new VisualdevModelDataEntity();
            entity.setData(JsonUtilEx.getObjectToString(map));
            entity.setVisualDevId(visualdevEntity.getId());
            entity.setId(id);
            entity.setLastModifyTime(new Date());
            entity.setLastmodifyuserid(userProvider.get().getUserId());
            this.updateById(entity);
        }
        if (visualdevEntity.getWebType()!=null && visualdevEntity.getWebType().equals("3")){
            //流程提交
            if (visualdevModelDataUpForm.getStatus().equals(FlowStatusEnum.submit.getMessage())){
                VisualUtils.submitFlowTask(visualdevEntity,id,map,userProvider.get());
                return ActionResult.success("提交成功，请耐心等待");
            }else {
                return ActionResult.success("保存成功");
            }
        }else {
            return ActionResult.success("更新成功");
        }
    }


    /**
     * 主表添加时将时间转换成字符串时间，子表不做改变
     *
     * @param visualdevEntity
     * @param visualdevModelDataCrForm
     * @throws DataException
     */
    @SneakyThrows
    @Override
    public ActionResult create(VisualdevEntity visualdevEntity, VisualdevModelDataCrForm visualdevModelDataCrForm) throws DataException, SQLException {
        Map<String, Object> allDataMap = JsonUtil.stringToMap(visualdevModelDataCrForm.getData());
        List<FieLdsModel> fieLdsModelList = JsonUtil.getJsonToList(JsonUtil.stringToMap(visualdevEntity.getFormData()).get("fields"), FieLdsModel.class);
        //去除模板多级控件
        fieLdsModelList = VisualUtils.deleteMore(fieLdsModelList);

        //去除无意义控件
        fieLdsModelList = VisualUtils.deleteVmodel(fieLdsModelList);

        //生成系统自动生成字段
        allDataMap = AutoFeildsUtil.createFeilds(fieLdsModelList, allDataMap);

        Map<String, Object> newMainDataMap = JsonUtil.stringToMap(visualdevModelDataCrForm.getData());
        //生成id
        String mainId = RandomUtil.uuId();
        if (!StringUtil.isEmpty(visualdevEntity.getTables()) && !OnlineDevData.TABLE_CONST.equals(visualdevEntity.getTables())) {
            //没有选择数据源,默认配置文件的数据源
                DbLinkEntity linkEntity = dblinkService.getInfo(visualdevEntity.getDbLinkId());
                OnlineDevDbUtil.insertTable(visualdevEntity, fieLdsModelList, allDataMap, newMainDataMap,linkEntity,mainId);
                if (visualdevEntity.getWebType()!=null && visualdevEntity.getWebType().equals("3")){
                    //流程提交
                    if (visualdevModelDataCrForm.getStatus().equals(FlowStatusEnum.submit.getMessage())){
                        VisualUtils.submitFlowTask(visualdevEntity,mainId,newMainDataMap,userProvider.get());
                        return ActionResult.success("提交成功，请耐心等待");
                    }else {
                        return ActionResult.success("保存成功");
                    }
                }else {
                    return ActionResult.success("新建成功");
                }
        } else {
            VisualdevModelDataEntity entity = new VisualdevModelDataEntity();
            entity.setData(JsonUtilEx.getObjectToString(allDataMap));
            entity.setVisualDevId(visualdevEntity.getId());
            entity.setId(mainId);
            entity.setSortcode(RandomUtil.parses());
            entity.setCreatortime(new Date());
            entity.setCreatoruserid(userProvider.get().getUserId());
            entity.setEnabledmark(1);
            this.save(entity);
            if (visualdevEntity.getWebType()!=null && visualdevEntity.getWebType().equals("3")){
                //流程提交
                if (visualdevModelDataCrForm.getStatus().equals(FlowStatusEnum.submit.getMessage())){
                    VisualUtils.submitFlowTask(visualdevEntity,mainId,newMainDataMap,userProvider.get());
                    return ActionResult.success("提交成功，请耐心等待");
                }else {
                    return ActionResult.success("保存成功");
                }
            }else {
                return ActionResult.success("新建成功");
            }
        }

    }
    @SneakyThrows
    @Override
    public ActionResult update(String id, VisualdevEntity visualdevEntity, VisualdevModelDataUpForm visualdevModelDataUpForm) throws DataException, SQLException {

        Map<String, Object> allDataMap = JsonUtil.stringToMap(visualdevModelDataUpForm.getData());
        List<FieLdsModel> fieLdsModelList = JsonUtil.getJsonToList(JsonUtil.stringToMap(visualdevEntity.getFormData()).get("fields"), FieLdsModel.class);
        //去除模板多级控件
        fieLdsModelList = VisualUtils.deleteMore(fieLdsModelList);
        //去除无意义控件
        fieLdsModelList = VisualUtils.deleteVmodel(fieLdsModelList);

        //生成系统自动生成字段
        allDataMap = AutoFeildsUtil.updateFeilds(fieLdsModelList, allDataMap);

        Map<String, Object> newMainDataMap = JsonUtil.stringToMap(visualdevModelDataUpForm.getData());

        boolean flag;
        if (!StringUtil.isEmpty(visualdevEntity.getTables()) && !OnlineDevData.TABLE_CONST.equals(visualdevEntity.getTables())) {
            DbLinkEntity linkEntity = dblinkService.getInfo(visualdevEntity.getDbLinkId());
            Map<String, Object> dataMap = OnlineDevDbUtil.swapTimeType(allDataMap, fieLdsModelList);
            flag= OnlineDevDbUtil.updateTables(id,dataMap,fieLdsModelList,linkEntity,visualdevEntity);
            if (!flag){
                return ActionResult.fail("更新失败，数据不存在");
            }
        }
        VisualdevModelDataEntity entity = new VisualdevModelDataEntity();
        entity.setData(JsonUtilEx.getObjectToString(allDataMap));
        entity.setVisualDevId(visualdevEntity.getId());
        entity.setId(id);
        entity.setLastModifyTime(new Date());
        entity.setLastmodifyuserid(userProvider.get().getUserId());
        this.updateById(entity);
            if (visualdevEntity.getWebType()!=null && visualdevEntity.getWebType().equals("3")){
                //流程提交
                if (visualdevModelDataUpForm.getStatus().equals(FlowStatusEnum.submit.getMessage())){
                    VisualUtils.submitFlowTask(visualdevEntity,id,newMainDataMap,userProvider.get());
                    return ActionResult.success("提交成功，请耐心等待");
                }else {
                    return ActionResult.success("保存成功");
                }
            }else {
                return ActionResult.success("更新成功");
            }
        }



    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(VisualdevModelDataEntity entity) {
        if (entity != null) {
            this.removeById(entity.getId());
        }
    }

    @Override
    public boolean tableDelete(String id, VisualdevEntity visualdevEntity) throws DataException, SQLException {
        DbLinkEntity linkEntity = dblinkService.getInfo(visualdevEntity.getDbLinkId());
        return OnlineDevDbUtil.deleteTable(id, visualdevEntity,linkEntity);
    }

    @Override
    public ActionResult tableDeleteMore(List<String> ids, VisualdevEntity visualdevEntity) throws DataException, SQLException {
        DbLinkEntity linkEntity = dblinkService.getInfo(visualdevEntity.getDbLinkId());
        return OnlineDevDbUtil.deleteTables(ids, visualdevEntity,linkEntity);
    }


    @Override
    public void importData(List<VisualdevModelDataEntity> list) {

    }


}
