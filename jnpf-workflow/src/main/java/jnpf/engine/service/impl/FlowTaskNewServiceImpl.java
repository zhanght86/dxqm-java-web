package jnpf.engine.service.impl;

import com.alibaba.fastjson.JSONObject;
import jnpf.base.UserInfo;
import jnpf.database.model.DbLinkEntity;
import jnpf.base.service.BillRuleService;
import jnpf.base.service.DblinkService;
import jnpf.base.util.FlowDataUtil;
import jnpf.base.util.FlowJsonUtil;
import jnpf.base.util.FlowNature;
import jnpf.base.util.FormCloumnUtil;
import jnpf.engine.entity.*;
import jnpf.engine.enums.*;
import jnpf.engine.model.FlowHandleModel;
import jnpf.engine.model.flowbefore.*;
import jnpf.engine.model.flowengine.FlowModel;
import jnpf.engine.model.flowengine.FlowMsgModel;
import jnpf.engine.model.flowengine.FlowOperatordModel;
import jnpf.engine.model.flowengine.shuntjson.childnode.ChildNode;
import jnpf.engine.model.flowengine.shuntjson.childnode.FlowAssignModel;
import jnpf.engine.model.flowengine.shuntjson.childnode.Properties;
import jnpf.engine.model.flowengine.shuntjson.childnode.TimeOutConfig;
import jnpf.engine.model.flowengine.shuntjson.nodejson.ChildNodeList;
import jnpf.engine.model.flowengine.shuntjson.nodejson.ConditionList;
import jnpf.engine.model.flowengine.shuntjson.nodejson.Custom;
import jnpf.engine.model.flowengine.shuntjson.nodejson.DateProperties;
import jnpf.engine.model.flowtask.FlowTableModel;
import jnpf.engine.service.*;
import jnpf.exception.WorkFlowException;
import jnpf.message.enums.MessageTypeEnum;
import jnpf.message.model.message.SentMessageForm;
import jnpf.message.util.SentMessageUtil;
import jnpf.model.FormAllModel;
import jnpf.model.FormEnum;
import jnpf.model.visiual.FormDataModel;
import jnpf.model.visiual.JnpfKeyConsts;
import jnpf.model.visiual.fields.FieLdsModel;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.UserRelationService;
import jnpf.permission.service.UserService;
import jnpf.util.*;
import jnpf.util.context.SpringContext;
import jnpf.util.wxutil.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ????????????
 *
 * @author JNPF???????????????
 * @version V3.2.0
 * @copyright ??????????????????????????????
 * @date 2021???4???27??? ??????9:18
 */
