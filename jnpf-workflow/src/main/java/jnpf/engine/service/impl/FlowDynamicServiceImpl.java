package jnpf.engine.service.impl;

import jnpf.database.model.DbLinkEntity;
import jnpf.base.service.DblinkService;
import jnpf.engine.util.ModelUtil;
import jnpf.model.visiual.FormDataModel;
import jnpf.util.*;
import jnpf.base.UserInfo;
import jnpf.exception.WorkFlowException;
import jnpf.model.visiual.fields.FieLdsModel;
import jnpf.model.FormAllModel;
import jnpf.base.util.*;
import jnpf.engine.entity.FlowEngineEntity;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.model.flowtask.FlowTableModel;
import jnpf.engine.model.flowtask.FlowTaskInfoVO;
import jnpf.engine.service.FlowDynamicService;
import jnpf.engine.service.FlowEngineService;
import jnpf.engine.service.FlowTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 在线开发工作流
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:19
 */
@Slf4j
@Service
public class FlowDynamicServiceImpl implements FlowDynamicService {

    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private FlowEngineService flowEngineService;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private FlowDataUtil flowDataUtil;
    @Autowired
    private DblinkService dblinkService;

    @Override
    public FlowTaskInfoVO info(FlowTaskEntity entity) throws WorkFlowException {
        FlowEngineEntity flowentity = flowEngineService.getInfo(entity.getFlowId());
        List<FlowTableModel> tableModelList = JsonUtil.getJsonToList(flowentity.getTables(), FlowTableModel.class);
        FlowTaskInfoVO vo = JsonUtil.getJsonToBean(entity, FlowTaskInfoVO.class);
        //formTempJson
        FormDataModel formData = JsonUtil.getJsonToBean(entity.getFlowForm(), FormDataModel.class);
        List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
        DbLinkEntity link = null;
        if (StringUtil.isNotEmpty(flowentity.getDbLinkId())) {
            link = dblinkService.getInfo(flowentity.getDbLinkId());
        }
        Map<String, Object> result = flowDataUtil.info(list, entity, tableModelList, false, link);
        vo.setData(JsonUtilEx.getObjectToString(result));
        return vo;
    }

    @Override
    public void save(String id, String flowId, String data) throws WorkFlowException {
        FlowEngineEntity entity = flowEngineService.getInfo(flowId);
        UserInfo info = userProvider.get();
        String billNo = "单据规则不存在";
        String title = info.getUserName() + "的" + entity.getFullName();
        String formId = RandomUtil.uuId();
        //formTempJson
        FormDataModel formData = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
        List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
        List<FormAllModel> formAllModel = new ArrayList<>();
        FormCloumnUtil.recursionForm(list, formAllModel);
        //主表的单据数据
        Map<String, String> billData = new HashMap<>(16);
        boolean type = id != null;
        if (type) {
            formId = id;
        }
        //tableJson
        List<FlowTableModel> tableModelList = JsonUtil.getJsonToList(entity.getTables(), FlowTableModel.class);
        //表单值
        Map<String, Object> dataMap = JsonUtil.stringToMap(data);
        Map<String, Object> result = new HashMap<>(16);
        DbLinkEntity link = null;
        if (StringUtil.isNotEmpty(entity.getDbLinkId())) {
            link = dblinkService.getInfo(entity.getDbLinkId());
        }
        if (type) {
            result = flowDataUtil.update(dataMap, list, tableModelList, formId, link);
        } else {
            result = flowDataUtil.create(dataMap, list, tableModelList, formId, billData, link);
        }
        //流程信息
        ModelUtil.save(id, flowId, formId, title, 1, billNo, result);
    }

    @Override
    public void submit(String id, String flowId, String data, String freeUserId) throws WorkFlowException {
        FlowEngineEntity entity = flowEngineService.getInfo(flowId);
        UserInfo info = userProvider.get();
        String billNo = "单据规则不存在";
        String title = info.getUserName() + "的" + entity.getFullName();
        String formId = RandomUtil.uuId();
        //formTempJson
        FormDataModel formData = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
        List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
        List<FormAllModel> formAllModel = new ArrayList<>();
        FormCloumnUtil.recursionForm(list, formAllModel);
        //主表的单据数据
        Map<String, String> billData = new HashMap<>(16);
        boolean type = id != null;
        if (type) {
            formId = id;
        }
        //tableJson
        List<FlowTableModel> tableModelList = JsonUtil.getJsonToList(entity.getTables(), FlowTableModel.class);
        //表单值
        Map<String, Object> dataMap = JsonUtil.stringToMap(data);
        Map<String, Object> result = new HashMap<>(16);
        DbLinkEntity link = null;
        if (StringUtil.isNotEmpty(entity.getDbLinkId())) {
            link = dblinkService.getInfo(entity.getDbLinkId());
        }
        if (type) {
            result = flowDataUtil.update(dataMap, list, tableModelList, formId, link);
        } else {
            result = flowDataUtil.create(dataMap, list, tableModelList, formId, billData, link);
        }
        //流程信息
        ModelUtil.submit(id, flowId, formId, title, 1, billNo, result, freeUserId);
    }

    @Override
    public Map<String, Object> getData(String flowId, String id) throws WorkFlowException {
        FlowTaskEntity entity = flowTaskService.getInfo(id);
        FlowEngineEntity flowentity = flowEngineService.getInfo(flowId);
        List<FlowTableModel> tableModelList = JsonUtil.getJsonToList(flowentity.getTables(), FlowTableModel.class);
        //formTempJson
        FormDataModel formData = JsonUtil.getJsonToBean(entity.getFlowForm(), FormDataModel.class);
        List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
        DbLinkEntity link = null;
        if (StringUtil.isNotEmpty(flowentity.getDbLinkId())) {
            link = dblinkService.getInfo(flowentity.getDbLinkId());
        }
        Map<String, Object> resultData = flowDataUtil.info(list, entity, tableModelList, true, link);
        return resultData;
    }

}
