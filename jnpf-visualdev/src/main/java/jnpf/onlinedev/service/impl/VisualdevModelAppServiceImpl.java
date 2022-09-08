package jnpf.onlinedev.service.impl;

import jnpf.base.ActionResult;
import jnpf.model.visiual.FormDataModel;
import jnpf.onlinedev.model.*;
import jnpf.onlinedev.util.AutoFeildsUtil;
import jnpf.util.JsonUtilEx;
import jnpf.base.service.VisualdevService;
import jnpf.base.entity.VisualdevEntity;
import jnpf.onlinedev.entity.VisualdevModelDataEntity;
import jnpf.database.exception.DataException;
import jnpf.model.visiual.ColumnDataModel;
import jnpf.model.visiual.TableModel;
import jnpf.model.visiual.fields.FieLdsModel;
import jnpf.model.visiual.fields.config.ConfigModel;
import jnpf.model.FormAllModel;
import jnpf.model.FormColumnModel;
import jnpf.model.FormEnum;
import jnpf.onlinedev.service.VisualdevModelAppService;
import jnpf.onlinedev.service.VisualdevModelDataService;
import jnpf.base.util.*;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 * @date 2021/3/16
 */
@Service
public class VisualdevModelAppServiceImpl implements VisualdevModelAppService {

    @Autowired
    private VisualdevService visualdevService;
    @Autowired
    private VisualdevModelDataService visualdevModelDataService;

    @Override
    public List<Map<String, Object>> resultList(String modelId, PaginationModel paginationModel) throws DataException, ParseException, SQLException, IOException {
        VisualdevEntity entity = visualdevService.getInfo(modelId);
        //赋值type
        ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(entity.getColumnData(), ColumnDataModel.class);
        columnDataModel.setType(1);
        entity.setColumnData(JsonUtilEx.getObjectToString(columnDataModel));
        common(entity);
        List<Map<String, Object>> realList = visualdevModelDataService.getListResult(entity, paginationModel);
        //排序
        if (StringUtil.isNotEmpty(paginationModel.getSidx()) && realList.size() > 0) {
            Object value = realList.get(0).get(paginationModel.getSidx());
            if (value != null) {
                if ("desc".equals(paginationModel.getSort())) {
                    realList.sort(Comparator.comparing((Map<String, Object> h) -> ((String) h.get(paginationModel.getSidx()))).reversed());
                } else {
                    realList.sort(Comparator.comparing((Map<String, Object> h) -> ((String) h.get(paginationModel.getSidx()))));
                }
            }
        }
        return realList;
    }

    @Override
    public void create(VisualdevEntity entity, String data) throws DataException, SQLException {
        VisualdevModelDataCrForm form = new VisualdevModelDataCrForm();
        form.setData(data);
        common(entity);
        visualdevModelDataService.create(entity, form);
    }

    @Override
    public ActionResult update(String id, VisualdevEntity entity, String data) throws DataException, SQLException {
        VisualdevModelDataUpForm form = new VisualdevModelDataUpForm();
        form.setData(data);
        common(entity);
        return visualdevModelDataService.update(id, entity, form);
    }

    @Override
    public boolean delete(String id, VisualdevEntity entity) throws DataException, SQLException {
        boolean flag = false;
        List<TableModel> tableModelList = JsonUtil.getJsonToList(entity.getTables(), TableModel.class);
        if (tableModelList.size() > 0) {
            flag = visualdevModelDataService.tableDelete(id, entity);
        } else {
            VisualdevModelDataEntity dataEntity = visualdevModelDataService.getInfo(id);
            if (dataEntity != null) {
                visualdevModelDataService.delete(dataEntity);
                flag = true;
            }
        }
        return flag;
    }