@Service
public class FlowTaskNewServiceImpl implements FlowTaskNewService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private UserService userService;
    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private UserRelationService userRelationService;
    @Autowired
    private FlowDelegateService flowDelegateService;
    @Autowired
    private FlowTaskNodeService flowTaskNodeService;
    @Autowired
    private FlowTaskOperatorService flowTaskOperatorService;
    @Autowired
    private FlowTaskOperatorRecordService flowTaskOperatorRecordService;
    @Autowired
    private FlowTaskCirculateService flowTaskCirculateService;
    @Autowired
    private FlowEngineService flowEngineService;
    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private FlowDataUtil flowDataUtil;
    @Autowired
    private SentMessageUtil sentMessageUtil;
    @Autowired
    private DblinkService dblinkService;
    @Autowired
    private BillRuleService billRuleService;

    /**
     * ??????id
     **/
    private String taskNodeId = "taskNodeId";
    /**
     * ??????
     **/
    private String handleStatus = "handleStatus";
    /**
     * ??????id
     **/
    private String taskId = "taskId";
    /**
     * ????????????????????????
     **/
    private String user = "admin";

    @Override
    public FlowTaskEntity saveIsAdmin(FlowModel flowModel) throws WorkFlowException {
        FlowTaskEntity entity = this.save(flowModel);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FlowTaskEntity save(FlowModel flowModel) throws WorkFlowException {
        String flowId = flowModel.getFlowId();
        UserInfo userInfo = userProvider.get();
        flowModel.setStatus(StringUtil.isNotEmpty(flowModel.getStatus()) ? flowModel.getStatus() : FlowStatusEnum.save.getMessage());
        String userId = StringUtil.isNotEmpty(flowModel.getUserId()) ? flowModel.getUserId() : userInfo.getUserId();
        //????????????
        FlowEngineEntity engine = flowEngineService.getInfo(flowId);
        boolean flag = flowModel.getId() == null;
        //????????????
        FlowTaskEntity taskEntity = new FlowTaskEntity();
        if (!flag) {
            flowModel.setProcessId(flowModel.getId());
            taskEntity = flowTaskService.getInfo(flowModel.getProcessId());
            if (!FlowNature.ParentId.equals(taskEntity.getParentId())) {
                flowModel.setParentId(taskEntity.getParentId());
                flowModel.setFlowTitle(taskEntity.getFullName());
            }
        }
        this.task(taskEntity, engine, flowModel, userId);
        //??????????????????
        if (flag) {
            flowTaskService.save(taskEntity);
        } else {
            flowTaskService.updateById(taskEntity);
        }
        return taskEntity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(FlowModel flowModel) throws WorkFlowException {
        UserInfo userInfo = userProvider.get();
        flowModel.setStatus(FlowStatusEnum.submit.getMessage());
        //????????????
        FlowTaskEntity flowTask = saveIsAdmin(flowModel);
        try {
            //????????????
            List<FlowTaskNodeEntity> taskNodeList = new LinkedList<>();
            //????????????
            List<FlowTaskOperatorEntity> operatorList = new ArrayList<>();
            //????????????Json
            String formDataJson = flowTask.getFlowTemplateJson();
            ChildNode childNodeAll = JsonUtil.getJsonToBean(formDataJson, ChildNode.class);
            //??????????????????
            List<ChildNodeList> nodeListAll = new ArrayList<>();
            List<ConditionList> conditionListAll = new ArrayList<>();
            //???????????????????????????????????????
            FlowJsonUtil.getTemplateAll(childNodeAll, nodeListAll, conditionListAll);
            //????????????
            this.createNodeList(flowTask, nodeListAll, conditionListAll, taskNodeList);
            //????????????
            this.nodeListAll(taskNodeList);
            //?????????????????????
            FlowTaskNodeEntity startNode = taskNodeList.stream().filter(t -> FlowNature.NodeStart.equals(t.getNodeType())).findFirst().get();
            List<String> nodeList = Arrays.asList(startNode.getNodeNext().split(","));
            //?????????????????????
            List<ChildNodeList> nextOperatorList = nodeListAll.stream().filter(t -> nodeList.contains(t.getCustom().getNodeId())).collect(Collectors.toList());
            Map<String, List<String>> nodeIdAll = this.nextOperator(operatorList, nextOperatorList, flowTask, flowModel);
            //?????????
            flowTaskOperatorService.create(operatorList);
            //?????????????????????id
            for (String nodeId : nodeIdAll.keySet()) {
                FlowTaskNodeEntity entity = taskNodeList.stream().filter(t -> t.getId().equals(nodeId)).findFirst().orElse(null);
                if (entity != null) {
                    ChildNodeList childNodeList = JsonUtil.getJsonToBean(entity.getNodePropertyJson(), ChildNodeList.class);
                    childNodeList.getCustom().setTaskId(nodeIdAll.get(nodeId));
                    entity.setNodePropertyJson(JsonUtil.getObjectToString(childNodeList));
                    flowTaskNodeService.update(entity);
                }
            }
            //????????????
            ChildNodeList start = JsonUtil.getJsonToBean(startNode.getNodePropertyJson(), ChildNodeList.class);
            FlowTaskOperatorEntity operator = new FlowTaskOperatorEntity();
            operator.setTaskId(flowTask.getId());
            operator.setNodeCode(start.getCustom().getNodeId());
            FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
            //??????????????????
            FlowOperatordModel flowOperatordModel = new FlowOperatordModel();
            flowOperatordModel.setStatus(FlowRecordEnum.submit.getCode());
            flowOperatordModel.setFlowModel(flowModel);
            flowOperatordModel.setUserId(userInfo.getUserId());
            flowOperatordModel.setOperator(operator);
            this.operatorRecord(operatorRecord, flowOperatordModel);
            flowTaskOperatorRecordService.create(operatorRecord);
            //?????????
            FlowTaskOperatorEntity startOperator = new FlowTaskOperatorEntity();
            startOperator.setTaskId(start.getTaskId());
            startOperator.setTaskNodeId(start.getTaskNodeId());
            DateProperties timer = start.getTimer();
            List<Date> dateList = new ArrayList<>();
            if (timer.getTime()) {
                Date date = new Date();
                date = DateUtil.dateAddDays(date, timer.getDay());
                date = DateUtil.dateAddHours(date, timer.getHour());
                date = DateUtil.dateAddMinutes(date, timer.getMinute());
                date = DateUtil.dateAddSeconds(date, timer.getSecond());
                dateList.add(date);
            }
            startOperator.setDescription(JsonUtil.getObjectToString(dateList));
            List<FlowTaskOperatorEntity> operatorAll = this.timer(startOperator, taskNodeList, operatorList);
            for (FlowTaskOperatorEntity operatorTime : operatorAll) {
                List<Date> dateAll = JsonUtil.getJsonToList(operatorTime.getDescription(), Date.class);
                if (dateAll.size() > 0) {
                    Date max = Collections.max(dateAll);
                    operatorTime.setCreatorTime(max);
                }
                flowTaskOperatorService.update(operatorTime);
            }
            //????????????
            this.event(FlowRecordEnum.submit.getCode(), start, operatorRecord);
            //??????????????????
            boolean isEnd = this.getNextStepId(nextOperatorList, taskNodeList, flowTask);
            if (FlowNature.NodeEnd.equals(startNode.getNodeNext())) {
                this.endround(flowTask, nodeListAll.get(0));
            }
            flowTaskService.updateById(flowTask);
            //????????????
            List<FlowTaskCirculateEntity> circulateList = new ArrayList<>();
            FlowMsgModel flowMsgModel = new FlowMsgModel();
            flowMsgModel.setCirculateList(circulateList);
            flowMsgModel.setMsgTitel(isEnd ? "??????????????????" : null);
            flowMsgModel.setNodeList(taskNodeList);
            flowMsgModel.setOperatorList(operatorList);
            flowMsgModel.setTaskEntity(flowTask);
            this.message(flowMsgModel);
        } catch (WorkFlowException e) {
            throw new WorkFlowException(e.getMessage());
        }
    }

    @Override
    public void audit(String id, FlowModel flowModel) throws WorkFlowException {
        FlowTaskOperatorEntity operator = flowTaskOperatorService.getInfo(id);
        if (operator != null) {
            FlowTaskEntity flowTaskEntity = flowTaskService.getInfo(operator.getTaskId());
            if (operator.getCompletion() == 0) {
                this.audit(flowTaskEntity, operator, flowModel);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(FlowTaskEntity flowTask, FlowTaskOperatorEntity operator, FlowModel flowModel) throws WorkFlowException {
        if (operator.getCompletion() != 0) {
            throw new WorkFlowException("???????????????");
        }
        try {
            UserInfo userInfo = userProvider.get();
            String userId = StringUtil.isNotEmpty(flowModel.getUserId()) ? flowModel.getUserId() : userInfo.getUserId();
            //??????????????????
            List<FlowTaskNodeEntity> flowTaskNodeAll = flowTaskNodeService.getList(flowTask.getId());
            List<FlowTaskNodeEntity> taskNodeList = flowTaskNodeAll.stream().filter(t -> FlowNodeEnum.Process.getCode().equals(t.getState())).collect(Collectors.toList());
            //????????????
            FlowTaskNodeEntity taskNode = taskNodeList.stream().filter(m -> m.getId().equals(operator.getTaskNodeId())).findFirst().get();
            //??????????????????
            ChildNodeList nodeModel = JsonUtil.getJsonToBean(taskNode.getNodePropertyJson(), ChildNodeList.class);
            //????????????
            FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
            //??????????????????
            FlowOperatordModel flowOperatordModel = new FlowOperatordModel();
            flowOperatordModel.setStatus(FlowRecordEnum.audit.getCode());
            flowOperatordModel.setFlowModel(flowModel);
            flowOperatordModel.setUserId(userId);
            flowOperatordModel.setOperator(operator);
            this.operatorRecord(operatorRecord, flowOperatordModel);
            flowTaskOperatorRecordService.create(operatorRecord);
            //?????????????????????????????????
            this.handleIdStatus(1, nodeModel, operator, userInfo, taskNodeList);
            //????????????
            this.event(FlowRecordEnum.audit.getCode(), nodeModel, operatorRecord);
            //??????????????????????????????
            if(StringUtil.isNotEmpty(operator.getId())) {
                flowTaskOperatorService.update(operator);
            }
            //??????????????????
            List<FlowTaskOperatorEntity> operatorList = new ArrayList<>();
            //?????????????????????
            List<FlowTaskNodeEntity> nextNode = taskNodeList.stream().filter(t -> taskNode.getNodeNext().contains(t.getNodeCode())).collect(Collectors.toList());
            List<ChildNodeList> nextOperatorList = new ArrayList<>();
            List<FlowTaskNodeEntity> result = this.isNextAll(taskNodeList, nextNode, taskNode, flowModel);
            for (FlowTaskNodeEntity entity : result) {
                ChildNodeList node = JsonUtil.getJsonToBean(entity.getNodePropertyJson(), ChildNodeList.class);
                nextOperatorList.add(node);
            }
            //????????????
            FlowEngineEntity engine = flowEngineService.getInfo(flowTask.getFlowId());
            flowModel.setProcessId(flowTask.getId());
            flowModel.setId(flowTask.getId());
            Map<String, Object> dataAll = JsonUtil.stringToMap(flowTask.getFlowFormContentJson());
            if (FlowNature.CUSTOM.equals(engine.getFormType())) {
                Map<String, Object> formDataAll = flowModel.getFormData();
                flowModel.setFormData(dataAll);
                if (formDataAll.get("data") != null) {
                    Map<String, Object> data = JsonUtil.stringToMap(String.valueOf(formDataAll.get("data")));
                    flowModel.setFormData(data);
                }
            }
            Map<String, Object> data = this.createData(engine, flowTask, flowModel);
            //??????????????????
            boolean isEnd = this.getNextStepId(nextOperatorList, taskNodeList, flowTask);
            flowTask.setFlowFormContentJson(JsonUtil.getObjectToString(data));
            flowTaskService.updateById(flowTask);
            //????????????
            Map<String, List<String>> nodeIdAll = this.nextOperator(operatorList, nextOperatorList, flowTask, flowModel);
            flowTaskOperatorService.create(operatorList);
            //?????????????????????id
            for (String nodeId : nodeIdAll.keySet()) {
                FlowTaskNodeEntity entity = flowTaskNodeService.getInfo(nodeId);
                if (entity != null) {
                    ChildNodeList childNodeList = JsonUtil.getJsonToBean(entity.getNodePropertyJson(), ChildNodeList.class);
                    childNodeList.getCustom().setTaskId(nodeIdAll.get(nodeId));
                    entity.setNodePropertyJson(JsonUtil.getObjectToString(childNodeList));
                    flowTaskNodeService.update(entity);
                }
            }
            //?????????
            List<FlowTaskOperatorEntity> operatorAll = this.timer(operator, taskNodeList, operatorList);
            for (FlowTaskOperatorEntity operatorTime : operatorAll) {
                List<Date> dateAll = JsonUtil.getJsonToList(operatorTime.getDescription(), Date.class);
                if (dateAll.size() > 0) {
                    Date max = Collections.max(dateAll);
                    operatorTime.setCreatorTime(max);
                }
                flowTaskOperatorService.update(operatorTime);
            }
            //???????????????
            List<FlowTaskCirculateEntity> circulateList = new ArrayList<>();
            this.circulateList(nodeModel, circulateList, flowModel);
            flowTaskCirculateService.create(circulateList);
            //????????????
            FlowMsgModel flowMsgModel = new FlowMsgModel();
            flowMsgModel.setCirculateList(circulateList);
            flowMsgModel.setNodeList(taskNodeList);
            flowMsgModel.setMsgTitel(isEnd ? "??????????????????" : "??????????????????");
            flowMsgModel.setOperatorList(operatorList);
            flowMsgModel.setTaskEntity(flowTask);
            this.message(flowMsgModel);
        } catch (WorkFlowException e) {
            //??????????????????
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new WorkFlowException(e.getMessage());
        }
    }

    @Override
    public void reject(String id, FlowModel flowModel) throws WorkFlowException {
        FlowTaskOperatorEntity operator = flowTaskOperatorService.getInfo(id);
        if (operator != null) {
            FlowTaskEntity flowTaskEntity = flowTaskService.getInfo(operator.getTaskId());
            if (operator.getCompletion() == 0) {
                this.reject(flowTaskEntity, operator, flowModel);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(FlowTaskEntity flowTask, FlowTaskOperatorEntity operator, FlowModel flowModel) throws WorkFlowException {
        UserInfo userInfo = userProvider.get();
        String userId = StringUtil.isNotEmpty(flowModel.getUserId()) ? flowModel.getUserId() : userInfo.getUserId();
        //??????????????????
        List<FlowTaskNodeEntity> flowTaskNodeAll = flowTaskNodeService.getList(flowTask.getId());
        List<FlowTaskNodeEntity> taskNodeList = flowTaskNodeAll.stream().filter(t -> FlowNodeEnum.Process.getCode().equals(t.getState())).collect(Collectors.toList());
        //????????????
        FlowTaskNodeEntity taskNode = taskNodeList.stream().filter(m -> m.getId().equals(operator.getTaskNodeId())).findFirst().get();
        //??????????????????
        ChildNodeList nodeModel = JsonUtil.getJsonToBean(taskNode.getNodePropertyJson(), ChildNodeList.class);
        //????????????
        FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
        //??????????????????
        FlowOperatordModel flowOperatordModel = new FlowOperatordModel();
        flowOperatordModel.setStatus(FlowRecordEnum.reject.getCode());
        flowOperatordModel.setFlowModel(flowModel);
        flowOperatordModel.setUserId(userId);
        flowOperatordModel.setOperator(operator);
        this.operatorRecord(operatorRecord, flowOperatordModel);
        flowTaskOperatorRecordService.create(operatorRecord);
        //?????????????????????????????????
        this.handleIdStatus(0, nodeModel, operator, userInfo, taskNodeList);
        //??????????????????????????????
        flowTaskOperatorService.update(operator);
        boolean isUp = this.isReject(taskNode);
        //??????????????????
        List<ChildNodeList> nextOperatorList = new ArrayList<>();
        List<FlowTaskNodeEntity> upAll = this.isUpAll(taskNodeList, taskNode, isUp);
        Set<FlowTaskNodeEntity> thisStepAll = new HashSet<>();
        for (FlowTaskNodeEntity entity : upAll) {
            List<FlowTaskNodeEntity> collect = taskNodeList.stream().filter(t -> entity.getSortCode().equals(t.getSortCode())).collect(Collectors.toList());
            thisStepAll.addAll(collect);
            ChildNodeList node = JsonUtil.getJsonToBean(entity.getNodePropertyJson(), ChildNodeList.class);
            nextOperatorList.add(node);
        }
        long nodeSortCode = upAll.size() > 0 ? upAll.get(0).getSortCode() : 0;
        //????????????????????????
        Set<String> taskNodeId = taskNodeList.stream().filter(t -> t.getSortCode() >= nodeSortCode).map(t -> t.getId()).collect(Collectors.toSet());
        flowTaskOperatorRecordService.updateStatus(taskNodeId, flowTask.getId());
        //????????????
        List<FlowTaskOperatorEntity> operatorList = new ArrayList<>();
        //?????????????????????????????????????????????
        boolean isStart = nextOperatorList.stream().filter(t -> FlowNature.NodeStart.equals(t.getCustom().getType())).count() > 0;
        if (!isStart) {
            //????????????
            FlowEngineEntity engine = flowEngineService.getInfo(flowTask.getFlowId());
            flowModel.setProcessId(flowTask.getId());
            flowModel.setId(flowTask.getId());
            if (FlowNature.CUSTOM.equals(engine.getFormType())) {
                Map<String, Object> formDataAll = flowModel.getFormData();
                Map<String, Object> data = JsonUtil.stringToMap(String.valueOf(formDataAll.get("data")));
                flowModel.setFormData(data);
            }
            this.nextOperator(operatorList, nextOperatorList, flowTask, flowModel);
            //?????????????????????????????????
            List<String> rejectList = taskNodeList.stream().filter(t -> t.getSortCode() >= nodeSortCode).map(t -> t.getId()).collect(Collectors.toList());
            flowTaskNodeService.updateCompletion(rejectList, 0);
        } else {
            flowTaskNodeService.update(flowTask.getId());
            flowTaskOperatorService.update(flowTask.getId());
        }
        flowTaskOperatorService.create(operatorList);
        //????????????????????????
        List<String> stepIdList = new ArrayList<>();
        List<String> stepNameList = new ArrayList<>();
        List<String> progressList = new ArrayList<>();
        for (FlowTaskNodeEntity taskNodes : thisStepAll) {
            ChildNodeList childNode = JsonUtil.getJsonToBean(taskNodes.getNodePropertyJson(), ChildNodeList.class);
            Properties properties = childNode.getProperties();
            String progress = properties.getProgress();
            if (StringUtil.isNotEmpty(progress)) {
                progressList.add(progress);
            }
            stepIdList.add(taskNodes.getNodeCode());
            stepNameList.add(taskNodes.getNodeName());
        }
        //??????????????????????????????????????????
        if (thisStepAll.size() > 0) {
            Collections.sort(progressList);
            flowTask.setCompletion(progressList.size() > 0 ? Integer.valueOf(progressList.get(0)) : 0);
            flowTask.setThisStepId(String.join(",", stepIdList));
            flowTask.setThisStep(String.join(",", stepNameList));
            //???????????????????????????????????????
            flowTask.setStatus(isStart ? FlowTaskStatusEnum.Reject.getCode() : flowTask.getStatus());
            //?????????????????????????????????
            Set<String> nodeCode = new HashSet<>();
            nodeCode.add(operator.getNodeCode());
            flowTaskOperatorService.updateReject(operator.getTaskId(), nodeCode);
        }
        //??????????????????
        flowTaskService.updateById(flowTask);
        //???????????????
        List<FlowTaskCirculateEntity> circulateList = new ArrayList<>();
        nodeModel.getProperties().setCirculatePosition(new ArrayList<>());
        nodeModel.getProperties().setCirculateRole(new ArrayList<>());
        nodeModel.getProperties().setCirculateUser(new ArrayList<>());
        this.circulateList(nodeModel, circulateList, flowModel);
        flowTaskCirculateService.create(circulateList);
        //????????????
        this.event(FlowRecordEnum.reject.getCode(), nodeModel, operatorRecord);
        //????????????
        FlowMsgModel flowMsgModel = new FlowMsgModel();
        flowMsgModel.setCirculateList(new ArrayList<>());
        flowMsgModel.setNodeList(taskNodeList);
        flowMsgModel.setMsgTitel("??????????????????");
        flowMsgModel.setOperatorList(operatorList);
        flowMsgModel.setTaskEntity(flowTask);
        this.message(flowMsgModel);
    }

    @Override
    public void recall(String id, FlowTaskOperatorRecordEntity operatorRecord, FlowModel flowModel) throws WorkFlowException {
        UserInfo userInfo = userProvider.get();
        FlowTaskEntity flowTask = flowTaskService.getInfo(operatorRecord.getTaskId());
        if (FlowNature.CompletionEnd.equals(flowTask.getCompletion())) {
            throw new WorkFlowException("??????????????????????????????????????????");
        }
        //??????????????????
        List<FlowTaskNodeEntity> flowTaskNodeAllList = flowTaskNodeService.getList(flowTask.getId());
        List<FlowTaskNodeEntity> taskNodeList = flowTaskNodeAllList.stream().filter(t -> FlowNodeEnum.Process.getCode().equals(t.getState())).collect(Collectors.toList());
        //????????????
        FlowTaskNodeEntity recallNode = taskNodeList.stream().filter(t -> t.getNodeCode().equals(operatorRecord.getNodeCode())).findFirst().get();
        //??????????????????
        ChildNodeList nodeModel = JsonUtil.getJsonToBean(recallNode.getNodePropertyJson(), ChildNodeList.class);
        //????????????
        List<FlowTaskOperatorEntity> operatorList = flowTaskOperatorService.getList(flowTask.getId());
        //???????????????????????????
        List<FlowTaskNodeEntity> nextFlowTaskNodeList = flowTaskNodeAllList.stream().filter(t -> t.getSortCode() == recallNode.getSortCode() + 1).collect(Collectors.toList());
        List<FlowTaskOperatorEntity> isOperatorList = new ArrayList<>();
        for (FlowTaskNodeEntity node : nextFlowTaskNodeList) {
            List<FlowTaskOperatorEntity> operator = operatorList.stream().filter(t -> t.getNodeCode().equals(node.getNodeCode()) && t.getCompletion() == 0).collect(Collectors.toList());
            isOperatorList.addAll(operator);
        }
        if (FlowTaskStatusEnum.Reject.getCode().equals(flowTask.getStatus())) {
            throw new WorkFlowException("????????????????????????");
        }
        boolean isRecall = (isOperatorList.size() == 0 || FlowTaskStatusEnum.Adopt.getCode().equals(flowTask.getStatus()));
        if (isRecall) {
            throw new WorkFlowException("??????????????????????????????????????????");
        }
        //????????????????????????
        List<String> stepIdList = new ArrayList<>();
        List<String> stepNameList = new ArrayList<>();
        List<String> progressList = new ArrayList<>();
        List<FlowTaskNodeEntity> thisStepAll = flowTaskNodeAllList.stream().filter(t -> t.getSortCode().equals(recallNode.getSortCode())).collect(Collectors.toList());
        for (FlowTaskNodeEntity taskNodes : thisStepAll) {
            ChildNodeList childNode = JsonUtil.getJsonToBean(taskNodes.getNodePropertyJson(), ChildNodeList.class);
            Properties properties = childNode.getProperties();
            String progress = properties.getProgress();
            if (StringUtil.isNotEmpty(progress)) {
                progressList.add(progress);
            }
            stepIdList.add(taskNodes.getNodeCode());
            stepNameList.add(taskNodes.getNodeName());
        }
        Collections.sort(progressList);
        flowTask.setCompletion(progressList.size() > 0 ? Integer.valueOf(progressList.get(0)) : null);
        flowTask.setThisStepId(String.join(",", stepIdList));
        flowTask.setThisStep(String.join(",", stepNameList));
        //??????????????????
        flowTaskService.updateById(flowTask);
        //??????????????????
        List<FlowTaskNodeEntity> recallNodeList = flowTaskNodeAllList.stream().filter(t -> t.getSortCode().equals(recallNode.getSortCode())).collect(Collectors.toList());
        for (FlowTaskNodeEntity nodeEntity : recallNodeList) {
            nodeEntity.setCompletion(0);
            flowTaskNodeService.update(nodeEntity);
        }
        List<FlowTaskNodeEntity> beforeNodeList = flowTaskNodeAllList.stream().filter(t -> t.getSortCode() != -1 && t.getSortCode() < recallNode.getSortCode()).collect(Collectors.toList());
        for (FlowTaskNodeEntity nodeEntity : beforeNodeList) {
            nodeEntity.setCompletion(1);
            flowTaskNodeService.update(nodeEntity);
        }
        //????????????????????????
        Set<String> nodeCodeAll = operatorList.stream().filter(t -> t.getCompletion() == 0).map(t -> t.getNodeCode()).collect(Collectors.toSet());
        flowTaskOperatorService.updateReject(flowTask.getId(), nodeCodeAll);
        //??????????????????
        FlowTaskOperatorEntity recallOperator = flowTaskOperatorService.getInfo(operatorRecord.getTaskOperatorId());
        recallOperator.setHandleStatus(null);
        recallOperator.setHandleTime(null);
        recallOperator.setCompletion(0);
        flowTaskOperatorService.update(recallOperator);
        //????????????????????????
        List<String> recallList = recallNodeList.stream().map(t -> t.getId()).collect(Collectors.toList());
        flowTaskNodeService.updateCompletion(recallList, 0);
        List<String> beforeList = flowTaskNodeAllList.stream().filter(t -> t.getSortCode() < recallNode.getSortCode()).map(t -> t.getId()).collect(Collectors.toList());
        flowTaskNodeService.updateCompletion(beforeList, 1);
        //????????????
        FlowTaskOperatorEntity operator = JsonUtil.getJsonToBean(operatorRecord, FlowTaskOperatorEntity.class);
        operator.setId(operatorRecord.getTaskOperatorId());
        //??????????????????
        FlowOperatordModel flowOperatordModel = new FlowOperatordModel();
        flowOperatordModel.setStatus(FlowRecordEnum.revoke.getCode());
        flowOperatordModel.setFlowModel(flowModel);
        flowOperatordModel.setUserId(userInfo.getUserId());
        flowOperatordModel.setOperator(operator);
        this.operatorRecord(operatorRecord, flowOperatordModel);
        flowTaskOperatorRecordService.create(operatorRecord);
        //????????????
        this.event(FlowRecordEnum.recall.getCode(), nodeModel, operatorRecord);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revoke(FlowTaskEntity flowTask, FlowModel flowModel) {
        UserInfo userInfo = userProvider.get();
        List<FlowTaskNodeEntity> list = flowTaskNodeService.getList(flowTask.getId());
        FlowTaskNodeEntity start = list.stream().filter(t -> FlowNature.NodeStart.equals(String.valueOf(t.getNodeType()))).findFirst().orElse(null);
        //????????????
        flowTaskNodeService.deleteByTaskId(flowTask.getId());
        //????????????
        flowTaskOperatorService.deleteByTaskId(flowTask.getId());
        //??????????????????
        flowTask.setThisStepId(start.getNodeCode());
        flowTask.setThisStep(start.getNodeName());
        flowTask.setCompletion(0);
        flowTask.setStatus(FlowTaskStatusEnum.Revoke.getCode());
        flowTask.setStartTime(null);
        flowTask.setEndTime(null);
        flowTaskService.updateById(flowTask);
        //????????????
        FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
        operatorRecord.setTaskId(flowTask.getId());
        operatorRecord.setHandleStatus(FlowRecordEnum.revoke.getCode());
        FlowTaskOperatorEntity operator = JsonUtil.getJsonToBean(operatorRecord, FlowTaskOperatorEntity.class);
        //??????????????????
        FlowOperatordModel flowOperatordModel = new FlowOperatordModel();
        flowOperatordModel.setStatus(FlowRecordEnum.revoke.getCode());
        flowOperatordModel.setFlowModel(flowModel);
        flowOperatordModel.setUserId(userInfo.getUserId());
        flowOperatordModel.setOperator(operator);
        this.operatorRecord(operatorRecord, flowOperatordModel);
        flowTaskOperatorRecordService.create(operatorRecord);
        //????????????
        ChildNodeList nodeModel = JsonUtil.getJsonToBean(start.getNodePropertyJson(), ChildNodeList.class);
        this.event(FlowRecordEnum.revoke.getCode(), nodeModel, operatorRecord);
        //???????????????????????????
        this.delChild(flowTask);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(FlowTaskEntity flowTask, FlowModel flowModel) {
        UserInfo userInfo = userProvider.get();
        //????????????
        FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
        FlowTaskOperatorEntity operator = new FlowTaskOperatorEntity();
        operator.setTaskId(flowTask.getId());
        operator.setNodeCode(flowTask.getThisStepId());
        operator.setNodeName(flowTask.getThisStep());
        //??????????????????
        FlowOperatordModel flowOperatordModel = new FlowOperatordModel();
        flowOperatordModel.setStatus(FlowRecordEnum.cancel.getCode());
        flowOperatordModel.setFlowModel(flowModel);
        flowOperatordModel.setUserId(userInfo.getUserId());
        flowOperatordModel.setOperator(operator);
        this.operatorRecord(operatorRecord, flowOperatordModel);
        flowTaskOperatorRecordService.create(operatorRecord);
        //????????????
        List<FlowTaskNodeEntity> flowTaskNodeAllList = flowTaskNodeService.getList(flowTask.getId());
        FlowTaskNodeEntity startNode = flowTaskNodeAllList.stream().filter(t -> FlowNature.NodeStart.equals(t.getNodeType())).findFirst().get();
        flowTask.setThisStepId(startNode.getNodeCode());
        flowTask.setThisStep(startNode.getNodeName());
        flowTask.setStatus(FlowTaskStatusEnum.Cancel.getCode());
        flowTask.setEndTime(new Date());
        flowTaskService.updateById(flowTask);
    }

    @Override
    public boolean assign(String id, FlowHandleModel flowHandleModel) {
        List<FlowTaskOperatorEntity> list = flowTaskOperatorService.getList(id).stream().filter(t -> FlowNodeEnum.Process.getCode().equals(t.getState()) && flowHandleModel.getNodeCode().equals(t.getNodeCode())).collect(Collectors.toList());
        boolean isOk = list.size() > 0;
        if (list.size() > 0) {
            FlowTaskOperatorEntity entity = list.get(0);
            entity.setHandleStatus(null);
            entity.setHandleTime(null);
            entity.setCompletion(0);
            entity.setCreatorTime(new Date());
            entity.setHandleId(flowHandleModel.getFreeApproverUserId());
            List<String> idAll = list.stream().map(t -> t.getId()).collect(Collectors.toList());
            flowTaskOperatorService.deleteList(idAll);
            List<FlowTaskOperatorEntity> addList = new ArrayList<>();
            addList.add(entity);
            flowTaskOperatorService.create(addList);
            //????????????
            UserInfo userInfo = userProvider.get();
            FlowModel flowModel = JsonUtil.getJsonToBean(flowHandleModel, FlowModel.class);
            FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
            FlowTaskOperatorEntity operator = new FlowTaskOperatorEntity();
            operator.setTaskId(entity.getTaskId());
            operator.setNodeCode(entity.getNodeCode());
            operator.setNodeName(entity.getNodeName());
            //??????????????????
            FlowOperatordModel flowOperatordModel = new FlowOperatordModel();
            flowOperatordModel.setStatus(FlowRecordEnum.assign.getCode());
            flowOperatordModel.setFlowModel(flowModel);
            flowOperatordModel.setUserId(userInfo.getUserId());
            flowOperatordModel.setOperator(operator);
            flowOperatordModel.setOperatorId(entity.getHandleId());
            this.operatorRecord(operatorRecord, flowOperatordModel);
            flowTaskOperatorRecordService.create(operatorRecord);
        }
        return isOk;
    }

    @Override
    public void transfer(FlowTaskOperatorEntity taskOperator) {
        flowTaskOperatorService.update(taskOperator);
        //????????????
        UserInfo userInfo = userProvider.get();
        FlowModel flowModel = new FlowModel();
        FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
        FlowTaskOperatorEntity operator = new FlowTaskOperatorEntity();
        operator.setTaskId(taskOperator.getTaskId());
        operator.setNodeCode(taskOperator.getNodeCode());
        operator.setNodeName(taskOperator.getNodeName());
        //??????????????????
        FlowOperatordModel flowOperatordModel = new FlowOperatordModel();
        flowOperatordModel.setStatus(FlowRecordEnum.transfer.getCode());
        flowOperatordModel.setFlowModel(flowModel);
        flowOperatordModel.setUserId(userInfo.getUserId());
        flowOperatordModel.setOperator(operator);
        flowOperatordModel.setOperatorId(taskOperator.getHandleId());
        this.operatorRecord(operatorRecord, flowOperatordModel);
        flowTaskOperatorRecordService.create(operatorRecord);
    }

    @Override
    public FlowBeforeInfoVO getBeforeInfo(String id, String taskNodeId) throws WorkFlowException {
        FlowBeforeInfoVO vo = new FlowBeforeInfoVO();
        FlowTaskEntity taskEntity = flowTaskService.getInfo(id);
        List<FlowTaskNodeEntity> taskNodeAllList = flowTaskNodeService.getList(taskEntity.getId()).stream().filter(t -> FlowNodeEnum.Process.getCode().equals(t.getState()) || FlowNodeEnum.Futility.getCode().equals(t.getState())).collect(Collectors.toList());
        List<FlowTaskNodeEntity> taskNodeList = taskNodeAllList.stream().sorted(Comparator.comparing(FlowTaskNodeEntity::getSortCode)).collect(Collectors.toList());
        List<FlowTaskOperatorEntity> taskOperatorList = flowTaskOperatorService.getList(taskEntity.getId()).stream().filter(t -> FlowNodeEnum.Process.getCode().equals(t.getState())).collect(Collectors.toList());
        List<FlowTaskOperatorRecordEntity> operatorRecordList = flowTaskOperatorRecordService.getList(taskEntity.getId());
        boolean colorFlag = true;
        //????????????
        List<UserEntity> userList = userService.getList();
        List<FlowTaskOperatorRecordModel> recordList = new ArrayList<>();
        for (FlowTaskOperatorRecordEntity entity : operatorRecordList) {
            UserEntity userName = userList.stream().filter(t -> t.getId().equals(entity.getHandleId())).findFirst().orElse(null);
            FlowTaskOperatorRecordModel infoModel = JsonUtil.getJsonToBean(entity, FlowTaskOperatorRecordModel.class);
            infoModel.setUserName(userName != null ? userName.getRealName() + "/" + userName.getAccount() : "");
            UserEntity operatorName = userList.stream().filter(t -> t.getId().equals(entity.getOperatorId())).findFirst().orElse(null);
            infoModel.setOperatorId(operatorName != null ? operatorName.getRealName() + "/" + operatorName.getAccount() : "");
            recordList.add(infoModel);
        }
        vo.setFlowTaskOperatorRecordList(recordList);
        //????????????
        String[] tepId = taskEntity.getThisStepId() != null ? taskEntity.getThisStepId().split(",") : new String[]{};
        List<String> tepIdAll = Arrays.asList(tepId);
        List<FlowTaskNodeModel> flowTaskNodeListAll = JsonUtil.getJsonToList(taskNodeList, FlowTaskNodeModel.class);
        for (FlowTaskNodeModel model : flowTaskNodeListAll) {
            //?????????????????????
            if (colorFlag || model.getCompletion() == 1) {
                if (model.getSortCode() != -2) {
                    model.setType("0");
                }
            }
            if (tepIdAll.contains(model.getNodeCode())) {
                model.setType("1");
                colorFlag = false;
                if (FlowNature.NodeEnd.equals(model.getNodeCode())) {
                    model.setType("0");
                }
            }
            //???????????????
            ChildNodeList childNode = JsonUtil.getJsonToBean(model.getNodePropertyJson(), ChildNodeList.class);
            Custom custom = childNode.getCustom();
            Properties properties = childNode.getProperties();
            String type = properties.getAssigneeType();
            List<FlowTaskOperatorEntity> operatorList = new ArrayList<>();
            FlowModel flowModel = new FlowModel();
            this.operator(childNode, operatorList, taskEntity, flowModel, userList, false);
            List<String> userName = new ArrayList<>();
            if (FlowNature.NodeStart.equals(custom.getType())) {
                UserEntity startUser = userList.stream().filter(t -> t.getId().equals(taskEntity.getCreatorUserId())).findFirst().orElse(null);
                userName.add(startUser != null ? startUser.getRealName() + "/" + startUser.getAccount() : "");
            } else if (FlowNature.NodeSubFlow.equals(custom.getType())) {
                List<String> list = this.childSaveList(childNode, taskEntity, userList);
                List<String> nameList = new ArrayList<>();
                for (String userId : list) {
                    UserEntity user = userList.stream().filter(t -> t.getId().equals(userId)).findFirst().orElse(null);
                    if (user != null) {
                        nameList.add(user.getRealName() + "/" + user.getAccount());
                    }
                }
                userName.addAll(nameList);
            } else if (!FlowNature.NodeEnd.equals(custom.getNodeId())) {
                boolean isShow = true;
                //????????????????????????????????????????????????
                if (FlowTaskOperatorEnum.Tache.getCode().equals(type)) {
                    boolean completion = ("0".equals(model.getType()) || "1".equals(model.getType()));
                    if (!completion) {
                        isShow = false;
                    }
                }
                if (isShow) {
                    List<String> nameList = new ArrayList<>();
                    for (FlowTaskOperatorEntity operator : operatorList) {
                        UserEntity user = userList.stream().filter(t -> t.getId().equals(operator.getHandleId())).findFirst().orElse(null);
                        if (user != null) {
                            nameList.add(user.getRealName() + "/" + user.getAccount());
                        }
                    }
                    userName.addAll(nameList);
                }
            }
            model.setUserName(String.join(",", userName));
        }
        vo.setFlowTaskNodeList(flowTaskNodeListAll);
        //????????????
        Properties approversProperties = new Properties();
        if (StringUtil.isNotEmpty(taskNodeId)) {
            FlowTaskNodeEntity taskNode = flowTaskNodeService.getInfo(taskNodeId);
            vo.setFormOperates(new ArrayList<>());
            if (taskNode != null) {
                ChildNodeList childNode = JsonUtil.getJsonToBean(taskNode.getNodePropertyJson(), ChildNodeList.class);
                approversProperties = childNode.getProperties();
                vo.setFormOperates(childNode.getProperties().getFormOperates());
            }
        }
        FlowJsonUtil.assignment(approversProperties);
        vo.setApproversProperties(approversProperties);
        //????????????
        FlowTaskModel inof = JsonUtil.getJsonToBean(taskEntity, FlowTaskModel.class);
        FlowEngineEntity engine = flowEngineService.getInfo(taskEntity.getFlowId());
        inof.setAppFormUrl(engine.getAppFormUrl());
        inof.setFormUrl(engine.getFormUrl());
        inof.setType(engine.getType());
        vo.setFlowTaskInfo(inof);
        //????????????
        vo.setFlowTaskOperatorList(JsonUtil.getJsonToList(taskOperatorList, FlowTaskOperatorModel.class));
        //????????????
        vo.setFlowFormInfo(taskEntity.getFlowForm());
        return vo;
    }

    //-----------------------------------????????????--------------------------------------------

    /**
     * ??????????????????
     *
     * @param taskEntity ??????????????????
     * @param engine     ??????????????????
     * @param flowModel  ????????????
     * @throws WorkFlowException ??????
     */
    private void task(FlowTaskEntity taskEntity, FlowEngineEntity engine, FlowModel flowModel, String userId) throws WorkFlowException {
        if (flowModel.getId() != null && !checkStatus(taskEntity.getStatus())) {
            throw new WorkFlowException("??????????????????????????????????????????");
        }
        //????????????
        taskEntity.setId(flowModel.getProcessId());
        taskEntity.setProcessId(flowModel.getProcessId());
        taskEntity.setEnCode(flowModel.getBillNo());
        taskEntity.setFullName(flowModel.getFlowTitle());
        taskEntity.setFlowUrgent(flowModel.getFlowUrgent() != null ? flowModel.getFlowUrgent() : 1);
        taskEntity.setFlowId(engine.getId());
        taskEntity.setFlowCode(engine.getEnCode() != null ? engine.getEnCode() : "?????????????????????");
        taskEntity.setFlowName(engine.getFullName());
        taskEntity.setFlowType(engine.getType());
        taskEntity.setFlowCategory(engine.getCategory());
        taskEntity.setFlowForm(engine.getFormData());
        taskEntity.setFlowTemplateJson(engine.getFlowTemplateJson());
        taskEntity.setFlowVersion(engine.getVersion());
        taskEntity.setStatus(FlowStatusEnum.save.getMessage().equals(flowModel.getStatus()) ? FlowTaskStatusEnum.Draft.getCode() : FlowTaskStatusEnum.Handle.getCode());
        taskEntity.setCompletion(0);
        taskEntity.setStartTime(new Date());
        taskEntity.setCreatorTime(new Date());
        taskEntity.setCreatorUserId(userId);
        taskEntity.setFlowFormContentJson(flowModel.getFormData() != null ? JsonUtilEx.getObjectToString(flowModel.getFormData()) : "{}");
        taskEntity.setParentId(flowModel.getParentId() != null ? flowModel.getParentId() : "0");
    }

    /**
     * ??????????????????
     *
     * @param status ????????????
     * @return
     */
    private boolean checkStatus(int status) {
        if (status == FlowTaskStatusEnum.Draft.getCode() || status == FlowTaskStatusEnum.Reject.getCode() || status == FlowTaskStatusEnum.Revoke.getCode()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * ????????????
     *
     * @param dataAll ??????????????????
     */
    private void nodeListAll(List<FlowTaskNodeEntity> dataAll) {
        FlowTaskNodeEntity startNodes = dataAll.stream().filter(t -> FlowNature.NodeStart.equals(t.getNodeType())).findFirst().get();
        List<FlowTaskNodeEntity> treeList = new ArrayList<>();
        long num = 1L;
        this.nodeList(dataAll, startNodes.getNodeCode(), treeList, num);
        String nodeNext = FlowNature.NodeEnd;
        String type = "endround";
        long maxNum = 1L;
        for (FlowTaskNodeEntity entity : dataAll) {
            if (StringUtil.isEmpty(entity.getNodeNext())) {
                entity.setNodeNext(nodeNext);
            }
            if (entity.getSortCode() != null && entity.getSortCode() > maxNum) {
                maxNum = entity.getSortCode();
            }
            if (!"timer".equals(entity.getNodeType())) {
                flowTaskNodeService.save(entity);
            }
        }
        FlowTaskNodeEntity endround = new FlowTaskNodeEntity();
        endround.setId(RandomUtil.uuId());
        endround.setNodeCode(nodeNext);
        endround.setNodeName("??????");
        endround.setCompletion(0);
        endround.setCreatorTime(new Date());
        endround.setSortCode(++maxNum);
        endround.setTaskId(startNodes.getTaskId());
        ChildNodeList endNode = JsonUtil.getJsonToBean(startNodes.getNodePropertyJson(), ChildNodeList.class);
        endNode.getCustom().setNodeId(nodeNext);
        endNode.setTaskNodeId(endround.getId());
        endNode.getCustom().setType(type);
        endround.setNodePropertyJson(JsonUtil.getObjectToString(endNode));
        endround.setNodeType(type);
        endround.setState(FlowNodeEnum.Process.getCode());
        flowTaskNodeService.save(endround);
    }

    /**
     * ??????????????????
     *
     * @param dataAll  ????????????
     * @param node     ????????????
     * @param treeList ?????????????????????
     * @param num      ??????
     */
    private void nodeList(List<FlowTaskNodeEntity> dataAll, String node, List<FlowTaskNodeEntity> treeList, long num) {
        List<String> nodeAll = Arrays.asList(node.split(","));
        List<FlowTaskNodeEntity> nodeList = dataAll.stream().filter(t -> nodeAll.contains(t.getNodeCode())).collect(Collectors.toList());
        for (FlowTaskNodeEntity entity : nodeList) {
            entity.setSortCode(num);
            entity.setState(FlowNodeEnum.Process.getCode());
        }
        treeList.addAll(nodeList);
        List<String> nextNode = nodeList.stream().filter(t -> t.getNodeNext() != null).map(t -> t.getNodeNext()).collect(Collectors.toList());
        if (nextNode.size() > 0) {
            String nodes = String.join(",", nextNode);
            num++;
            nodeList(dataAll, nodes, treeList, num);
        }
    }

    /**
     * ????????????
     *
     * @param flowTask
     * @param nodeListAll
     * @param conditionListAll
     * @param taskNodeList
     */
    private void createNodeList(FlowTaskEntity flowTask, List<ChildNodeList> nodeListAll, List<ConditionList> conditionListAll, List<FlowTaskNodeEntity> taskNodeList) {
        List<FlowTaskNodeEntity> timerList = new ArrayList<>();
        List<FlowTaskNodeEntity> emptyList = new ArrayList<>();
        for (ChildNodeList childNode : nodeListAll) {
            FlowTaskNodeEntity taskNode = new FlowTaskNodeEntity();
            String nodeId = childNode.getCustom().getNodeId();
            Properties properties = childNode.getProperties();
            String dataJson = flowTask.getFlowFormContentJson();
            String type = childNode.getCustom().getType();
            taskNode.setId(RandomUtil.uuId());
            childNode.setTaskNodeId(taskNode.getId());
            childNode.setTaskId(flowTask.getId());
            taskNode.setCreatorTime(new Date());
            taskNode.setTaskId(flowTask.getId());
            taskNode.setNodeCode(nodeId);
            taskNode.setNodeType(type);
            taskNode.setState(FlowNodeEnum.Futility.getCode());
            taskNode.setSortCode(-2L);
            taskNode.setNodeUp(properties.getRejectStep());
            taskNode.setNodeNext(FlowJsonUtil.getNextNode(nodeId, dataJson, nodeListAll, conditionListAll));
            taskNode.setNodePropertyJson(JsonUtilEx.getObjectToString(childNode));
            boolean isSstart = FlowNature.NodeStart.equals(childNode.getCustom().getType());
            taskNode.setCompletion(isSstart ? 1 : 0);
            taskNode.setNodeName(isSstart ? "??????" : properties.getTitle());
            taskNodeList.add(taskNode);
            if ("empty".equals(type)) {
                emptyList.add(taskNode);
            }
            if ("timer".equals(type)) {
                timerList.add(taskNode);
            }
        }
        //??????empty??????????????????????????????
        for (FlowTaskNodeEntity empty : emptyList) {
            List<FlowTaskNodeEntity> noxtEmptyList = taskNodeList.stream().filter(t -> t.getNodeNext().contains(empty.getNodeCode())).collect(Collectors.toList());
            for (FlowTaskNodeEntity entity : noxtEmptyList) {
                entity.setNodeNext(empty.getNodeNext());
            }
        }
        //??????timer??????????????????????????????
        for (FlowTaskNodeEntity timer : timerList) {
            //?????????timer???????????????
            ChildNodeList timerlList = JsonUtil.getJsonToBean(timer.getNodePropertyJson(), ChildNodeList.class);
            DateProperties timers = timerlList.getTimer();
            timers.setNodeId(timer.getNodeCode());
            timers.setTime(true);
            List<FlowTaskNodeEntity> upEmptyList = taskNodeList.stream().filter(t -> t.getNodeNext().contains(timer.getNodeCode())).collect(Collectors.toList());
            for (FlowTaskNodeEntity entity : upEmptyList) {
                //??????????????????timer?????????
                ChildNodeList modelList = JsonUtil.getJsonToBean(entity.getNodePropertyJson(), ChildNodeList.class);
                modelList.setTimer(timers);
                entity.setNodeNext(timer.getNodeNext());
                entity.setNodePropertyJson(JsonUtilEx.getObjectToString(modelList));
            }
        }
    }

    //-------------------------??????--------------------------------
    //---------??????-------------

    /**
     * ???????????????????????????
     *
     * @param nodeIdAll     ??????????????????id
     * @param taskNodeList ????????????
     * @param operatorList ??????????????????
     */
    private void totalAll(Map<String, List<String>> nodeIdAll, List<FlowTaskOperatorEntity> operatorList, List<FlowTaskNodeEntity> taskNodeList) {
        //?????????????????????id
        for (String nodeId : nodeIdAll.keySet()) {
            FlowTaskNodeEntity entity = flowTaskNodeService.getInfo(nodeId);
            if (entity != null) {
                ChildNodeList childNodeList = JsonUtil.getJsonToBean(entity.getNodePropertyJson(), ChildNodeList.class);
                childNodeList.getCustom().setTaskId(nodeIdAll.get(nodeId));
                entity.setNodePropertyJson(JsonUtil.getObjectToString(childNodeList));
                flowTaskNodeService.update(entity);
            }
        }
        //???????????????????????????
        Map<String, Long> countAll = operatorList.stream().collect(Collectors.groupingBy(FlowTaskOperatorEntity::getTaskNodeId, Collectors.counting()));
        for (String key : countAll.keySet()) {
            FlowTaskNodeEntity entity = taskNodeList.stream().filter(t -> t.getId().equals(key)).findFirst().orElse(null);
            if (entity != null) {
                ChildNodeList childNodeList = JsonUtil.getJsonToBean(entity.getNodePropertyJson(), ChildNodeList.class);
                childNodeList.setTotal((double) countAll.get(key));
                entity.setNodePropertyJson(JsonUtil.getObjectToString(childNodeList));
                flowTaskNodeService.update(entity);
            }
        }
    }

    /**
     * ???????????????
     *
     * @param operatorListAll ???????????????
     * @param nodeList        ?????????????????????
     * @param taskEntity      ????????????
     * @param flowModel       ????????????
     * @throws WorkFlowException ??????
     */
    private Map<String, List<String>> nextOperator(List<FlowTaskOperatorEntity> operatorListAll, List<ChildNodeList> nodeList, FlowTaskEntity taskEntity, FlowModel flowModel) throws WorkFlowException {
        Map<String, List<String>> taskNode = new HashMap<>(16);
        try {
            List<UserEntity> userList = userService.getList();
            //???????????????
            for (ChildNodeList childNode : nodeList) {
                List<FlowTaskOperatorEntity> operatorList = new ArrayList<>();
                Custom custom = childNode.getCustom();
                Properties properties = childNode.getProperties();
                String type = custom.getType();
                String flowId = properties.getFlowId();
                List<FlowAssignModel> assignList = childNode.getProperties().getAssignList();
                //??????????????????
                TimeOutConfig config = properties.getTimeoutConfig();
                //???????????????
                boolean isChild = FlowNature.NodeSubFlow.equals(type);
                if (isChild) {
                    //??????????????????????????????
                    FlowEngineEntity parentEngine = flowEngineService.getInfo(taskEntity.getFlowId());
                    boolean isCustom = FlowNature.CUSTOM.equals(parentEngine.getFormType());
                    List<String> taskNodeList = new ArrayList<>();
                    FlowEngineEntity engine = flowEngineService.getInfo(flowId);
                    //???????????????
                    Map<String, Object> data = this.childData(engine, flowModel, assignList, isCustom);
                    data.put("flowId", flowId);
                    //??????????????????
                    List<String> list = this.childSaveList(childNode, taskEntity, userList);
                    for (String id : list) {
                        UserEntity userEntity = userList.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
                        String title = userEntity.getRealName() + "???" + engine.getFullName() + "(?????????)";
                        FlowModel nextFlowModel = this.assignment(data, parentEngine, taskEntity.getId(), title);
                        nextFlowModel.setUserId(id);
                        nextFlowModel.setFlowTitle(title);
                        nextFlowModel.setFormData(data);
                        nextFlowModel.setFlowId(engine.getId());
                        FlowTaskEntity childTaskEntity = this.save(nextFlowModel);
                        this.createData(engine, childTaskEntity, nextFlowModel);
                        taskNodeList.add(nextFlowModel.getProcessId());
                    }
                    taskNode.put(childNode.getTaskNodeId(), taskNodeList);
                } else {
                    if (!FlowNature.NodeEnd.equals(childNode.getCustom().getNodeId())) {
                        //?????????
                        this.operator(childNode, operatorList, taskEntity, flowModel, userList, true);
                    }
                }
                operatorListAll.addAll(operatorList);
            }
        } catch (WorkFlowException e) {
            throw new WorkFlowException(e.getMessage());
        }
        return taskNode;
    }

    /**
     * ?????????
     *
     * @param childNode    ??????????????????
     * @param operatorList ?????????????????????
     * @param taskEntity   ????????????
     * @param flowModel    ????????????
     * @param details      true?????? false?????????
     * @throws WorkFlowException ??????
     */
    private void operator(ChildNodeList childNode, List<FlowTaskOperatorEntity> operatorList, FlowTaskEntity taskEntity, FlowModel flowModel, List<UserEntity> userList, boolean details) {
        String createUserId = taskEntity.getCreatorUserId();
        Date date = new Date();
        List<FlowTaskOperatorEntity> nextList = new ArrayList<>();
        Properties properties = childNode.getProperties();
        String type = properties.getAssigneeType();
        String userId = "";
        String freeApproverUserId = flowModel.getFreeApproverUserId();
        //????????????
        if (StringUtil.isNotEmpty(freeApproverUserId)) {
            this.operatorUser(nextList, freeApproverUserId, date, childNode, true);
            //????????????
            if (details) {
                UserInfo userInfo = userProvider.get();
                Custom custom = childNode.getCustom();
                FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
                FlowTaskOperatorEntity operator = new FlowTaskOperatorEntity();
                operator.setTaskId(childNode.getTaskId());
                operator.setNodeCode(custom.getNodeId());
                operator.setNodeName(properties.getTitle());
                //??????????????????
                FlowOperatordModel flowOperatordModel = new FlowOperatordModel();
                flowOperatordModel.setStatus(FlowRecordEnum.copyId.getCode());
                flowOperatordModel.setFlowModel(flowModel);
                flowOperatordModel.setUserId(userInfo.getUserId());
                flowOperatordModel.setOperator(operator);
                flowOperatordModel.setOperatorId(freeApproverUserId);
                this.operatorRecord(operatorRecord, flowOperatordModel);
                flowTaskOperatorRecordService.create(operatorRecord);
            }
        } else {
            //??????????????????????????????
            if (FlowTaskOperatorEnum.LaunchCharge.getCode().equals(type)) {
                //?????????????????????
                UserEntity info = userService.getInfo(createUserId);
                if (info != null) {
                    userId = getManagerByLevel(info.getManagerId(), properties.getManagerLevel(), userList);
                    this.operatorUser(nextList, userId, date, childNode);
                }
            }
            //???????????????????????????
            if (FlowTaskOperatorEnum.DepartmentCharge.getCode().equals(type)) {
                UserEntity userEntity = userService.getInfo(createUserId);
                OrganizeEntity organizeEntity = organizeService.getInfo(userEntity != null ? userEntity.getOrganizeId() : "");
                if (organizeEntity != null) {
                    userId = organizeEntity.getManager();
                    this.operatorUser(nextList, userId, date, childNode);
                }
            }
            //???????????????????????????
            if (FlowTaskOperatorEnum.InitiatorMe.getCode().equals(type)) {
                this.operatorUser(nextList, createUserId, date, childNode);
            }
            //????????????
            if (FlowTaskOperatorEnum.Tache.getCode().equals(type)) {
                List<FlowTaskOperatorRecordEntity> operatorUserList = flowTaskOperatorRecordService.getList(taskEntity.getId()).stream().filter(t -> properties.getNodeId().equals(t.getNodeCode()) && FlowRecordEnum.audit.getCode().equals(t.getHandleStatus()) && FlowNodeEnum.Process.getCode().equals(t.getStatus())).collect(Collectors.toList());
                if (operatorUserList.size() > 0) {
                    for (FlowTaskOperatorRecordEntity entity : operatorUserList) {
                        this.operatorUser(nextList, entity.getHandleId(), date, childNode);
                    }
                }
            }
            //????????????
            if (FlowTaskOperatorEnum.Variate.getCode().equals(type)) {
                Map<String, Object> dataAll = JsonUtil.stringToMap(taskEntity.getFlowFormContentJson());
                Object data = dataAll.get(properties.getFormField());
                if (data != null) {
                    List<String> handleId = Arrays.asList(String.valueOf(data).split(","));
                    List<String> userAll = userList.stream().filter(t -> handleId.contains(t.getId())).map(t -> t.getId()).collect(Collectors.toList());
                    for (String user : userAll) {
                        this.operatorUser(nextList, user, date, childNode);
                    }
                }
            }
            //????????????
            if (FlowTaskOperatorEnum.Serve.getCode().equals(type)) {
                String url = properties.getGetUserUrl();
                String token = UserProvider.getToken();
                JSONObject object = HttpUtil.httpRequest(url, "GET", null, token);
                if (object != null) {
                    if (object.get("data") != null) {
                        JSONObject data = object.getJSONObject("data");
                        List<String> handleId = StringUtil.isNotEmpty(data.getString("handleId")) ? Arrays.asList(data.getString("handleId").split(",")) : new ArrayList<>();
                        List<String> userAll = userList.stream().filter(t -> handleId.contains(t.getId())).map(t -> t.getId()).collect(Collectors.toList());
                        for (String user : userAll) {
                            this.operatorUser(nextList, user, date, childNode);
                        }
                    }
                }
            }
            //???????????????????????????
            for (String userIdAll : properties.getApprovers()) {
                this.operatorUser(nextList, userIdAll, date, childNode);
            }
            //???????????????????????????
            List<String> positionList = properties.getApproverPos();
            //???????????????????????????
            List<String> roleList = properties.getApproverRole();
            List<String> list = new ArrayList<>();
            list.addAll(positionList);
            list.addAll(roleList);
            List<UserRelationEntity> listByObjectIdAll = userRelationService.getListByObjectIdAll(list);
            List<String> userPosition = listByObjectIdAll.stream().map(t -> t.getUserId()).collect(Collectors.toList());
            this.getApproverUser(userPosition, nextList, childNode);
        }
        if (nextList.size() == 0) {
            this.operatorUser(nextList, user, date, childNode);
        }
        operatorList.addAll(nextList);
    }

    /**
     * ????????????
     *
     * @param managerId ??????id
     * @param level     ?????????
     * @return
     */
    private static String getManagerByLevel(String managerId, long level, List<UserEntity> userList) {
        --level;
        if (level == 0) {
            return managerId;
        } else {
            UserEntity userEntity = userList.stream().filter(t -> t.getId().equals(managerId)).findFirst().orElse(null);
            return userEntity != null ? getManagerByLevel(userEntity.getManagerId(), level, userList) : "";
        }
    }

    /**
     * ???????????????
     *
     * @param nextList  ?????????????????????
     * @param handLeId  ?????????id
     * @param date      ????????????
     * @param childNode ??????????????????
     */
    private void operatorUser(List<FlowTaskOperatorEntity> nextList, String handLeId, Date date, ChildNodeList childNode) {
        this.operatorUser(nextList, handLeId, date, childNode, false);
    }

    /**
     * ???????????????
     *
     * @param nextList  ?????????????????????
     * @param handLeId  ?????????id
     * @param date      ????????????
     * @param childNode ??????????????????
     * @param isStatus  ???????????????
     */
    private void operatorUser(List<FlowTaskOperatorEntity> nextList, String handLeId, Date date, ChildNodeList childNode, boolean isStatus) {
        Properties properties = childNode.getProperties();
        Custom custom = childNode.getCustom();
        String type = properties.getAssigneeType();
        FlowTaskOperatorEntity operator = new FlowTaskOperatorEntity();
        operator.setId(RandomUtil.uuId());
        operator.setHandleType(isStatus ? FlowTaskOperatorEnum.FreeApprover.getCode() : type);
        operator.setHandleId(StringUtil.isEmpty(handLeId) ? user : handLeId);
        operator.setTaskNodeId(childNode.getTaskNodeId());
        operator.setTaskId(childNode.getTaskId());
        operator.setNodeCode(custom.getNodeId());
        operator.setNodeName(properties.getTitle());
        operator.setDescription(JsonUtil.getObjectToString(new ArrayList<>()));
        operator.setCreatorTime(date);
        operator.setCompletion(0);
        operator.setType(type);
        operator.setState(FlowNodeEnum.Process.getCode());
        nextList.add(operator);
    }

    /**
     * ??????????????????
     *
     * @param userPosition ?????????
     * @param operatorList ???????????????
     * @param childNode    ????????????
     */
    private void getApproverUser(List<String> userPosition, List<FlowTaskOperatorEntity> operatorList, ChildNodeList childNode) {
        Date date = new Date();
        for (String user : userPosition) {
            //??????????????????????????????
            if (operatorList.stream().filter(t -> t.getHandleId().equals(user)).count() == 0) {
                this.operatorUser(operatorList, user, date, childNode);
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param status       ???????????????0????????????1????????????
     * @param nodeModel    ??????????????????
     * @param operator     ????????????
     * @param userInfo     ??????
     * @param taskNodeList ??????list
     */
    private void handleIdStatus(int status, ChildNodeList nodeModel, FlowTaskOperatorEntity operator, UserInfo userInfo, List<FlowTaskNodeEntity> taskNodeList) {
        Properties properties = nodeModel.getProperties();
        Integer counterSign = properties.getCounterSign();
        operator.setHandleTime(new Date());
        operator.setHandleStatus(status);
        String type = properties.getAssigneeType();
        boolean isApprover = FlowNature.FixedJointlyApprover.equals(counterSign);
        //???????????????id
        List<String> userIdListAll = new ArrayList<>();
        //?????????????????????????????????????????????????????????????????????????????????
        if (userInfo.getUserId().equals(operator.getHandleId())) {
            List<String> userList = flowDelegateService.getUser(userInfo.getUserId()).stream().map(t -> t.getCreatorUserId()).collect(Collectors.toList());
            userIdListAll.addAll(userList);
        }
        if (status == 1) {
            if (isApprover) {
                //???????????????????????????
                flowTaskOperatorService.update(operator.getTaskNodeId(), userIdListAll, "1");
            } else {
                //???????????????????????????
                flowTaskOperatorService.update(operator.getTaskNodeId(), type);
            }
            operator.setCompletion(1);
            //??????????????????????????????
            List<Date> list = JsonUtil.getJsonToList(operator.getDescription(), Date.class);
            DateProperties timer = nodeModel.getTimer();
            if (timer.getTime()) {
                Date date = new Date();
                date = DateUtil.dateAddDays(date, timer.getDay());
                date = DateUtil.dateAddHours(date, timer.getHour());
                date = DateUtil.dateAddMinutes(date, timer.getMinute());
                date = DateUtil.dateAddSeconds(date, timer.getSecond());
                list.add(date);
                operator.setDescription(JsonUtil.getObjectToString(list));
            }
        } else {
            if (isApprover) {
                //???????????????????????????
                flowTaskOperatorService.update(operator.getTaskNodeId(), userIdListAll, "-1");
            } else {
                //???????????????????????????
                flowTaskOperatorService.update(operator.getTaskNodeId(), type);
            }
            operator.setCompletion(-1);
            operator.setState(FlowNodeEnum.Futility.getCode());
        }
    }

    /**
     * ???????????????????????????
     *
     * @param nodeListAll    ????????????
     * @param nextNodeEntity ????????????
     * @param taskNode       ????????????
     * @param flowModel      ????????????
     * @return
     */
    private List<FlowTaskNodeEntity> isNextAll(List<FlowTaskNodeEntity> nodeListAll, List<FlowTaskNodeEntity> nextNodeEntity, FlowTaskNodeEntity taskNode, FlowModel flowModel) {
        //1.???????????????????????????????????????????????????????????????
        //2.??????????????????????????????
        //3.???????????????????????????
        //4.??????????????????????????????
        List<FlowTaskNodeEntity> result = new ArrayList<>();
        boolean hasFreeApprover = StringUtil.isNotEmpty(flowModel.getFreeApproverUserId());
        if (hasFreeApprover) {
            result.add(taskNode);
            //????????????
        } else {
            ChildNodeList nodeModel = JsonUtil.getJsonToBean(taskNode.getNodePropertyJson(), ChildNodeList.class);
            Properties properties = nodeModel.getProperties();
            //????????????
            boolean isCountersign = true;
            boolean fixed = FlowNature.FixedJointlyApprover.equals(properties.getCounterSign());
            long pass = properties.getCountersignRatio();
            String type = properties.getAssigneeType();
            //?????????????????????
            if (fixed) {
                List<FlowTaskOperatorEntity> operatorList = flowTaskOperatorService.getList(taskNode.getTaskId()).stream().filter(t -> t.getTaskNodeId().equals(taskNode.getId()) && FlowNodeEnum.Process.getCode().equals(t.getState())).collect(Collectors.toList());
                double total = operatorList.size();
                double passNum = operatorList.stream().filter(t -> t.getCompletion() == 1).count();
                isCountersign = this.isCountersign(pass, total, passNum);
            }
            //????????????
            if (isCountersign) {
                //?????????????????????????????????
                if (fixed) {
                    flowTaskOperatorService.update(nodeModel.getTaskNodeId(), type);
                }
                taskNode.setCompletion(1);
                //??????????????????
                flowTaskNodeService.update(taskNode);
                //????????????
                boolean isShunt = this.isShunt(nodeListAll, nextNodeEntity, taskNode);
                if(isShunt){
                    result.addAll(nextNodeEntity);
                }
            }
        }
        return result;
    }

    /**
     * ????????????
     *
     * @param pass    ??????
     * @param total   ??????
     * @param passNum ??????
     * @return
     */
    private boolean isCountersign(long pass, double total, double passNum) {
        int scale = (int) (passNum / total * 100);
        return scale >= pass;
    }


    /**
     * ????????????????????????
     *
     * @param nodeListAll    ????????????
     * @param nextNodeEntity ????????????
     * @param taskNode       ????????????
     * @return
     */
    private boolean isShunt(List<FlowTaskNodeEntity> nodeListAll, List<FlowTaskNodeEntity> nextNodeEntity, FlowTaskNodeEntity taskNode) {
        boolean isNext = true;
        for (FlowTaskNodeEntity nodeEntity : nextNodeEntity) {
            String nextNode = nodeEntity.getNodeCode();
            List<FlowTaskNodeEntity> interflowAll = nodeListAll.stream().filter(t -> String.valueOf(t.getNodeNext()).contains(nextNode) &&  FlowNodeEnum.Process.getCode().equals(t.getState())).collect(Collectors.toList());
            List<FlowTaskNodeEntity> flowAll = interflowAll.stream().filter(t -> t.getCompletion() != 1).collect(Collectors.toList());
            if (flowAll.size() > 0) {
                isNext = false;
                break;
            }
        }
        return isNext;
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param userPosition  ???????????????????????????
     * @param circulateList ???????????????
     * @param nodeModel     ????????????
     */
    private void getCirculateUser(List<String> userPosition, List<FlowTaskCirculateEntity> circulateList, ChildNodeList nodeModel) {
        for (String user : userPosition) {
            //??????????????????????????????
            if (circulateList.stream().filter(t -> t.getObjectId().equals(user)).count() == 0) {
                FlowTaskCirculateEntity flowTask = new FlowTaskCirculateEntity();
                flowTask.setId(RandomUtil.uuId());
                flowTask.setObjectId(user);
                flowTask.setNodeCode(nodeModel.getCustom().getNodeId());
                flowTask.setNodeName(nodeModel.getProperties().getTitle());
                flowTask.setTaskNodeId(nodeModel.getTaskNodeId());
                flowTask.setTaskId(nodeModel.getTaskId());
                flowTask.setCreatorTime(new Date());
                circulateList.add(flowTask);
            }
        }
    }

    /**
     * ?????????
     *
     * @param nodeModel     ??????json??????
     * @param circulateList ??????list
     * @param flowModel     ????????????
     */
    private void circulateList(ChildNodeList nodeModel, List<FlowTaskCirculateEntity> circulateList, FlowModel flowModel) {
        Properties circleproperties = nodeModel.getProperties();
        Custom circlecustom = nodeModel.getCustom();
        String circletaskId = nodeModel.getTaskId();
        String circletaskNodeId = nodeModel.getTaskNodeId();
        //??????????????????????????????
        for (String userId : circleproperties.getCirculateUser()) {
            FlowTaskCirculateEntity flowTask = new FlowTaskCirculateEntity();
            flowTask.setId(RandomUtil.uuId());
            flowTask.setObjectId(userId);
            flowTask.setNodeCode(circlecustom.getNodeId());
            flowTask.setNodeName(circleproperties.getTitle());
            flowTask.setTaskNodeId(circletaskNodeId);
            flowTask.setTaskId(circletaskId);
            flowTask.setCreatorTime(new Date());
            circulateList.add(flowTask);
        }
        //???????????????????????????
        List<String> roleList = circleproperties.getCirculateRole();
        //???????????????????????????
        List<String> posList = circleproperties.getCirculatePosition();
        List<String> userAll = new ArrayList<>();
        userAll.addAll(roleList);
        userAll.addAll(posList);
        List<UserRelationEntity> listByObjectIdAll = userRelationService.getListByObjectIdAll(userAll);
        List<String> userPosition = listByObjectIdAll.stream().map(t -> t.getUserId()).collect(Collectors.toList());
        //???????????????
        String[] copyIds = StringUtil.isNotEmpty(flowModel.getCopyIds()) ? flowModel.getCopyIds().split(",") : new String[]{};
        List<String> id = Arrays.asList(copyIds);
        userPosition.addAll(id);
        this.getCirculateUser(userPosition, circulateList, nodeModel);
    }

    /**
     * ??????????????????
     *
     * @param flowTask ????????????
     */
    private boolean endround(FlowTaskEntity flowTask, ChildNodeList childNode) throws WorkFlowException {
        flowTask.setStatus(FlowTaskStatusEnum.Adopt.getCode());
        flowTask.setCompletion(100);
        flowTask.setEndTime(DateUtil.getNowDate());
        flowTask.setThisStepId(FlowNature.NodeEnd);
        flowTask.setThisStep("??????");
        //????????????
        FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
        operatorRecord.setTaskId(flowTask.getId());
        operatorRecord.setHandleStatus(flowTask.getStatus());
        this.event(FlowRecordEnum.end.getCode(), childNode, operatorRecord);
        //?????????????????????????????????
        boolean isEnd = this.isNext(flowTask);
        return isEnd;
    }

    //---------------??????-------------------

    /**
     * ??????????????????
     *
     * @param nodeListAll ????????????
     * @param taskNode    ????????????
     * @return
     */
    private List<FlowTaskNodeEntity> isUpAll(List<FlowTaskNodeEntity> nodeListAll, FlowTaskNodeEntity taskNode, boolean isUp) throws WorkFlowException {
        List<FlowTaskNodeEntity> result = new ArrayList<>();
        if (isUp) {
            if (FlowNature.START.equals(taskNode.getNodeUp())) {
                List<FlowTaskNodeEntity> startNode = nodeListAll.stream().filter(t -> FlowNature.NodeStart.equals(t.getNodeType())).collect(Collectors.toList());
                result.addAll(startNode);
            } else {
                List<FlowTaskNodeEntity> taskNodeList = nodeListAll.stream().filter(t -> t.getNodeCode().equals(taskNode.getNodeUp())).collect(Collectors.toList());
                if (taskNodeList.size() == 0) {
                    taskNodeList = nodeListAll.stream().filter(t -> t.getNodeNext() != null && t.getNodeNext().contains(taskNode.getNodeCode()) && "0".equals(t.getState())).collect(Collectors.toList());
                }
                result.addAll(taskNodeList);
            }
            boolean isChild = result.stream().filter(t -> FlowNature.NodeSubFlow.equals(t.getNodeType())).count() > 0;
            if (isChild) {
                throw new WorkFlowException("??????????????????????????????");
            }
        }
        return result;
    }

    /**
     * ????????????
     *
     * @param taskNode ????????????
     * @return
     */
    private boolean isReject(FlowTaskNodeEntity taskNode) {
        List<FlowTaskOperatorEntity> operatorList = flowTaskOperatorService.getList(taskNode.getTaskId()).stream().filter(t -> t.getTaskNodeId().equals(taskNode.getId())).collect(Collectors.toList());
        ChildNodeList nodeModel = JsonUtil.getJsonToBean(taskNode.getNodePropertyJson(), ChildNodeList.class);
        Properties properties = nodeModel.getProperties();
        long pass = 100 - properties.getCountersignRatio();
        double total = operatorList.size();
        double passNum = operatorList.stream().filter(t -> t.getCompletion() == -1).count();
        boolean isCountersign = this.isCountersign(pass, total, passNum);
        return isCountersign;
    }

    //-----------------------?????????---------------------------------

    /**
     * ????????????
     *
     * @param engine    ??????
     * @param flowModel ????????????
     */
    private Map<String, Object> createData(FlowEngineEntity engine, FlowTaskEntity taskEntity, FlowModel flowModel) throws WorkFlowException {
        Map<String, Object> resultData = new HashMap<>(16);
        try {
            Map<String, Object> data = flowModel.getFormData();
            if (FlowNature.CUSTOM.equals(engine.getFormType())) {
                List<FlowTableModel> tableList = JsonUtil.getJsonToList(engine.getTables(), FlowTableModel.class);
                //????????????
                DbLinkEntity dbLink = null;
                if (StringUtil.isNotEmpty(engine.getDbLinkId())) {
                    dbLink = dblinkService.getInfo(engine.getDbLinkId());
                }
                FormDataModel formData = JsonUtil.getJsonToBean(taskEntity.getFlowForm(), FormDataModel.class);
                List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
                if (StringUtil.isNotEmpty(flowModel.getId())) {
                    //??????
                    resultData = flowDataUtil.update(data, list, tableList, taskEntity.getProcessId(), dbLink);
                } else {
                    //??????
                    resultData = flowDataUtil.create(data, list, tableList, taskEntity.getProcessId(), new HashMap<>(16), dbLink);
                }
            } else {
                //????????????
                String dataAll = JsonUtil.getObjectToString(data);
                if (engine.getType() != 1) {
                    String coed = engine.getEnCode();
                    this.formData(coed, flowModel.getProcessId(), dataAll);
                }
            }
        } catch (Exception e) {
            throw new WorkFlowException("??????????????????");
        }
        return resultData;
    }

    /**
     * ?????????????????????????????????????????????????????????
     *
     * @param flowTask ???????????????
     * @throws WorkFlowException
     */
    private boolean isNext(FlowTaskEntity flowTask) throws WorkFlowException {
        boolean isEnd = true;
        //?????????????????????????????????
        if (!FlowNature.ParentId.equals(flowTask.getParentId()) && StringUtil.isNotEmpty(flowTask.getParentId())) {
            isEnd = false;
            List<FlowTaskEntity> parentList = flowTaskService.getChildList(flowTask.getParentId());
            //??????????????????????????????????????????????????????????????????
            boolean isNext = parentList.stream().filter(t -> !t.getId().equals(flowTask.getId()) && !FlowTaskStatusEnum.Adopt.getCode().equals(flowTask.getStatus())).count() == 0;
            if (isNext) {
                FlowTaskEntity parent = flowTaskService.getInfo(flowTask.getParentId());
                List<FlowTaskNodeEntity> parentNodeAll = flowTaskNodeService.getList(parent.getId());
                FlowTaskOperatorEntity parentOperator = new FlowTaskOperatorEntity();
                boolean isNode = true;
                for (FlowTaskNodeEntity nodeEntity : parentNodeAll) {
                    ChildNodeList parentNode = JsonUtil.getJsonToBean(nodeEntity.getNodePropertyJson(), ChildNodeList.class);
                    isNode = parentNode.getCustom().getTaskId().stream().filter(t -> flowTask.getId().equals(t)).count() > 0;
                    if (isNode) {
                        parentOperator.setTaskNodeId(nodeEntity.getId());
                        parentOperator.setDescription(JsonUtil.getObjectToString(new ArrayList<>()));
                        parentOperator.setNodeCode(nodeEntity.getNodeCode());
                        parentOperator.setNodeName(nodeEntity.getNodeName());
                        parentOperator.setTaskId(nodeEntity.getTaskId());
                        parentOperator.setCompletion(0);
                        break;
                    }
                }
                FlowModel parentModel = new FlowModel();
                parentModel.setUserId("");
                Map<String, Object> data = new HashMap<>(16);
                parentModel.setFormData(data);
                if (isNode) {
                    this.audit(parent, parentOperator, parentModel);
                }
            }
        }
        return isEnd;
    }

    /**
     * ??????????????????
     *
     * @param childNode
     * @param taskEntity
     * @return
     */
    private List<String> childSaveList(ChildNodeList childNode, FlowTaskEntity taskEntity, List<UserEntity> userList) {
        String createUserId = taskEntity.getCreatorUserId();
        Properties properties = childNode.getProperties();
        String type = properties.getInitiateType();
        List<FlowTaskOperatorEntity> nextList = new ArrayList<>();
        String userId = "";
        Date date = new Date();
        //?????????????????????
        if (FlowTaskOperatorEnum.ChildDepartmentCharge.getCode().equals(type)) {
            UserEntity userEntity = userService.getInfo(createUserId);
            OrganizeEntity organizeEntity = organizeService.getInfo(userEntity != null ? userEntity.getOrganizeId() : "");
            if (organizeEntity != null) {
                userId = organizeEntity.getManager();
                this.operatorUser(nextList, userId, date, childNode);
            }
        }
        //????????????????????????
        if (FlowTaskOperatorEnum.ChildLaunchCharge.getCode().equals(type)) {
            //?????????????????????
            UserEntity info = userService.getInfo(createUserId);
            if (info != null) {
                userId = getManagerByLevel(info.getManagerId(), properties.getManagerLevel(), userList);
                this.operatorUser(nextList, userId, date, childNode);
            }
        }
        //????????????????????????
        if (FlowTaskOperatorEnum.ChildInitiatorMe.getCode().equals(type)) {
            this.operatorUser(nextList, createUserId, date, childNode);
        }
        //???????????????????????????
        for (String userIdAll : properties.getInitiator()) {
            this.operatorUser(nextList, userIdAll, date, childNode);
        }
        //???????????????????????????
        List<String> positionList = properties.getInitiatePos();
        //???????????????????????????
        List<String> roleList = properties.getInitiateRole();
        List<String> list = new ArrayList<>();
        list.addAll(positionList);
        list.addAll(roleList);
        List<UserRelationEntity> listByObjectIdAll = userRelationService.getListByObjectIdAll(list);
        List<String> userPosition = listByObjectIdAll.stream().map(t -> t.getUserId()).collect(Collectors.toList());
        this.getApproverUser(userPosition, nextList, childNode);
        List<String> result = nextList.stream().map(t -> t.getHandleId()).collect(Collectors.toList());
        if (result.size() == 0) {
            result.add(user);
        }
        return result;
    }

    /**
     * ??????
     *
     * @param data     ??????
     * @param engine   ??????
     * @param parentId ????????????
     * @return
     */
    private FlowModel assignment(Map<String, Object> data, FlowEngineEntity engine, String parentId, String title) {
        FlowModel flowModel = new FlowModel();
        String billNo = "?????????????????????";
        if (FlowNature.CUSTOM.equals(engine.getFormType())) {
            FormDataModel formData = JsonUtil.getJsonToBean(engine.getFormData(), FormDataModel.class);
            List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
            List<FormAllModel> formAllModel = new ArrayList<>();
            FormCloumnUtil.recursionForm(list, formAllModel);
            List<FormAllModel> mastForm = formAllModel.stream().filter(t -> FormEnum.mast.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
            FormAllModel formModel = mastForm.stream().filter(t -> JnpfKeyConsts.BILLRULE.equals(t.getFormColumnModel().getFieLdsModel().getConfig().getJnpfKey())).findFirst().orElse(null);
            try {
                if (flowModel != null) {
                    FieLdsModel fieLdsModel = formModel.getFormColumnModel().getFieLdsModel();
                    String ruleKey = fieLdsModel.getConfig().getRule();
                    billNo = billRuleService.getBillNumber(ruleKey, false);
                }
            } catch (Exception e) {

            }
        }
        flowModel.setFormData(data);
        flowModel.setParentId(parentId);
        flowModel.setProcessId(RandomUtil.uuId());
        flowModel.setBillNo(billNo);
        flowModel.setFlowTitle(title);
        return flowModel;
    }

    /**
     * ??????????????????
     *
     * @param engine     ????????????
     * @param flowModel  ????????????
     * @param assignList ????????????
     * @param isCustom   true??????????????? false????????????
     * @return
     */
    private Map<String, Object> childData(FlowEngineEntity engine, FlowModel flowModel, List<FlowAssignModel> assignList, boolean isCustom) {
        Map<String, Object> result = new HashMap<>(16);
        if (engine != null) {
            Map<String, Object> formData = flowModel.getFormData();
            for (FlowAssignModel assignMode : assignList) {
                String childField = assignMode.getChildField();
                String parentField = assignMode.getParentField();
                result.put(childField, formData.get(parentField));
            }
        }
        return result;
    }

    /**
     * ???????????????????????????
     *
     * @param task ?????????????????????
     */
    private void delChild(FlowTaskEntity task) {
        List<FlowTaskEntity> childTaskList = flowTaskService.getChildList(task.getId());
        for (FlowTaskEntity flowTask : childTaskList) {
            //???????????????
            flowTaskService.deleteChild(flowTask);
            this.delChild(flowTask);
        }
    }

    //---------------------????????????--------------------------

    /**
     * ????????????
     *
     * @param status    ????????????
     * @param childNode ????????????
     * @param record    ????????????
     */
    private void event(Integer status, ChildNodeList childNode, FlowTaskOperatorRecordEntity record) {
        boolean flag = false;
        String faceUrl = "";
        //??????
        if (childNode != null) {
            Properties properties = childNode.getProperties();
            if (FlowRecordEnum.audit.getCode().equals(status) || FlowRecordEnum.reject.getCode().equals(status)) {
                flag = properties.getHasApproverfunc() != null ? properties.getHasApproverfunc() : false;
                faceUrl = properties.getApproverInterfaceUrl() + "?" + taskNodeId + "=" + record.getTaskNodeId() + "&" +
                        handleStatus + "=" + record.getHandleStatus() + "&" + taskId + "=" + record.getTaskId();
                System.out.println("??????????????????:" + faceUrl);
            } else if (FlowRecordEnum.submit.getCode().equals(status)) {
                flag = properties.getHasInitfunc() != null ? properties.getHasInitfunc() : false;
                faceUrl = properties.getInitInterfaceUrl() + "?" + taskNodeId + "=" + record.getTaskNodeId() + "&" + taskId + "=" + record.getTaskId();
                System.out.println("??????????????????:" + faceUrl);
            } else if (FlowRecordEnum.revoke.getCode().equals(status)) {
                flag = properties.getHasFlowRecallFunc() != null ? properties.getHasFlowRecallFunc() : false;
                faceUrl = properties.getFlowRecallInterfaceUrl() + "?" + handleStatus + "=" + record.getHandleStatus()
                        + "&" + taskId + "=" + record.getTaskId();
                System.out.println("??????????????????:" + faceUrl);
            } else if (FlowRecordEnum.end.getCode().equals(status)) {
                flag = properties.getHasEndfunc() != null ? properties.getHasEndfunc() : false;
                faceUrl = properties.getEndInterfaceUrl() + "?" + taskNodeId + "=" + record.getTaskNodeId() + "&" +
                        handleStatus + "=" + record.getHandleStatus() + "&" + taskId + "=" + record.getTaskId();
                System.out.println("??????????????????:" + faceUrl);
            } else if (FlowRecordEnum.recall.getCode().equals(status)) {
                flag = properties.getHasRecallFunc() != null ? properties.getHasRecallFunc() : false;
                faceUrl = properties.getRecallInterfaceUrl() + "?" + taskNodeId + "=" + record.getTaskNodeId() + "&" +
                        handleStatus + "=" + record.getHandleStatus() + "&" + taskId + "=" + record.getTaskId();
                System.out.println("??????????????????:" + faceUrl);
            }
        }
        if (flag) {
            String token = UserProvider.getToken();
            HttpUtil.httpRequest(faceUrl, "GET", null, token);
        }
    }


    /**
     * ????????????
     *
     * @param flowMsgModel
     */
    private void message(FlowMsgModel flowMsgModel) {
        List<SentMessageForm> messageList = new ArrayList<>();
        List<FlowTaskNodeEntity> nodeList = flowMsgModel.getNodeList();
        List<FlowTaskOperatorEntity> operatorList = flowMsgModel.getOperatorList();
        List<FlowTaskCirculateEntity> circulateList = flowMsgModel.getCirculateList();
        FlowTaskEntity taskEntity = flowMsgModel.getTaskEntity();
        String msgTitle = flowMsgModel.getMsgTitel();
        //?????????
        for (FlowTaskOperatorEntity operator : operatorList) {
            FlowTaskNodeEntity node = nodeList.stream().filter(t -> t.getId().equals(operator.getTaskNodeId())).findFirst().orElse(null);
            if (node != null) {
                List<String> userList = operatorList.stream().map(t -> t.getHandleId()).collect(Collectors.toList());
                ChildNodeList childNode = JsonUtil.getJsonToBean(node.getNodePropertyJson(), ChildNodeList.class);
                Properties properties = childNode.getProperties();
                List<String> messageType = properties.getMessageType() != null ? properties.getMessageType() : Arrays.asList(new String[]{});
                SentMessageForm sentMessageForm = this.message("????????????", FlowMessageEnum.wait.getCode(), messageType, taskEntity, userList);
                messageList.add(sentMessageForm);
            }
        }
        //?????????
        for (FlowTaskCirculateEntity circulate : circulateList) {
            FlowTaskNodeEntity node = nodeList.stream().filter(t -> t.getId().equals(circulate.getTaskNodeId())).findFirst().orElse(null);
            if (node != null) {
                List<String> userList = circulateList.stream().map(t -> t.getObjectId()).collect(Collectors.toList());
                ChildNodeList childNode = JsonUtil.getJsonToBean(node.getNodePropertyJson(), ChildNodeList.class);
                Properties properties = childNode.getProperties();
                List<String> messageType = properties.getMessageType() != null ? properties.getMessageType() : Arrays.asList(new String[]{});
                SentMessageForm sentMessageForm = this.message("????????????", FlowMessageEnum.circulate.getCode(), messageType, taskEntity, userList);
                messageList.add(sentMessageForm);
            }
        }
        //???????????????
        if (StringUtil.isNotEmpty(msgTitle)) {
            List<String> userList = new ArrayList<>();
            userList.add(taskEntity.getCreatorUserId());
            List<String> messageType = new ArrayList<>();
            messageType.add(MessageTypeEnum.SysMessage.getCode());
            SentMessageForm sentMessageForm = this.message(msgTitle, FlowMessageEnum.me.getCode(), messageType, taskEntity, userList);
            messageList.add(sentMessageForm);
        }
        for (SentMessageForm messageForm : messageList) {
            sentMessageUtil.sendMessage(messageForm);
        }
    }

    /**
     * ??????????????????
     *
     * @param title       ??????
     * @param type        ??????
     * @param messageType ??????????????????
     * @param taskEntity  ????????????
     * @param userList    ????????????
     * @return
     */
    private SentMessageForm message(String title, int type, List<String> messageType, FlowTaskEntity taskEntity, List<String> userList) {
        SentMessageForm sentMessageForm = new SentMessageForm();
        sentMessageForm.setSendType(messageType);
        Map<String, Object> message = new HashMap<>(16);
        message.put("type", type);
        message.put("id", taskEntity.getId());
        sentMessageForm.setContent(JsonUtilEx.getObjectToString(message));
        sentMessageForm.setTitle(taskEntity.getFullName() + title);
        sentMessageForm.setToUserIds(userList);
        Map<String, String> smsContent = new HashMap<>(16);
        this.message(taskEntity.getFullName(), title, smsContent);
        sentMessageForm.setSmsContent(smsContent);
        return sentMessageForm;
    }

    /**
     * ??????????????????
     *
     * @param nextOperatorList ??????????????????
     * @param flowTaskNodeList ????????????
     * @param flowTask         ????????????
     */
    private boolean getNextStepId(List<ChildNodeList> nextOperatorList, List<FlowTaskNodeEntity> flowTaskNodeList, FlowTaskEntity flowTask) throws WorkFlowException {
        boolean isEnd = false;
        Set<String> delNodeList = new HashSet<>();
        List<String> progressList = new ArrayList<>();
        List<String> nextOperator = new ArrayList<>();
        ChildNodeList end = nextOperatorList.stream().filter(t -> t.getCustom().getNodeId().contains(FlowNature.NodeEnd)).findFirst().orElse(null);
        for (ChildNodeList childNode : nextOperatorList) {
            Properties properties = childNode.getProperties();
            String id = childNode.getCustom().getNodeId();
            String progress = properties.getProgress();
            List<FlowTaskNodeEntity> taskNodeList = flowTaskNodeList.stream().filter(t -> t.getNodeNext() != null).filter(t -> t.getNodeNext().contains(id)).collect(Collectors.toList());
            List<String> nodeList = taskNodeList.stream().map(t -> t.getNodeCode()).collect(Collectors.toList());
            nextOperatorList.stream().filter(t -> t.getProperties().getProgress() != null).map(t -> t.getProperties().getProgress()).collect(Collectors.toList());
            delNodeList.addAll(nodeList);
            nextOperator.add(id);
            if (StringUtil.isNotEmpty(progress)) {
                progressList.add(progress);
            }
        }
        String[] thisNode = flowTask.getThisStepId() != null ? flowTask.getThisStepId().split(",") : new String[]{};
        Set<String> thisStepId = new HashSet<>();
        for (String id : thisNode) {
            boolean isStepId = flowTaskNodeList.stream().filter(t -> t.getNodeCode().equals(id) && t.getCompletion() == 0).count() > 0;
            if (isStepId) {
                thisStepId.add(id);
            }
        }
        thisStepId.removeAll(delNodeList);
        thisStepId.addAll(nextOperator);
        List<String> thisNodeName = new ArrayList<>();
        for (String id : thisStepId) {
            List<String> nodeList = flowTaskNodeList.stream().filter(t -> t.getNodeCode().equals(id)).map(t -> t.getNodeName()).collect(Collectors.toList());
            thisNodeName.addAll(nodeList);
        }
        flowTask.setThisStepId(String.join(",", thisStepId));
        flowTask.setThisStep(String.join(",", thisNodeName));
        Collections.sort(progressList);
        flowTask.setCompletion(progressList.size() > 0 ? Integer.valueOf(progressList.get(0)) : null);
        if (end != null) {
            isEnd = this.endround(flowTask, end);
        }
        return isEnd;
    }

    /**
     * ????????????
     *
     * @param record         ????????????
     * @param operatordModel ????????????
     */
    private void operatorRecord(FlowTaskOperatorRecordEntity record, FlowOperatordModel operatordModel) {
        int status = operatordModel.getStatus();
        FlowModel flowModel = operatordModel.getFlowModel();
        String userId = operatordModel.getUserId();
        FlowTaskOperatorEntity operator = operatordModel.getOperator();
        String operatorId = operatordModel.getOperatorId();
        record.setHandleOpinion(flowModel.getHandleOpinion());
        record.setHandleId(userId);
        record.setHandleTime(new Date());
        record.setHandleStatus(status);
        record.setOperatorId(operatorId);
        record.setNodeCode(operator.getNodeCode());
        record.setNodeName(operator.getNodeName() != null ? operator.getNodeName() : "??????");
        record.setTaskOperatorId(operator.getId());
        record.setTaskNodeId(operator.getTaskNodeId());
        record.setTaskId(operator.getTaskId());
        record.setSignImg(flowModel.getSignImg());
        record.setStatus(FlowTaskOperatorEnum.FreeApprover.getCode().equals(operator.getHandleType()) ? FlowNodeEnum.Futility.getCode() : FlowNodeEnum.Process.getCode());
    }

    /**
     * ????????????????????????
     *
     * @param code ??????
     * @param id   ??????id
     * @param data ??????
     * @throws WorkFlowException
     */
    private void formData(String code, String id, String data) throws WorkFlowException {
        Map<String, Object> objectData = JsonUtil.stringToMap(data);
        if (objectData.size() > 0) {
            try {
                Class[] types = new Class[]{String.class, String.class};
                Object[] datas = new Object[]{id, data};
                Object service = SpringContext.getBean(code + "ServiceImpl");
                ReflectionUtil.invokeMethod(service, "data", types, datas);
            } catch (Exception e) {
                throw new WorkFlowException("????????????????????????");
            }
        }
    }

    /**
     * ?????????
     *
     * @param taskOperator ????????????
     * @param taskNodeList ??????????????????
     * @param operatorList ??????????????????
     * @return
     */
    private List<FlowTaskOperatorEntity> timer(FlowTaskOperatorEntity taskOperator, List<FlowTaskNodeEntity> taskNodeList, List<FlowTaskOperatorEntity> operatorList) {
        List<FlowTaskOperatorEntity> operatorListAll = new ArrayList<>();
        FlowTaskNodeEntity taskNode = taskNodeList.stream().filter(t -> t.getId().equals(taskOperator.getTaskNodeId())).findFirst().orElse(null);
        if (taskNode != null) {
            //??????????????????????????????
            List<String> nodeList = taskNodeList.stream().filter(t -> t.getSortCode().equals(taskNode.getSortCode())).map(t -> t.getId()).collect(Collectors.toList());
            List<FlowTaskOperatorEntity> operatorAll = flowTaskOperatorService.getList(taskOperator.getTaskId());
            Set<Date> dateListAll = new HashSet<>();
            List<FlowTaskOperatorEntity> list = operatorAll.stream().filter(t -> nodeList.contains(t.getTaskNodeId())).collect(Collectors.toList());
            for (FlowTaskOperatorEntity operator : list) {
                List<Date> dateList = JsonUtil.getJsonToList(operator.getDescription(), Date.class);
                dateListAll.addAll(dateList);
            }
            //???????????????????????????
            List<Date> date = JsonUtil.getJsonToList(taskOperator.getDescription(), Date.class);
            dateListAll.addAll(date);
            for (FlowTaskOperatorEntity operator : operatorList) {
                operator.setDescription(JsonUtil.getObjectToString(dateListAll));
                operatorListAll.add(operator);
            }
        }
        return operatorListAll;
    }

    /**
     * ????????????
     *
     * @param title  ??????
     * @param status ??????
     * @return
     */
    private void message(String title, String status, Map<String, String> msg) {
        Map<String, String> aliyun = new HashMap<>(16);
        aliyun.put("title", title);
        aliyun.put("status", status);
        msg.put("1", JsonUtil.getObjectToString(aliyun));
        msg.put("2", "2");
    }

}

