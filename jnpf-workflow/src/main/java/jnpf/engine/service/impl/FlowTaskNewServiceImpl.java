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
 * 流程引擎
 *
 * @author JNPF开发平台组
 * @version V3.2.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年4月27日 上午9:18
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
     * 节点id
     **/
    private String taskNodeId = "taskNodeId";
    /**
     * 状态
     **/
    private String handleStatus = "handleStatus";
    /**
     * 任务id
     **/
    private String taskId = "taskId";
    /**
     * 空节点默认审批人
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
        //流程引擎
        FlowEngineEntity engine = flowEngineService.getInfo(flowId);
        boolean flag = flowModel.getId() == null;
        //流程实例
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
        //更新流程任务
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
        //流程实例
        FlowTaskEntity flowTask = saveIsAdmin(flowModel);
        try {
            //流程节点
            List<FlowTaskNodeEntity> taskNodeList = new LinkedList<>();
            //流程经办
            List<FlowTaskOperatorEntity> operatorList = new ArrayList<>();
            //流程表单Json
            String formDataJson = flowTask.getFlowTemplateJson();
            ChildNode childNodeAll = JsonUtil.getJsonToBean(formDataJson, ChildNode.class);
            //获取流程节点
            List<ChildNodeList> nodeListAll = new ArrayList<>();
            List<ConditionList> conditionListAll = new ArrayList<>();
            //递归获取条件数据和节点数据
            FlowJsonUtil.getTemplateAll(childNodeAll, nodeListAll, conditionListAll);
            //创建节点
            this.createNodeList(flowTask, nodeListAll, conditionListAll, taskNodeList);
            //保存节点
            this.nodeListAll(taskNodeList);
            //获取下一个节点
            FlowTaskNodeEntity startNode = taskNodeList.stream().filter(t -> FlowNature.NodeStart.equals(t.getNodeType())).findFirst().get();
            List<String> nodeList = Arrays.asList(startNode.getNodeNext().split(","));
            //获取下一审批人
            List<ChildNodeList> nextOperatorList = nodeListAll.stream().filter(t -> nodeList.contains(t.getCustom().getNodeId())).collect(Collectors.toList());
            Map<String, List<String>> nodeIdAll = this.nextOperator(operatorList, nextOperatorList, flowTask, flowModel);
            //审核人
            flowTaskOperatorService.create(operatorList);
            //更新关联子流程id
            for (String nodeId : nodeIdAll.keySet()) {
                FlowTaskNodeEntity entity = taskNodeList.stream().filter(t -> t.getId().equals(nodeId)).findFirst().orElse(null);
                if (entity != null) {
                    ChildNodeList childNodeList = JsonUtil.getJsonToBean(entity.getNodePropertyJson(), ChildNodeList.class);
                    childNodeList.getCustom().setTaskId(nodeIdAll.get(nodeId));
                    entity.setNodePropertyJson(JsonUtil.getObjectToString(childNodeList));
                    flowTaskNodeService.update(entity);
                }
            }
            //提交记录
            ChildNodeList start = JsonUtil.getJsonToBean(startNode.getNodePropertyJson(), ChildNodeList.class);
            FlowTaskOperatorEntity operator = new FlowTaskOperatorEntity();
            operator.setTaskId(flowTask.getId());
            operator.setNodeCode(start.getCustom().getNodeId());
            FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
            //审批数据赋值
            FlowOperatordModel flowOperatordModel = new FlowOperatordModel();
            flowOperatordModel.setStatus(FlowRecordEnum.submit.getCode());
            flowOperatordModel.setFlowModel(flowModel);
            flowOperatordModel.setUserId(userInfo.getUserId());
            flowOperatordModel.setOperator(operator);
            this.operatorRecord(operatorRecord, flowOperatordModel);
            flowTaskOperatorRecordService.create(operatorRecord);
            //定时器
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
            //开始事件
            this.event(FlowRecordEnum.submit.getCode(), start, operatorRecord);
            //更新流程节点
            boolean isEnd = this.getNextStepId(nextOperatorList, taskNodeList, flowTask);
            if (FlowNature.NodeEnd.equals(startNode.getNodeNext())) {
                this.endround(flowTask, nodeListAll.get(0));
            }
            flowTaskService.updateById(flowTask);
            //发送消息
            List<FlowTaskCirculateEntity> circulateList = new ArrayList<>();
            FlowMsgModel flowMsgModel = new FlowMsgModel();
            flowMsgModel.setCirculateList(circulateList);
            flowMsgModel.setMsgTitel(isEnd ? "【审核通过】" : null);
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
            throw new WorkFlowException("审批已完成");
        }
        try {
            UserInfo userInfo = userProvider.get();
            String userId = StringUtil.isNotEmpty(flowModel.getUserId()) ? flowModel.getUserId() : userInfo.getUserId();
            //流程所有节点
            List<FlowTaskNodeEntity> flowTaskNodeAll = flowTaskNodeService.getList(flowTask.getId());
            List<FlowTaskNodeEntity> taskNodeList = flowTaskNodeAll.stream().filter(t -> FlowNodeEnum.Process.getCode().equals(t.getState())).collect(Collectors.toList());
            //当前节点
            FlowTaskNodeEntity taskNode = taskNodeList.stream().filter(m -> m.getId().equals(operator.getTaskNodeId())).findFirst().get();
            //当前节点属性
            ChildNodeList nodeModel = JsonUtil.getJsonToBean(taskNode.getNodePropertyJson(), ChildNodeList.class);
            //同意记录
            FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
            //审批数据赋值
            FlowOperatordModel flowOperatordModel = new FlowOperatordModel();
            flowOperatordModel.setStatus(FlowRecordEnum.audit.getCode());
            flowOperatordModel.setFlowModel(flowModel);
            flowOperatordModel.setUserId(userId);
            flowOperatordModel.setOperator(operator);
            this.operatorRecord(operatorRecord, flowOperatordModel);
            flowTaskOperatorRecordService.create(operatorRecord);
            //修改或签、会签经办数据
            this.handleIdStatus(1, nodeModel, operator, userInfo, taskNodeList);
            //节点事件
            this.event(FlowRecordEnum.audit.getCode(), nodeModel, operatorRecord);
            //更新流当前程经办状态
            if(StringUtil.isNotEmpty(operator.getId())) {
                flowTaskOperatorService.update(operator);
            }
            //更新下一节点
            List<FlowTaskOperatorEntity> operatorList = new ArrayList<>();
            //获取下一审批人
            List<FlowTaskNodeEntity> nextNode = taskNodeList.stream().filter(t -> taskNode.getNodeNext().contains(t.getNodeCode())).collect(Collectors.toList());
            List<ChildNodeList> nextOperatorList = new ArrayList<>();
            List<FlowTaskNodeEntity> result = this.isNextAll(taskNodeList, nextNode, taskNode, flowModel);
            for (FlowTaskNodeEntity entity : result) {
                ChildNodeList node = JsonUtil.getJsonToBean(entity.getNodePropertyJson(), ChildNodeList.class);
                nextOperatorList.add(node);
            }
            //更新数据
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
            //更新流程节点
            boolean isEnd = this.getNextStepId(nextOperatorList, taskNodeList, flowTask);
            flowTask.setFlowFormContentJson(JsonUtil.getObjectToString(data));
            flowTaskService.updateById(flowTask);
            //下个节点
            Map<String, List<String>> nodeIdAll = this.nextOperator(operatorList, nextOperatorList, flowTask, flowModel);
            flowTaskOperatorService.create(operatorList);
            //更新关联子流程id
            for (String nodeId : nodeIdAll.keySet()) {
                FlowTaskNodeEntity entity = flowTaskNodeService.getInfo(nodeId);
                if (entity != null) {
                    ChildNodeList childNodeList = JsonUtil.getJsonToBean(entity.getNodePropertyJson(), ChildNodeList.class);
                    childNodeList.getCustom().setTaskId(nodeIdAll.get(nodeId));
                    entity.setNodePropertyJson(JsonUtil.getObjectToString(childNodeList));
                    flowTaskNodeService.update(entity);
                }
            }
            //定时器
            List<FlowTaskOperatorEntity> operatorAll = this.timer(operator, taskNodeList, operatorList);
            for (FlowTaskOperatorEntity operatorTime : operatorAll) {
                List<Date> dateAll = JsonUtil.getJsonToList(operatorTime.getDescription(), Date.class);
                if (dateAll.size() > 0) {
                    Date max = Collections.max(dateAll);
                    operatorTime.setCreatorTime(max);
                }
                flowTaskOperatorService.update(operatorTime);
            }
            //获取抄送人
            List<FlowTaskCirculateEntity> circulateList = new ArrayList<>();
            this.circulateList(nodeModel, circulateList, flowModel);
            flowTaskCirculateService.create(circulateList);
            //发送消息
            FlowMsgModel flowMsgModel = new FlowMsgModel();
            flowMsgModel.setCirculateList(circulateList);
            flowMsgModel.setNodeList(taskNodeList);
            flowMsgModel.setMsgTitel(isEnd ? "【审核通过】" : "【审核同意】");
            flowMsgModel.setOperatorList(operatorList);
            flowMsgModel.setTaskEntity(flowTask);
            this.message(flowMsgModel);
        } catch (WorkFlowException e) {
            //手动回滚事务
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
        //流程所有节点
        List<FlowTaskNodeEntity> flowTaskNodeAll = flowTaskNodeService.getList(flowTask.getId());
        List<FlowTaskNodeEntity> taskNodeList = flowTaskNodeAll.stream().filter(t -> FlowNodeEnum.Process.getCode().equals(t.getState())).collect(Collectors.toList());
        //当前节点
        FlowTaskNodeEntity taskNode = taskNodeList.stream().filter(m -> m.getId().equals(operator.getTaskNodeId())).findFirst().get();
        //当前节点属性
        ChildNodeList nodeModel = JsonUtil.getJsonToBean(taskNode.getNodePropertyJson(), ChildNodeList.class);
        //驳回记录
        FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
        //审批数据赋值
        FlowOperatordModel flowOperatordModel = new FlowOperatordModel();
        flowOperatordModel.setStatus(FlowRecordEnum.reject.getCode());
        flowOperatordModel.setFlowModel(flowModel);
        flowOperatordModel.setUserId(userId);
        flowOperatordModel.setOperator(operator);
        this.operatorRecord(operatorRecord, flowOperatordModel);
        flowTaskOperatorRecordService.create(operatorRecord);
        //修改或签、会签经办数据
        this.handleIdStatus(0, nodeModel, operator, userInfo, taskNodeList);
        //更新流当前程经办状态
        flowTaskOperatorService.update(operator);
        boolean isUp = this.isReject(taskNode);
        //更新驳回节点
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
        //驳回审批数据作废
        Set<String> taskNodeId = taskNodeList.stream().filter(t -> t.getSortCode() >= nodeSortCode).map(t -> t.getId()).collect(Collectors.toSet());
        flowTaskOperatorRecordService.updateStatus(taskNodeId, flowTask.getId());
        //驳回节点
        List<FlowTaskOperatorEntity> operatorList = new ArrayList<>();
        //如果开始节点就不需要找下一节点
        boolean isStart = nextOperatorList.stream().filter(t -> FlowNature.NodeStart.equals(t.getCustom().getType())).count() > 0;
        if (!isStart) {
            //赋值数据
            FlowEngineEntity engine = flowEngineService.getInfo(flowTask.getFlowId());
            flowModel.setProcessId(flowTask.getId());
            flowModel.setId(flowTask.getId());
            if (FlowNature.CUSTOM.equals(engine.getFormType())) {
                Map<String, Object> formDataAll = flowModel.getFormData();
                Map<String, Object> data = JsonUtil.stringToMap(String.valueOf(formDataAll.get("data")));
                flowModel.setFormData(data);
            }
            this.nextOperator(operatorList, nextOperatorList, flowTask, flowModel);
            //驳回节点之后的状态修改
            List<String> rejectList = taskNodeList.stream().filter(t -> t.getSortCode() >= nodeSortCode).map(t -> t.getId()).collect(Collectors.toList());
            flowTaskNodeService.updateCompletion(rejectList, 0);
        } else {
            flowTaskNodeService.update(flowTask.getId());
            flowTaskOperatorService.update(flowTask.getId());
        }
        flowTaskOperatorService.create(operatorList);
        //更新驳回当前节点
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
        //驳回比例不够，不修改当前节点
        if (thisStepAll.size() > 0) {
            Collections.sort(progressList);
            flowTask.setCompletion(progressList.size() > 0 ? Integer.valueOf(progressList.get(0)) : 0);
            flowTask.setThisStepId(String.join(",", stepIdList));
            flowTask.setThisStep(String.join(",", stepNameList));
            //判断驳回节点是否是开发节点
            flowTask.setStatus(isStart ? FlowTaskStatusEnum.Reject.getCode() : flowTask.getStatus());
            //会签拒绝更新未审批用户
            Set<String> nodeCode = new HashSet<>();
            nodeCode.add(operator.getNodeCode());
            flowTaskOperatorService.updateReject(operator.getTaskId(), nodeCode);
        }
        //更新流程节点
        flowTaskService.updateById(flowTask);
        //获取抄送人
        List<FlowTaskCirculateEntity> circulateList = new ArrayList<>();
        nodeModel.getProperties().setCirculatePosition(new ArrayList<>());
        nodeModel.getProperties().setCirculateRole(new ArrayList<>());
        nodeModel.getProperties().setCirculateUser(new ArrayList<>());
        this.circulateList(nodeModel, circulateList, flowModel);
        flowTaskCirculateService.create(circulateList);
        //节点事件
        this.event(FlowRecordEnum.reject.getCode(), nodeModel, operatorRecord);
        //发送消息
        FlowMsgModel flowMsgModel = new FlowMsgModel();
        flowMsgModel.setCirculateList(new ArrayList<>());
        flowMsgModel.setNodeList(taskNodeList);
        flowMsgModel.setMsgTitel("【审核拒绝】");
        flowMsgModel.setOperatorList(operatorList);
        flowMsgModel.setTaskEntity(flowTask);
        this.message(flowMsgModel);
    }

    @Override
    public void recall(String id, FlowTaskOperatorRecordEntity operatorRecord, FlowModel flowModel) throws WorkFlowException {
        UserInfo userInfo = userProvider.get();
        FlowTaskEntity flowTask = flowTaskService.getInfo(operatorRecord.getTaskId());
        if (FlowNature.CompletionEnd.equals(flowTask.getCompletion())) {
            throw new WorkFlowException("当前流程已结束，无法撤回流程");
        }
        //流程所有节点
        List<FlowTaskNodeEntity> flowTaskNodeAllList = flowTaskNodeService.getList(flowTask.getId());
        List<FlowTaskNodeEntity> taskNodeList = flowTaskNodeAllList.stream().filter(t -> FlowNodeEnum.Process.getCode().equals(t.getState())).collect(Collectors.toList());
        //撤回节点
        FlowTaskNodeEntity recallNode = taskNodeList.stream().filter(t -> t.getNodeCode().equals(operatorRecord.getNodeCode())).findFirst().get();
        //撤回节点属性
        ChildNodeList nodeModel = JsonUtil.getJsonToBean(recallNode.getNodePropertyJson(), ChildNodeList.class);
        //流程经办
        List<FlowTaskOperatorEntity> operatorList = flowTaskOperatorService.getList(flowTask.getId());
        //撤回节点的下一节点
        List<FlowTaskNodeEntity> nextFlowTaskNodeList = flowTaskNodeAllList.stream().filter(t -> t.getSortCode() == recallNode.getSortCode() + 1).collect(Collectors.toList());
        List<FlowTaskOperatorEntity> isOperatorList = new ArrayList<>();
        for (FlowTaskNodeEntity node : nextFlowTaskNodeList) {
            List<FlowTaskOperatorEntity> operator = operatorList.stream().filter(t -> t.getNodeCode().equals(node.getNodeCode()) && t.getCompletion() == 0).collect(Collectors.toList());
            isOperatorList.addAll(operator);
        }
        if (FlowTaskStatusEnum.Reject.getCode().equals(flowTask.getStatus())) {
            throw new WorkFlowException("审批拒绝不可撤回");
        }
        boolean isRecall = (isOperatorList.size() == 0 || FlowTaskStatusEnum.Adopt.getCode().equals(flowTask.getStatus()));
        if (isRecall) {
            throw new WorkFlowException("当前流程被处理，无法撤回流程");
        }
        //更新撤回当前节点
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
        //更新流程节点
        flowTaskService.updateById(flowTask);
        //更新节点状态
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
        //未审批的节点作废
        Set<String> nodeCodeAll = operatorList.stream().filter(t -> t.getCompletion() == 0).map(t -> t.getNodeCode()).collect(Collectors.toSet());
        flowTaskOperatorService.updateReject(flowTask.getId(), nodeCodeAll);
        //修改撤回经办
        FlowTaskOperatorEntity recallOperator = flowTaskOperatorService.getInfo(operatorRecord.getTaskOperatorId());
        recallOperator.setHandleStatus(null);
        recallOperator.setHandleTime(null);
        recallOperator.setCompletion(0);
        flowTaskOperatorService.update(recallOperator);
        //修改撤回节点状态
        List<String> recallList = recallNodeList.stream().map(t -> t.getId()).collect(Collectors.toList());
        flowTaskNodeService.updateCompletion(recallList, 0);
        List<String> beforeList = flowTaskNodeAllList.stream().filter(t -> t.getSortCode() < recallNode.getSortCode()).map(t -> t.getId()).collect(Collectors.toList());
        flowTaskNodeService.updateCompletion(beforeList, 1);
        //召回记录
        FlowTaskOperatorEntity operator = JsonUtil.getJsonToBean(operatorRecord, FlowTaskOperatorEntity.class);
        operator.setId(operatorRecord.getTaskOperatorId());
        //审批数据赋值
        FlowOperatordModel flowOperatordModel = new FlowOperatordModel();
        flowOperatordModel.setStatus(FlowRecordEnum.revoke.getCode());
        flowOperatordModel.setFlowModel(flowModel);
        flowOperatordModel.setUserId(userInfo.getUserId());
        flowOperatordModel.setOperator(operator);
        this.operatorRecord(operatorRecord, flowOperatordModel);
        flowTaskOperatorRecordService.create(operatorRecord);
        //节点事件
        this.event(FlowRecordEnum.recall.getCode(), nodeModel, operatorRecord);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revoke(FlowTaskEntity flowTask, FlowModel flowModel) {
        UserInfo userInfo = userProvider.get();
        List<FlowTaskNodeEntity> list = flowTaskNodeService.getList(flowTask.getId());
        FlowTaskNodeEntity start = list.stream().filter(t -> FlowNature.NodeStart.equals(String.valueOf(t.getNodeType()))).findFirst().orElse(null);
        //删除节点
        flowTaskNodeService.deleteByTaskId(flowTask.getId());
        //删除经办
        flowTaskOperatorService.deleteByTaskId(flowTask.getId());
        //更新当前节点
        flowTask.setThisStepId(start.getNodeCode());
        flowTask.setThisStep(start.getNodeName());
        flowTask.setCompletion(0);
        flowTask.setStatus(FlowTaskStatusEnum.Revoke.getCode());
        flowTask.setStartTime(null);
        flowTask.setEndTime(null);
        flowTaskService.updateById(flowTask);
        //撤回记录
        FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
        operatorRecord.setTaskId(flowTask.getId());
        operatorRecord.setHandleStatus(FlowRecordEnum.revoke.getCode());
        FlowTaskOperatorEntity operator = JsonUtil.getJsonToBean(operatorRecord, FlowTaskOperatorEntity.class);
        //审批数据赋值
        FlowOperatordModel flowOperatordModel = new FlowOperatordModel();
        flowOperatordModel.setStatus(FlowRecordEnum.revoke.getCode());
        flowOperatordModel.setFlowModel(flowModel);
        flowOperatordModel.setUserId(userInfo.getUserId());
        flowOperatordModel.setOperator(operator);
        this.operatorRecord(operatorRecord, flowOperatordModel);
        flowTaskOperatorRecordService.create(operatorRecord);
        //撤回事件
        ChildNodeList nodeModel = JsonUtil.getJsonToBean(start.getNodePropertyJson(), ChildNodeList.class);
        this.event(FlowRecordEnum.revoke.getCode(), nodeModel, operatorRecord);
        //递归删除子流程任务
        this.delChild(flowTask);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(FlowTaskEntity flowTask, FlowModel flowModel) {
        UserInfo userInfo = userProvider.get();
        //终止记录
        FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
        FlowTaskOperatorEntity operator = new FlowTaskOperatorEntity();
        operator.setTaskId(flowTask.getId());
        operator.setNodeCode(flowTask.getThisStepId());
        operator.setNodeName(flowTask.getThisStep());
        //审批数据赋值
        FlowOperatordModel flowOperatordModel = new FlowOperatordModel();
        flowOperatordModel.setStatus(FlowRecordEnum.cancel.getCode());
        flowOperatordModel.setFlowModel(flowModel);
        flowOperatordModel.setUserId(userInfo.getUserId());
        flowOperatordModel.setOperator(operator);
        this.operatorRecord(operatorRecord, flowOperatordModel);
        flowTaskOperatorRecordService.create(operatorRecord);
        //更新实例
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
            //指派记录
            UserInfo userInfo = userProvider.get();
            FlowModel flowModel = JsonUtil.getJsonToBean(flowHandleModel, FlowModel.class);
            FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
            FlowTaskOperatorEntity operator = new FlowTaskOperatorEntity();
            operator.setTaskId(entity.getTaskId());
            operator.setNodeCode(entity.getNodeCode());
            operator.setNodeName(entity.getNodeName());
            //审批数据赋值
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
        //转办记录
        UserInfo userInfo = userProvider.get();
        FlowModel flowModel = new FlowModel();
        FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
        FlowTaskOperatorEntity operator = new FlowTaskOperatorEntity();
        operator.setTaskId(taskOperator.getTaskId());
        operator.setNodeCode(taskOperator.getNodeCode());
        operator.setNodeName(taskOperator.getNodeName());
        //审批数据赋值
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
        //已办人员
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
        //流程节点
        String[] tepId = taskEntity.getThisStepId() != null ? taskEntity.getThisStepId().split(",") : new String[]{};
        List<String> tepIdAll = Arrays.asList(tepId);
        List<FlowTaskNodeModel> flowTaskNodeListAll = JsonUtil.getJsonToList(taskNodeList, FlowTaskNodeModel.class);
        for (FlowTaskNodeModel model : flowTaskNodeListAll) {
            //流程图节点颜色
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
            //查询审批人
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
                //环节还没有经过和当前不显示审批人
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
        //表单权限
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
        //流程任务
        FlowTaskModel inof = JsonUtil.getJsonToBean(taskEntity, FlowTaskModel.class);
        FlowEngineEntity engine = flowEngineService.getInfo(taskEntity.getFlowId());
        inof.setAppFormUrl(engine.getAppFormUrl());
        inof.setFormUrl(engine.getFormUrl());
        inof.setType(engine.getType());
        vo.setFlowTaskInfo(inof);
        //流程经办
        vo.setFlowTaskOperatorList(JsonUtil.getJsonToList(taskOperatorList, FlowTaskOperatorModel.class));
        //流程引擎
        vo.setFlowFormInfo(taskEntity.getFlowForm());
        return vo;
    }

    //-----------------------------------提交保存--------------------------------------------

    /**
     * 流程任务赋值
     *
     * @param taskEntity 流程任务实例
     * @param engine     流程引擎实例
     * @param flowModel  提交数据
     * @throws WorkFlowException 异常
     */
    private void task(FlowTaskEntity taskEntity, FlowEngineEntity engine, FlowModel flowModel, String userId) throws WorkFlowException {
        if (flowModel.getId() != null && !checkStatus(taskEntity.getStatus())) {
            throw new WorkFlowException("当前流程正在运行不能重复提交");
        }
        //创建实例
        taskEntity.setId(flowModel.getProcessId());
        taskEntity.setProcessId(flowModel.getProcessId());
        taskEntity.setEnCode(flowModel.getBillNo());
        taskEntity.setFullName(flowModel.getFlowTitle());
        taskEntity.setFlowUrgent(flowModel.getFlowUrgent() != null ? flowModel.getFlowUrgent() : 1);
        taskEntity.setFlowId(engine.getId());
        taskEntity.setFlowCode(engine.getEnCode() != null ? engine.getEnCode() : "单据规则不存在");
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
     * 验证有效状态
     *
     * @param status 状态编码
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
     * 提交节点
     *
     * @param dataAll 所有流程节点
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
        endround.setNodeName("结束");
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
     * 递归遍历编码
     *
     * @param dataAll  所有节点
     * @param node     当前节点
     * @param treeList 保存走过的节点
     * @param num      排序
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
     * 创建节点
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
            taskNode.setNodeName(isSstart ? "开始" : properties.getTitle());
            taskNodeList.add(taskNode);
            if ("empty".equals(type)) {
                emptyList.add(taskNode);
            }
            if ("timer".equals(type)) {
                timerList.add(taskNode);
            }
        }
        //指向empty，继续指向下一个节点
        for (FlowTaskNodeEntity empty : emptyList) {
            List<FlowTaskNodeEntity> noxtEmptyList = taskNodeList.stream().filter(t -> t.getNodeNext().contains(empty.getNodeCode())).collect(Collectors.toList());
            for (FlowTaskNodeEntity entity : noxtEmptyList) {
                entity.setNodeNext(empty.getNodeNext());
            }
        }
        //指向timer，继续指向下一个节点
        for (FlowTaskNodeEntity timer : timerList) {
            //获取到timer的上一节点
            ChildNodeList timerlList = JsonUtil.getJsonToBean(timer.getNodePropertyJson(), ChildNodeList.class);
            DateProperties timers = timerlList.getTimer();
            timers.setNodeId(timer.getNodeCode());
            timers.setTime(true);
            List<FlowTaskNodeEntity> upEmptyList = taskNodeList.stream().filter(t -> t.getNodeNext().contains(timer.getNodeCode())).collect(Collectors.toList());
            for (FlowTaskNodeEntity entity : upEmptyList) {
                //上一节点赋值timer的属性
                ChildNodeList modelList = JsonUtil.getJsonToBean(entity.getNodePropertyJson(), ChildNodeList.class);
                modelList.setTimer(timers);
                entity.setNodeNext(timer.getNodeNext());
                entity.setNodePropertyJson(JsonUtilEx.getObjectToString(modelList));
            }
        }
    }

    //-------------------------审批--------------------------------
    //---------通过-------------

    /**
     * 统计当前节点总人数
     *
     * @param nodeIdAll     子流程关联的id
     * @param taskNodeList 所有节点
     * @param operatorList 节点的审批人
     */
    private void totalAll(Map<String, List<String>> nodeIdAll, List<FlowTaskOperatorEntity> operatorList, List<FlowTaskNodeEntity> taskNodeList) {
        //更新关联子流程id
        for (String nodeId : nodeIdAll.keySet()) {
            FlowTaskNodeEntity entity = flowTaskNodeService.getInfo(nodeId);
            if (entity != null) {
                ChildNodeList childNodeList = JsonUtil.getJsonToBean(entity.getNodePropertyJson(), ChildNodeList.class);
                childNodeList.getCustom().setTaskId(nodeIdAll.get(nodeId));
                entity.setNodePropertyJson(JsonUtil.getObjectToString(childNodeList));
                flowTaskNodeService.update(entity);
            }
        }
        //统计当前节点总人数
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
     * 下一审批人
     *
     * @param operatorListAll 审批人数据
     * @param nodeList        下一审批的数据
     * @param taskEntity      引擎实例
     * @param flowModel       提交数据
     * @throws WorkFlowException 异常
     */
    private Map<String, List<String>> nextOperator(List<FlowTaskOperatorEntity> operatorListAll, List<ChildNodeList> nodeList, FlowTaskEntity taskEntity, FlowModel flowModel) throws WorkFlowException {
        Map<String, List<String>> taskNode = new HashMap<>(16);
        try {
            List<UserEntity> userList = userService.getList();
            //查询审批人
            for (ChildNodeList childNode : nodeList) {
                List<FlowTaskOperatorEntity> operatorList = new ArrayList<>();
                Custom custom = childNode.getCustom();
                Properties properties = childNode.getProperties();
                String type = custom.getType();
                String flowId = properties.getFlowId();
                List<FlowAssignModel> assignList = childNode.getProperties().getAssignList();
                //判断是否超时
                TimeOutConfig config = properties.getTimeoutConfig();
                //判断子流程
                boolean isChild = FlowNature.NodeSubFlow.equals(type);
                if (isChild) {
                    //判断当前流程引擎类型
                    FlowEngineEntity parentEngine = flowEngineService.getInfo(taskEntity.getFlowId());
                    boolean isCustom = FlowNature.CUSTOM.equals(parentEngine.getFormType());
                    List<String> taskNodeList = new ArrayList<>();
                    FlowEngineEntity engine = flowEngineService.getInfo(flowId);
                    //创建子流程
                    Map<String, Object> data = this.childData(engine, flowModel, assignList, isCustom);
                    data.put("flowId", flowId);
                    //子节点审批人
                    List<String> list = this.childSaveList(childNode, taskEntity, userList);
                    for (String id : list) {
                        UserEntity userEntity = userList.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
                        String title = userEntity.getRealName() + "的" + engine.getFullName() + "(子流程)";
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
                        //审批人
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
     * 审批人
     *
     * @param childNode    当前节点数据
     * @param operatorList 审批人所有数据
     * @param taskEntity   引擎实例
     * @param flowModel    提交数据
     * @param details      true记录 false不记录
     * @throws WorkFlowException 异常
     */
    private void operator(ChildNodeList childNode, List<FlowTaskOperatorEntity> operatorList, FlowTaskEntity taskEntity, FlowModel flowModel, List<UserEntity> userList, boolean details) {
        String createUserId = taskEntity.getCreatorUserId();
        Date date = new Date();
        List<FlowTaskOperatorEntity> nextList = new ArrayList<>();
        Properties properties = childNode.getProperties();
        String type = properties.getAssigneeType();
        String userId = "";
        String freeApproverUserId = flowModel.getFreeApproverUserId();
        //【加签】
        if (StringUtil.isNotEmpty(freeApproverUserId)) {
            this.operatorUser(nextList, freeApproverUserId, date, childNode, true);
            //加签记录
            if (details) {
                UserInfo userInfo = userProvider.get();
                Custom custom = childNode.getCustom();
                FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
                FlowTaskOperatorEntity operator = new FlowTaskOperatorEntity();
                operator.setTaskId(childNode.getTaskId());
                operator.setNodeCode(custom.getNodeId());
                operator.setNodeName(properties.getTitle());
                //审批数据赋值
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
            //发起者【发起者主管】
            if (FlowTaskOperatorEnum.LaunchCharge.getCode().equals(type)) {
                //时时查用户主管
                UserEntity info = userService.getInfo(createUserId);
                if (info != null) {
                    userId = getManagerByLevel(info.getManagerId(), properties.getManagerLevel(), userList);
                    this.operatorUser(nextList, userId, date, childNode);
                }
            }
            //发起者【部门主管】
            if (FlowTaskOperatorEnum.DepartmentCharge.getCode().equals(type)) {
                UserEntity userEntity = userService.getInfo(createUserId);
                OrganizeEntity organizeEntity = organizeService.getInfo(userEntity != null ? userEntity.getOrganizeId() : "");
                if (organizeEntity != null) {
                    userId = organizeEntity.getManager();
                    this.operatorUser(nextList, userId, date, childNode);
                }
            }
            //发起者【发起本人】
            if (FlowTaskOperatorEnum.InitiatorMe.getCode().equals(type)) {
                this.operatorUser(nextList, createUserId, date, childNode);
            }
            //【环节】
            if (FlowTaskOperatorEnum.Tache.getCode().equals(type)) {
                List<FlowTaskOperatorRecordEntity> operatorUserList = flowTaskOperatorRecordService.getList(taskEntity.getId()).stream().filter(t -> properties.getNodeId().equals(t.getNodeCode()) && FlowRecordEnum.audit.getCode().equals(t.getHandleStatus()) && FlowNodeEnum.Process.getCode().equals(t.getStatus())).collect(Collectors.toList());
                if (operatorUserList.size() > 0) {
                    for (FlowTaskOperatorRecordEntity entity : operatorUserList) {
                        this.operatorUser(nextList, entity.getHandleId(), date, childNode);
                    }
                }
            }
            //【变量】
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
            //【服务】
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
            //发起者【指定用户】
            for (String userIdAll : properties.getApprovers()) {
                this.operatorUser(nextList, userIdAll, date, childNode);
            }
            //发起者【指定岗位】
            List<String> positionList = properties.getApproverPos();
            //发起者【指定角色】
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
     * 递归主管
     *
     * @param managerId 主管id
     * @param level     第几级
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
     * 封装审批人
     *
     * @param nextList  所有审批人数据
     * @param handLeId  审批人id
     * @param date      审批日期
     * @param childNode 当前节点数据
     */
    private void operatorUser(List<FlowTaskOperatorEntity> nextList, String handLeId, Date date, ChildNodeList childNode) {
        this.operatorUser(nextList, handLeId, date, childNode, false);
    }

    /**
     * 封装审批人
     *
     * @param nextList  所有审批人数据
     * @param handLeId  审批人id
     * @param date      审批日期
     * @param childNode 当前节点数据
     * @param isStatus  是否加签人
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
     * 用户是否重复
     *
     * @param userPosition 审批人
     * @param operatorList 经办的数据
     * @param childNode    节点数据
     */
    private void getApproverUser(List<String> userPosition, List<FlowTaskOperatorEntity> operatorList, ChildNodeList childNode) {
        Date date = new Date();
        for (String user : userPosition) {
            //判断是否有添加这个人
            if (operatorList.stream().filter(t -> t.getHandleId().equals(user)).count() == 0) {
                this.operatorUser(operatorList, user, date, childNode);
            }
        }
    }

    /**
     * 更新经办数据
     *
     * @param status       审批类型（0：拒绝，1：同意）
     * @param nodeModel    当前节点属性
     * @param operator     当前经办
     * @param userInfo     用户
     * @param taskNodeList 节点list
     */
    private void handleIdStatus(int status, ChildNodeList nodeModel, FlowTaskOperatorEntity operator, UserInfo userInfo, List<FlowTaskNodeEntity> taskNodeList) {
        Properties properties = nodeModel.getProperties();
        Integer counterSign = properties.getCounterSign();
        operator.setHandleTime(new Date());
        operator.setHandleStatus(status);
        String type = properties.getAssigneeType();
        boolean isApprover = FlowNature.FixedJointlyApprover.equals(counterSign);
        //更新委托的id
        List<String> userIdListAll = new ArrayList<>();
        //审核自己的话，更新委托人，不是审核自己的话，不更新自己
        if (userInfo.getUserId().equals(operator.getHandleId())) {
            List<String> userList = flowDelegateService.getUser(userInfo.getUserId()).stream().map(t -> t.getCreatorUserId()).collect(Collectors.toList());
            userIdListAll.addAll(userList);
        }
        if (status == 1) {
            if (isApprover) {
                //更新会签都改成完成
                flowTaskOperatorService.update(operator.getTaskNodeId(), userIdListAll, "1");
            } else {
                //更新或签都改成完成
                flowTaskOperatorService.update(operator.getTaskNodeId(), type);
            }
            operator.setCompletion(1);
            //修改当前审批的定时器
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
                //更新会签都改成完成
                flowTaskOperatorService.update(operator.getTaskNodeId(), userIdListAll, "-1");
            } else {
                //更新或签都改成完成
                flowTaskOperatorService.update(operator.getTaskNodeId(), type);
            }
            operator.setCompletion(-1);
            operator.setState(FlowNodeEnum.Futility.getCode());
        }
    }

    /**
     * 判断是否进行下一步
     *
     * @param nodeListAll    所有节点
     * @param nextNodeEntity 下一节点
     * @param taskNode       当前节点
     * @param flowModel      提交数据
     * @return
     */
    private List<FlowTaskNodeEntity> isNextAll(List<FlowTaskNodeEntity> nodeListAll, List<FlowTaskNodeEntity> nextNodeEntity, FlowTaskNodeEntity taskNode, FlowModel flowModel) {
        //1.先看是否加签人，有都不要进行，无进行下一步
        //2.判断会签是否比例通过
        //3.判断分流是否都结束
        //4.判断审批人是否都通过
        List<FlowTaskNodeEntity> result = new ArrayList<>();
        boolean hasFreeApprover = StringUtil.isNotEmpty(flowModel.getFreeApproverUserId());
        if (hasFreeApprover) {
            result.add(taskNode);
            //加签记录
        } else {
            ChildNodeList nodeModel = JsonUtil.getJsonToBean(taskNode.getNodePropertyJson(), ChildNodeList.class);
            Properties properties = nodeModel.getProperties();
            //会签通过
            boolean isCountersign = true;
            boolean fixed = FlowNature.FixedJointlyApprover.equals(properties.getCounterSign());
            long pass = properties.getCountersignRatio();
            String type = properties.getAssigneeType();
            //判断是否是会签
            if (fixed) {
                List<FlowTaskOperatorEntity> operatorList = flowTaskOperatorService.getList(taskNode.getTaskId()).stream().filter(t -> t.getTaskNodeId().equals(taskNode.getId()) && FlowNodeEnum.Process.getCode().equals(t.getState())).collect(Collectors.toList());
                double total = operatorList.size();
                double passNum = operatorList.stream().filter(t -> t.getCompletion() == 1).count();
                isCountersign = this.isCountersign(pass, total, passNum);
            }
            //流程通过
            if (isCountersign) {
                //会签通过更新未审批用户
                if (fixed) {
                    flowTaskOperatorService.update(nodeModel.getTaskNodeId(), type);
                }
                taskNode.setCompletion(1);
                //跟新审批状态
                flowTaskNodeService.update(taskNode);
                //分流通过
                boolean isShunt = this.isShunt(nodeListAll, nextNodeEntity, taskNode);
                if(isShunt){
                    result.addAll(nextNodeEntity);
                }
            }
        }
        return result;
    }

    /**
     * 会签比例
     *
     * @param pass    比例
     * @param total   总数
     * @param passNum 数量
     * @return
     */
    private boolean isCountersign(long pass, double total, double passNum) {
        int scale = (int) (passNum / total * 100);
        return scale >= pass;
    }


    /**
     * 判断分流是否结束
     *
     * @param nodeListAll    所有节点
     * @param nextNodeEntity 下一节点
     * @param taskNode       单前节点
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
     * 判断传阅用户是否存在指定用户
     *
     * @param userPosition  获取岗位岗位下的人
     * @param circulateList 经办的数据
     * @param nodeModel     节点数据
     */
    private void getCirculateUser(List<String> userPosition, List<FlowTaskCirculateEntity> circulateList, ChildNodeList nodeModel) {
        for (String user : userPosition) {
            //判断是否有添加这个人
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
     * 抄送人
     *
     * @param nodeModel     当前json对象
     * @param circulateList 抄送list
     * @param flowModel     提交数据
     */
    private void circulateList(ChildNodeList nodeModel, List<FlowTaskCirculateEntity> circulateList, FlowModel flowModel) {
        Properties circleproperties = nodeModel.getProperties();
        Custom circlecustom = nodeModel.getCustom();
        String circletaskId = nodeModel.getTaskId();
        String circletaskNodeId = nodeModel.getTaskNodeId();
        //创建传阅【指定用户】
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
        //传阅者【指定角色】
        List<String> roleList = circleproperties.getCirculateRole();
        //传阅者【指定岗位】
        List<String> posList = circleproperties.getCirculatePosition();
        List<String> userAll = new ArrayList<>();
        userAll.addAll(roleList);
        userAll.addAll(posList);
        List<UserRelationEntity> listByObjectIdAll = userRelationService.getListByObjectIdAll(userAll);
        List<String> userPosition = listByObjectIdAll.stream().map(t -> t.getUserId()).collect(Collectors.toList());
        //指定传阅人
        String[] copyIds = StringUtil.isNotEmpty(flowModel.getCopyIds()) ? flowModel.getCopyIds().split(",") : new String[]{};
        List<String> id = Arrays.asList(copyIds);
        userPosition.addAll(id);
        this.getCirculateUser(userPosition, circulateList, nodeModel);
    }

    /**
     * 流程任务结束
     *
     * @param flowTask 流程任务
     */
    private boolean endround(FlowTaskEntity flowTask, ChildNodeList childNode) throws WorkFlowException {
        flowTask.setStatus(FlowTaskStatusEnum.Adopt.getCode());
        flowTask.setCompletion(100);
        flowTask.setEndTime(DateUtil.getNowDate());
        flowTask.setThisStepId(FlowNature.NodeEnd);
        flowTask.setThisStep("结束");
        //结束事件
        FlowTaskOperatorRecordEntity operatorRecord = new FlowTaskOperatorRecordEntity();
        operatorRecord.setTaskId(flowTask.getId());
        operatorRecord.setHandleStatus(flowTask.getStatus());
        this.event(FlowRecordEnum.end.getCode(), childNode, operatorRecord);
        //子流程结束，触发主流程
        boolean isEnd = this.isNext(flowTask);
        return isEnd;
    }

    //---------------拒绝-------------------

    /**
     * 审批驳回节点
     *
     * @param nodeListAll 所有节点
     * @param taskNode    审批节点
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
                throw new WorkFlowException("驳回节点不能是子流程");
            }
        }
        return result;
    }

    /**
     * 拒绝比例
     *
     * @param taskNode 节点实体
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

    //-----------------------子节点---------------------------------

    /**
     * 插入数据
     *
     * @param engine    引擎
     * @param flowModel 提交数据
     */
    private Map<String, Object> createData(FlowEngineEntity engine, FlowTaskEntity taskEntity, FlowModel flowModel) throws WorkFlowException {
        Map<String, Object> resultData = new HashMap<>(16);
        try {
            Map<String, Object> data = flowModel.getFormData();
            if (FlowNature.CUSTOM.equals(engine.getFormType())) {
                List<FlowTableModel> tableList = JsonUtil.getJsonToList(engine.getTables(), FlowTableModel.class);
                //获取属性
                DbLinkEntity dbLink = null;
                if (StringUtil.isNotEmpty(engine.getDbLinkId())) {
                    dbLink = dblinkService.getInfo(engine.getDbLinkId());
                }
                FormDataModel formData = JsonUtil.getJsonToBean(taskEntity.getFlowForm(), FormDataModel.class);
                List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
                if (StringUtil.isNotEmpty(flowModel.getId())) {
                    //更新
                    resultData = flowDataUtil.update(data, list, tableList, taskEntity.getProcessId(), dbLink);
                } else {
                    //新增
                    resultData = flowDataUtil.create(data, list, tableList, taskEntity.getProcessId(), new HashMap<>(16), dbLink);
                }
            } else {
                //系统表单
                String dataAll = JsonUtil.getObjectToString(data);
                if (engine.getType() != 1) {
                    String coed = engine.getEnCode();
                    this.formData(coed, flowModel.getProcessId(), dataAll);
                }
            }
        } catch (Exception e) {
            throw new WorkFlowException("新增数据失败");
        }
        return resultData;
    }

    /**
     * 判断子流程是否全部走完，进行主流程任务
     *
     * @param flowTask 子流程任务
     * @throws WorkFlowException
     */
    private boolean isNext(FlowTaskEntity flowTask) throws WorkFlowException {
        boolean isEnd = true;
        //子流程结束，触发主流程
        if (!FlowNature.ParentId.equals(flowTask.getParentId()) && StringUtil.isNotEmpty(flowTask.getParentId())) {
            isEnd = false;
            List<FlowTaskEntity> parentList = flowTaskService.getChildList(flowTask.getParentId());
            //判断子流程排除自己，判断其他子流程是否都完成
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
     * 子节点审批人
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
        //子节点部门主管
        if (FlowTaskOperatorEnum.ChildDepartmentCharge.getCode().equals(type)) {
            UserEntity userEntity = userService.getInfo(createUserId);
            OrganizeEntity organizeEntity = organizeService.getInfo(userEntity != null ? userEntity.getOrganizeId() : "");
            if (organizeEntity != null) {
                userId = organizeEntity.getManager();
                this.operatorUser(nextList, userId, date, childNode);
            }
        }
        //子节点发起者主管
        if (FlowTaskOperatorEnum.ChildLaunchCharge.getCode().equals(type)) {
            //时时查用户主管
            UserEntity info = userService.getInfo(createUserId);
            if (info != null) {
                userId = getManagerByLevel(info.getManagerId(), properties.getManagerLevel(), userList);
                this.operatorUser(nextList, userId, date, childNode);
            }
        }
        //子节点发起者本人
        if (FlowTaskOperatorEnum.ChildInitiatorMe.getCode().equals(type)) {
            this.operatorUser(nextList, createUserId, date, childNode);
        }
        //发起者【指定用户】
        for (String userIdAll : properties.getInitiator()) {
            this.operatorUser(nextList, userIdAll, date, childNode);
        }
        //发起者【指定岗位】
        List<String> positionList = properties.getInitiatePos();
        //发起者【指定角色】
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
     * 赋值
     *
     * @param data     数据
     * @param engine   引擎
     * @param parentId 上一节点
     * @return
     */
    private FlowModel assignment(Map<String, Object> data, FlowEngineEntity engine, String parentId, String title) {
        FlowModel flowModel = new FlowModel();
        String billNo = "单据规则不存在";
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
     * 子表表单赋值
     *
     * @param engine     子表引擎
     * @param flowModel  提交数据
     * @param assignList 数据传递
     * @param isCustom   true自定义表单 false系统表单
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
     * 递归删除子流程任务
     *
     * @param task 父节点流程任务
     */
    private void delChild(FlowTaskEntity task) {
        List<FlowTaskEntity> childTaskList = flowTaskService.getChildList(task.getId());
        for (FlowTaskEntity flowTask : childTaskList) {
            //删除子流程
            flowTaskService.deleteChild(flowTask);
            this.delChild(flowTask);
        }
    }

    //---------------------公共方法--------------------------

    /**
     * 事件处理
     *
     * @param status    事件状态
     * @param childNode 节点数据
     * @param record    审批数据
     */
    private void event(Integer status, ChildNodeList childNode, FlowTaskOperatorRecordEntity record) {
        boolean flag = false;
        String faceUrl = "";
        //属性
        if (childNode != null) {
            Properties properties = childNode.getProperties();
            if (FlowRecordEnum.audit.getCode().equals(status) || FlowRecordEnum.reject.getCode().equals(status)) {
                flag = properties.getHasApproverfunc() != null ? properties.getHasApproverfunc() : false;
                faceUrl = properties.getApproverInterfaceUrl() + "?" + taskNodeId + "=" + record.getTaskNodeId() + "&" +
                        handleStatus + "=" + record.getHandleStatus() + "&" + taskId + "=" + record.getTaskId();
                System.out.println("进入节点事件:" + faceUrl);
            } else if (FlowRecordEnum.submit.getCode().equals(status)) {
                flag = properties.getHasInitfunc() != null ? properties.getHasInitfunc() : false;
                faceUrl = properties.getInitInterfaceUrl() + "?" + taskNodeId + "=" + record.getTaskNodeId() + "&" + taskId + "=" + record.getTaskId();
                System.out.println("进入开始事件:" + faceUrl);
            } else if (FlowRecordEnum.revoke.getCode().equals(status)) {
                flag = properties.getHasFlowRecallFunc() != null ? properties.getHasFlowRecallFunc() : false;
                faceUrl = properties.getFlowRecallInterfaceUrl() + "?" + handleStatus + "=" + record.getHandleStatus()
                        + "&" + taskId + "=" + record.getTaskId();
                System.out.println("开始撤回事件:" + faceUrl);
            } else if (FlowRecordEnum.end.getCode().equals(status)) {
                flag = properties.getHasEndfunc() != null ? properties.getHasEndfunc() : false;
                faceUrl = properties.getEndInterfaceUrl() + "?" + taskNodeId + "=" + record.getTaskNodeId() + "&" +
                        handleStatus + "=" + record.getHandleStatus() + "&" + taskId + "=" + record.getTaskId();
                System.out.println("进入结束事件:" + faceUrl);
            } else if (FlowRecordEnum.recall.getCode().equals(status)) {
                flag = properties.getHasRecallFunc() != null ? properties.getHasRecallFunc() : false;
                faceUrl = properties.getRecallInterfaceUrl() + "?" + taskNodeId + "=" + record.getTaskNodeId() + "&" +
                        handleStatus + "=" + record.getHandleStatus() + "&" + taskId + "=" + record.getTaskId();
                System.out.println("进入撤回事件:" + faceUrl);
            }
        }
        if (flag) {
            String token = UserProvider.getToken();
            HttpUtil.httpRequest(faceUrl, "GET", null, token);
        }
    }


    /**
     * 发送消息
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
        //审批人
        for (FlowTaskOperatorEntity operator : operatorList) {
            FlowTaskNodeEntity node = nodeList.stream().filter(t -> t.getId().equals(operator.getTaskNodeId())).findFirst().orElse(null);
            if (node != null) {
                List<String> userList = operatorList.stream().map(t -> t.getHandleId()).collect(Collectors.toList());
                ChildNodeList childNode = JsonUtil.getJsonToBean(node.getNodePropertyJson(), ChildNodeList.class);
                Properties properties = childNode.getProperties();
                List<String> messageType = properties.getMessageType() != null ? properties.getMessageType() : Arrays.asList(new String[]{});
                SentMessageForm sentMessageForm = this.message("【审核】", FlowMessageEnum.wait.getCode(), messageType, taskEntity, userList);
                messageList.add(sentMessageForm);
            }
        }
        //抄送人
        for (FlowTaskCirculateEntity circulate : circulateList) {
            FlowTaskNodeEntity node = nodeList.stream().filter(t -> t.getId().equals(circulate.getTaskNodeId())).findFirst().orElse(null);
            if (node != null) {
                List<String> userList = circulateList.stream().map(t -> t.getObjectId()).collect(Collectors.toList());
                ChildNodeList childNode = JsonUtil.getJsonToBean(node.getNodePropertyJson(), ChildNodeList.class);
                Properties properties = childNode.getProperties();
                List<String> messageType = properties.getMessageType() != null ? properties.getMessageType() : Arrays.asList(new String[]{});
                SentMessageForm sentMessageForm = this.message("【抄送】", FlowMessageEnum.circulate.getCode(), messageType, taskEntity, userList);
                messageList.add(sentMessageForm);
            }
        }
        //发给创建人
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
     * 发送消息封装
     *
     * @param title       名称
     * @param type        类型
     * @param messageType 发送消息类型
     * @param taskEntity  所有节点
     * @param userList    发送用户
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
     * 更新当前节点
     *
     * @param nextOperatorList 下一审批节点
     * @param flowTaskNodeList 所有节点
     * @param flowTask         流程任务
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
     * 审核记录
     *
     * @param record         审批实例
     * @param operatordModel 对象数据
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
        record.setNodeName(operator.getNodeName() != null ? operator.getNodeName() : "开始");
        record.setTaskOperatorId(operator.getId());
        record.setTaskNodeId(operator.getTaskNodeId());
        record.setTaskId(operator.getTaskId());
        record.setSignImg(flowModel.getSignImg());
        record.setStatus(FlowTaskOperatorEnum.FreeApprover.getCode().equals(operator.getHandleType()) ? FlowNodeEnum.Futility.getCode() : FlowNodeEnum.Process.getCode());
    }

    /**
     * 修改系统表单数据
     *
     * @param code 编码
     * @param id   主键id
     * @param data 数据
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
                throw new WorkFlowException("系统表单反射失败");
            }
        }
    }

    /**
     * 定时器
     *
     * @param taskOperator 流程经办
     * @param taskNodeList 所有流程节点
     * @param operatorList 下一流程经办
     * @return
     */
    private List<FlowTaskOperatorEntity> timer(FlowTaskOperatorEntity taskOperator, List<FlowTaskNodeEntity> taskNodeList, List<FlowTaskOperatorEntity> operatorList) {
        List<FlowTaskOperatorEntity> operatorListAll = new ArrayList<>();
        FlowTaskNodeEntity taskNode = taskNodeList.stream().filter(t -> t.getId().equals(taskOperator.getTaskNodeId())).findFirst().orElse(null);
        if (taskNode != null) {
            //获取其他分流的定时器
            List<String> nodeList = taskNodeList.stream().filter(t -> t.getSortCode().equals(taskNode.getSortCode())).map(t -> t.getId()).collect(Collectors.toList());
            List<FlowTaskOperatorEntity> operatorAll = flowTaskOperatorService.getList(taskOperator.getTaskId());
            Set<Date> dateListAll = new HashSet<>();
            List<FlowTaskOperatorEntity> list = operatorAll.stream().filter(t -> nodeList.contains(t.getTaskNodeId())).collect(Collectors.toList());
            for (FlowTaskOperatorEntity operator : list) {
                List<Date> dateList = JsonUtil.getJsonToList(operator.getDescription(), Date.class);
                dateListAll.addAll(dateList);
            }
            //获取单前审批定时器
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
     * 短信模板
     *
     * @param title  标题
     * @param status 状态
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