    @Override
    public Map<String, Object> info(String id, VisualdevEntity entity) throws DataException, ParseException, SQLException, IOException {
        List<TableModel> tableModelList = JsonUtil.getJsonToList(entity.getTables(), TableModel.class);
        VisualdevModelDataInfoVO vo = new VisualdevModelDataInfoVO();
        if (tableModelList.size() > 0) {
            common(entity);
            vo = visualdevModelDataService.tableInfo(id, entity);
        } else {
            VisualdevModelDataEntity dataEntity = visualdevModelDataService.getInfo(id);
            List<FieLdsModel> list = info(entity);
            if (dataEntity != null) {
                String data = AutoFeildsUtil.autoFeilds(list, dataEntity.getData());
                vo.setData(data);
                vo.setId(id);
            }
        }
        Map<String, Object> result = JsonUtil.entityToMap(vo);
        return result;
    }

    /**
     * 信息去多余控件
     * @param entity
     * @return
     */
    private List<FieLdsModel> info(VisualdevEntity entity) {
        //修改app属性没有默认值
        FormDataModel formDataModel = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
        List<FieLdsModel> formModel = JsonUtil.getJsonToList(formDataModel.getFields(), FieLdsModel.class);
        List<FieLdsModel> fieldsAll = fieldsAll(formModel);
        return fieldsAll;
    }

    /**
     * 查询、新增和修改属性
     * @param entity
     */
    private void common(VisualdevEntity entity) {
        //修改app属性没有默认值
        FormDataModel formDataModel = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
        List<FieLdsModel> formModel = JsonUtil.getJsonToList(formDataModel.getFields(), FieLdsModel.class);
        List<FieLdsModel> fieldsAll = fieldsAll(formModel);
        Map<String, Object> map = new HashMap<>(16);
        map.put("fields", fieldsAll);
        entity.setFormData(JsonUtil.getObjectToString(map));
    }

    /**
     * app的默认值
     * @param formModel
     * @return
     */
    private List<FieLdsModel> fieldsAll(List<FieLdsModel> formModel) {
        //修改app属性没有默认值
        List<FormAllModel> formAllModel = new ArrayList<>();
        FormCloumnUtil.recursionForm(formModel, formAllModel);
        //赋值主表的日期类型
        List<FormAllModel> formAll = formAllModel.stream().filter(t -> FormEnum.mast.getMessage().equals(t.getJnpfKey()) || FormEnum.table.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        List<FieLdsModel> fieldsAll = new ArrayList<>();
        for (FormAllModel model : formAll) {
            if (FormEnum.mast.getMessage().equals(model.getJnpfKey())) {
                FieLdsModel fieLdsModel = model.getFormColumnModel().getFieLdsModel();
                model(fieLdsModel);
                fieldsAll.add(fieLdsModel);
            } else {
                String tabVmodel = model.getChildList().getTableModel();
                FieLdsModel child = new FieLdsModel();
                child.setVModel(tabVmodel);
                String tableName = model.getChildList().getTableName();
                ConfigModel configModel = new ConfigModel();
                configModel.setTableName(tableName);
                configModel.setJnpfKey(FormEnum.table.getMessage());
                List<FieLdsModel> childAll = new ArrayList<>();
                List<FormColumnModel> childList = model.getChildList().getChildList();
                for (FormColumnModel column : childList) {
                    FieLdsModel fieLdsModel = column.getFieLdsModel();
                    model(fieLdsModel);
                    childAll.add(fieLdsModel);
                }
                configModel.setChildren(childAll);
                child.setConfig(configModel);
                fieldsAll.add(child);
            }
        }
        return fieldsAll;
    }

    /**
     * app日期赋默认属性
     * @param fieLdsModel
     */
    private void model(FieLdsModel fieLdsModel) {
        String jnpfkey = fieLdsModel.getConfig().getJnpfKey();
        if (StringUtil.isNotEmpty(fieLdsModel.getVModel())) {
            if ("date".equals(jnpfkey) || "dateRange".equals(jnpfkey)) {
                fieLdsModel.setFormat("yyyy-MM-dd");
                fieLdsModel.setValueformat("timestamp");
            } else if ("timeRange".equals(jnpfkey) || "time".equals(jnpfkey)) {
                fieLdsModel.setFormat("HH:mm:ss");
                fieLdsModel.setValueformat("HH:mm:ss");
            }
        }
    }

}
