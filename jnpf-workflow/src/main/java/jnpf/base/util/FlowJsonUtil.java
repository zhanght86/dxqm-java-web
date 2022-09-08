package jnpf.base.util;

import jnpf.engine.entity.FlowTaskNodeEntity;
import jnpf.engine.model.flowengine.shuntjson.childnode.ChildNode;
import jnpf.engine.model.flowengine.shuntjson.childnode.ProperCond;
import jnpf.engine.model.flowengine.shuntjson.childnode.Properties;
import jnpf.engine.model.flowengine.shuntjson.childnode.TimeOutConfig;
import jnpf.engine.model.flowengine.shuntjson.nodejson.ChildNodeList;
import jnpf.engine.model.flowengine.shuntjson.nodejson.ConditionList;
import jnpf.engine.model.flowengine.shuntjson.nodejson.Custom;
import jnpf.engine.model.flowengine.shuntjson.nodejson.DateProperties;
import jnpf.util.JsonUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 在线工作流开发
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Data
public class FlowJsonUtil {

    public static void main(String[] args) {
//        String nan1 = "{\"type\":\"start\",\"content\":\"所有人\",\"properties\":{\"title\":\"发起节点\",\"hasInitFunc\":false,\"initInterfaceUrl\":\"\",\"initInterfaceType\":\"POST\",\"hasEndFunc\":false,\"endInterfaceUrl\":\"\",\"endInterfaceType\":\"POST\",\"initiator\":[],\"initiatePos\":[]},\"nodeId\":\"7GtlbL\",\"childNode\":{\"type\":\"approver\",\"content\":\"游如柏/181005\",\"properties\":{\"title\":\"审批节点1\",\"approvers\":[\"146f6c3a-9eaa-4295-817f-8bf4e19b1a20\"],\"approverPos\":[],\"assigneeType\":6,\"formOperates\":[{\"id\":\"comInputField101\",\"name\":\"单行输入\",\"read\":true,\"write\":false},{\"id\":\"numInputField102\",\"name\":\"数字输入\",\"read\":true,\"write\":false},{\"id\":\"billRuleField103\",\"name\":\"单据组件\",\"read\":true,\"write\":false}],\"circulatePosition\":[],\"circulateUser\":[],\"progress\":\"50\",\"rejectStep\":\"0\",\"description\":\"\",\"hasApproverFunc\":false,\"approverInterfaceUrl\":\"\",\"approverInterfaceType\":\"GET\",\"hasRecallFunc\":false,\"recallInterfaceUrl\":\"\"},\"nodeId\":\"WDtlbL\",\"prevId\":\"7GtlbL\",\"childNode\":{\"type\":\"timer\",\"content\":\"将于1分钟后流转\",\"properties\":{\"title\":\"定时器\",\"day\":0,\"hour\":0,\"minute\":1,\"second\":0},\"nodeId\":\"8gxlbL\",\"prevId\":\"WDtlbL\",\"conditionNodes\":[{\"type\":\"condition\",\"content\":\"[单行输入 > 1] \\n\",\"properties\":{\"title\":\"条件1\",\"conditions\":[{\"fieldName\":\"单行输入\",\"symbolName\":\"大于\",\"filedValue\":\"1\",\"logicName\":\"并且\",\"field\":\"comInputField101\",\"symbol\":\">\",\"logic\":\"&&\"}],\"initiator\":null,\"priority\":0,\"isDefault\":false},\"nodeId\":\"e5zlbL\",\"prevId\":\"8gxlbL\",\"conditionNodes\":[{\"type\":\"condition\",\"content\":\"[单行输入 <= 50] \\n\",\"properties\":{\"title\":\"条件3\",\"conditions\":[{\"fieldName\":\"单行输入\",\"symbolName\":\"小于等于\",\"filedValue\":\"50\",\"logicName\":\"并且\",\"field\":\"comInputField101\",\"symbol\":\"<=\",\"logic\":\"&&\"}],\"initiator\":null,\"priority\":0,\"isDefault\":false},\"nodeId\":\"671mbL\",\"prevId\":\"e5zlbL\",\"childNode\":{\"type\":\"approver\",\"content\":\"卫丹雪/181008\",\"properties\":{\"title\":\"审批节点3\",\"approvers\":[\"22736bf4-f2d5-4081-9fb4-ca43d6235f56\"],\"approverPos\":[],\"assigneeType\":6,\"formOperates\":[{\"id\":\"comInputField101\",\"name\":\"单行输入\",\"read\":true,\"write\":false},{\"id\":\"numInputField102\",\"name\":\"数字输入\",\"read\":true,\"write\":false},{\"id\":\"billRuleField103\",\"name\":\"单据组件\",\"read\":true,\"write\":false}],\"circulatePosition\":[],\"circulateUser\":[],\"progress\":\"50\",\"rejectStep\":\"0\",\"description\":\"\",\"hasApproverFunc\":false,\"approverInterfaceUrl\":\"\",\"approverInterfaceType\":\"GET\",\"hasRecallFunc\":false,\"recallInterfaceUrl\":\"\"},\"nodeId\":\"vZ2mbL\",\"prevId\":\"671mbL\"}},{\"type\":\"condition\",\"content\":\"其他情况进入此流程\",\"properties\":{\"title\":\"条件4\",\"conditions\":[],\"initiator\":null,\"priority\":1,\"isDefault\":true},\"nodeId\":\"g51mbL\",\"prevId\":\"e5zlbL\",\"childNode\":{\"type\":\"approver\",\"content\":\"龙曼安/121021\",\"properties\":{\"title\":\"审批节点4\",\"approvers\":[\"7e2d74f1-277a-4a51-9311-8d02d579f251\"],\"approverPos\":[],\"assigneeType\":6,\"formOperates\":[{\"id\":\"comInputField101\",\"name\":\"单行输入\",\"read\":true,\"write\":false},{\"id\":\"numInputField102\",\"name\":\"数字输入\",\"read\":true,\"write\":false},{\"id\":\"billRuleField103\",\"name\":\"单据组件\",\"read\":true,\"write\":false}],\"circulatePosition\":[],\"circulateUser\":[],\"progress\":\"50\",\"rejectStep\":\"0\",\"description\":\"\",\"hasApproverFunc\":false,\"approverInterfaceUrl\":\"\",\"approverInterfaceType\":\"GET\",\"hasRecallFunc\":false,\"recallInterfaceUrl\":\"\"},\"nodeId\":\"ay2mbL\",\"prevId\":\"g51mbL\"}}],\"conditionType\":\"condition\"},{\"type\":\"condition\",\"content\":\"其他情况进入此流程\",\"properties\":{\"title\":\"条件2\",\"conditions\":[],\"initiator\":null,\"priority\":1,\"isDefault\":true},\"nodeId\":\"GFzlbL\",\"prevId\":\"8gxlbL\",\"childNode\":{\"type\":\"approver\",\"content\":\"织田信长/123456\",\"properties\":{\"title\":\"审批节点2\",\"approvers\":[\"1d291dca90bb4858b26f0bc0477bf97e\"],\"approverPos\":[],\"assigneeType\":6,\"formOperates\":[{\"id\":\"comInputField101\",\"name\":\"单行输入\",\"read\":true,\"write\":false},{\"id\":\"numInputField102\",\"name\":\"数字输入\",\"read\":true,\"write\":false},{\"id\":\"billRuleField103\",\"name\":\"单据组件\",\"read\":true,\"write\":false}],\"circulatePosition\":[],\"circulateUser\":[],\"progress\":\"50\",\"rejectStep\":\"0\",\"description\":\"\",\"hasApproverFunc\":false,\"approverInterfaceUrl\":\"\",\"approverInterfaceType\":\"GET\",\"hasRecallFunc\":false,\"recallInterfaceUrl\":\"\"},\"nodeId\":\"cD0mbL\",\"prevId\":\"GFzlbL\"}}],\"conditionType\":\"condition\",\"childNode\":{\"type\":\"approver\",\"content\":\"牧冰萍/181009\",\"properties\":{\"title\":\"审批节点5\",\"approvers\":[\"25e10fa4-7c68-4bc4-b751-92fa54f24c5b\"],\"approverPos\":[],\"assigneeType\":6,\"formOperates\":[{\"id\":\"comInputField101\",\"name\":\"单行输入\",\"read\":true,\"write\":false},{\"id\":\"numInputField102\",\"name\":\"数字输入\",\"read\":true,\"write\":false},{\"id\":\"billRuleField103\",\"name\":\"单据组件\",\"read\":true,\"write\":false}],\"circulatePosition\":[],\"circulateUser\":[],\"progress\":\"50\",\"rejectStep\":\"0\",\"description\":\"\",\"hasApproverFunc\":false,\"approverInterfaceUrl\":\"\",\"approverInterfaceType\":\"GET\",\"hasRecallFunc\":false,\"recallInterfaceUrl\":\"\"},\"nodeId\":\"E96mbL\",\"prevId\":\"8gxlbL\",\"childNode\":{\"type\":\"approver\",\"content\":\"桂白山/181028\",\"properties\":{\"title\":\"审批节点8\",\"approvers\":[\"964b44e3-b813-432a-8d05-cf6c36056b0d\"],\"approverPos\":[],\"assigneeType\":6,\"formOperates\":[{\"id\":\"comInputField101\",\"name\":\"单行输入\",\"read\":true,\"write\":false},{\"id\":\"numInputField102\",\"name\":\"数字输入\",\"read\":true,\"write\":false},{\"id\":\"billRuleField103\",\"name\":\"单据组件\",\"read\":true,\"write\":false}],\"circulatePosition\":[],\"circulateUser\":[],\"progress\":\"50\",\"rejectStep\":\"0\",\"description\":\"\",\"hasApproverFunc\":false,\"approverInterfaceUrl\":\"\",\"approverInterfaceType\":\"GET\",\"hasRecallFunc\":false,\"recallInterfaceUrl\":\"\"},\"nodeId\":\"BIHmbL\",\"prevId\":\"E96mbL\"},\"conditionNodes\":[{\"type\":\"approver\",\"content\":\"时紫安/181013\",\"isInterflow\":true,\"properties\":{\"title\":\"审批节点6\",\"approvers\":[\"317549d0-c4e5-400d-9ee5-72066869b1b3\"],\"approverPos\":[],\"assigneeType\":6,\"formOperates\":[{\"id\":\"comInputField101\",\"name\":\"单行输入\",\"read\":true,\"write\":false},{\"id\":\"numInputField102\",\"name\":\"数字输入\",\"read\":true,\"write\":false},{\"id\":\"billRuleField103\",\"name\":\"单据组件\",\"read\":true,\"write\":false}],\"circulatePosition\":[],\"circulateUser\":[],\"progress\":\"50\",\"rejectStep\":\"0\",\"description\":\"\",\"hasApproverFunc\":false,\"approverInterfaceUrl\":\"\",\"approverInterfaceType\":\"GET\",\"hasRecallFunc\":false,\"recallInterfaceUrl\":\"\"},\"nodeId\":\"YCDmbL\",\"prevId\":\"E96mbL\"},{\"type\":\"approver\",\"content\":\"越翠绿/181015\",\"isInterflow\":true,\"properties\":{\"title\":\"审批节点7\",\"approvers\":[\"3d855aed-83af-4fe3-92fd-3d105a3ebd02\"],\"approverPos\":[],\"assigneeType\":6,\"formOperates\":[{\"id\":\"comInputField101\",\"name\":\"单行输入\",\"read\":true,\"write\":false},{\"id\":\"numInputField102\",\"name\":\"数字输入\",\"read\":true,\"write\":false},{\"id\":\"billRuleField103\",\"name\":\"单据组件\",\"read\":true,\"write\":false}],\"circulatePosition\":[],\"circulateUser\":[],\"progress\":\"50\",\"rejectStep\":\"0\",\"description\":\"\",\"hasApproverFunc\":false,\"approverInterfaceUrl\":\"\",\"approverInterfaceType\":\"GET\",\"hasRecallFunc\":false,\"recallInterfaceUrl\":\"\"},\"nodeId\":\"P7DmbL\",\"prevId\":\"E96mbL\"}],\"conditionType\":\"interflow\"}}}}";
        String nan2 = "{\"type\":\"start\",\"content\":\"所有人\",\"properties\":{\"title\":\"发起节点\",\"hasInitFunc\":false,\"initInterfaceUrl\":\"\",\"initInterfaceType\":\"POST\",\"hasEndFunc\":false,\"endInterfaceUrl\":\"\",\"endInterfaceType\":\"POST\",\"initiator\":[],\"initiatePos\":[]},\"nodeId\":\"xT8I4L\",\"childNode\":{\"type\":\"approver\",\"content\":\"发起者主管\",\"properties\":{\"title\":\"审批节点1\",\"approvers\":[],\"approverPos\":[],\"assigneeType\":1,\"formOperates\":[{\"id\":\"comInputField101\",\"name\":\"单行输入\",\"read\":true,\"write\":false},{\"id\":\"textareaField102\",\"name\":\"多行输入\",\"read\":true,\"write\":false},{\"id\":\"numInputField103\",\"name\":\"数字输入\",\"read\":true,\"write\":false},{\"id\":\"switchField104\",\"name\":\"开关\",\"read\":true,\"write\":false},{\"id\":\"radioField105\",\"name\":\"单选框组\",\"read\":true,\"write\":false},{\"id\":\"checkboxField106\",\"name\":\"多选框组\",\"read\":true,\"write\":false},{\"id\":\"selectField107\",\"name\":\"下拉选择\",\"read\":true,\"write\":false},{\"id\":\"billRuleField108\",\"name\":\"单据组件\",\"read\":true,\"write\":false}],\"circulatePosition\":[],\"circulateUser\":[],\"progress\":\"50\",\"rejectStep\":\"0\",\"description\":\"\",\"hasApproverFunc\":false,\"approverInterfaceUrl\":\"\",\"approverInterfaceType\":\"GET\",\"hasRecallFunc\":false,\"recallInterfaceUrl\":\"\"},\"nodeId\":\"NX8I4L\",\"prevId\":\"xT8I4L\",\"childNode\":{\"type\":\"approver\",\"content\":\"发起者本人\",\"properties\":{\"title\":\"审批节点2\",\"approvers\":[],\"approverPos\":[],\"assigneeType\":3,\"formOperates\":[{\"id\":\"comInputField101\",\"name\":\"单行输入\",\"read\":true,\"write\":false},{\"id\":\"textareaField102\",\"name\":\"多行输入\",\"read\":true,\"write\":false},{\"id\":\"numInputField103\",\"name\":\"数字输入\",\"read\":true,\"write\":false},{\"id\":\"switchField104\",\"name\":\"开关\",\"read\":true,\"write\":false},{\"id\":\"radioField105\",\"name\":\"单选框组\",\"read\":true,\"write\":false},{\"id\":\"checkboxField106\",\"name\":\"多选框组\",\"read\":true,\"write\":false},{\"id\":\"selectField107\",\"name\":\"下拉选择\",\"read\":true,\"write\":false},{\"id\":\"billRuleField108\",\"name\":\"单据组件\",\"read\":true,\"write\":false}],\"circulatePosition\":[],\"circulateUser\":[],\"progress\":\"50\",\"rejectStep\":\"0\",\"description\":\"\",\"hasApproverFunc\":false,\"approverInterfaceUrl\":\"\",\"approverInterfaceType\":\"GET\",\"hasRecallFunc\":false,\"recallInterfaceUrl\":\"\"},\"nodeId\":\"G5BI4L\",\"prevId\":\"NX8I4L\",\"conditionNodes\":[{\"type\":\"condition\",\"content\":\"[单行输入 >= 20] 并且\\n[下拉选择 == 1] \\n\",\"properties\":{\"title\":\"条件\",\"conditions\":[{\"fieldName\":\"单行输入\",\"symbolName\":\"大于等于\",\"filedValue\":\"20\",\"logicName\":\"并且\",\"field\":\"comInputField101\",\"symbol\":\">=\",\"logic\":\"&&\"},{\"fieldName\":\"下拉选择\",\"symbolName\":\"等于\",\"filedValue\":\"1\",\"logicName\":\"并且\",\"field\":\"selectField107\",\"symbol\":\"==\",\"logic\":\"&&\"}],\"initiator\":null,\"priority\":0,\"isDefault\":false},\"nodeId\":\"H0DI4L\",\"prevId\":\"G5BI4L\",\"childNode\":{\"type\":\"approver\",\"content\":\"发起者主管\",\"properties\":{\"title\":\"审批节点3\",\"approvers\":[],\"approverPos\":[],\"assigneeType\":1,\"formOperates\":[{\"id\":\"comInputField101\",\"name\":\"单行输入\",\"read\":true,\"write\":false},{\"id\":\"textareaField102\",\"name\":\"多行输入\",\"read\":true,\"write\":false},{\"id\":\"numInputField103\",\"name\":\"数字输入\",\"read\":true,\"write\":false},{\"id\":\"switchField104\",\"name\":\"开关\",\"read\":true,\"write\":false},{\"id\":\"radioField105\",\"name\":\"单选框组\",\"read\":true,\"write\":false},{\"id\":\"checkboxField106\",\"name\":\"多选框组\",\"read\":true,\"write\":false},{\"id\":\"selectField107\",\"name\":\"下拉选择\",\"read\":true,\"write\":false},{\"id\":\"billRuleField108\",\"name\":\"单据组件\",\"read\":true,\"write\":false}],\"circulatePosition\":[],\"circulateUser\":[],\"progress\":\"50\",\"rejectStep\":\"0\",\"description\":\"\",\"hasApproverFunc\":false,\"approverInterfaceUrl\":\"\",\"approverInterfaceType\":\"GET\",\"hasRecallFunc\":false,\"recallInterfaceUrl\":\"\"},\"nodeId\":\"OmUI4L\",\"prevId\":\"H0DI4L\"}},{\"type\":\"condition\",\"content\":\"[单行输入 <= 10] 或者\\n[数字输入 <= 10] \\n\",\"properties\":{\"title\":\"条件\",\"conditions\":[{\"fieldName\":\"单行输入\",\"symbolName\":\"小于等于\",\"filedValue\":\"10\",\"logicName\":\"或者\",\"field\":\"comInputField101\",\"symbol\":\"<=\",\"logic\":\"||\"},{\"fieldName\":\"数字输入\",\"symbolName\":\"小于等于\",\"filedValue\":\"10\",\"logicName\":\"并且\",\"field\":\"numInputField103\",\"symbol\":\"<=\",\"logic\":\"&&\"}],\"initiator\":null,\"priority\":1,\"isDefault\":false},\"nodeId\":\"P0DI4L\",\"prevId\":\"G5BI4L\",\"childNode\":{\"type\":\"approver\",\"content\":\"加签\",\"properties\":{\"title\":\"审批节点4\",\"approvers\":[],\"approverPos\":[],\"assigneeType\":7,\"formOperates\":[{\"id\":\"comInputField101\",\"name\":\"单行输入\",\"read\":true,\"write\":false},{\"id\":\"textareaField102\",\"name\":\"多行输入\",\"read\":true,\"write\":false},{\"id\":\"numInputField103\",\"name\":\"数字输入\",\"read\":true,\"write\":false},{\"id\":\"switchField104\",\"name\":\"开关\",\"read\":true,\"write\":false},{\"id\":\"radioField105\",\"name\":\"单选框组\",\"read\":true,\"write\":false},{\"id\":\"checkboxField106\",\"name\":\"多选框组\",\"read\":true,\"write\":false},{\"id\":\"selectField107\",\"name\":\"下拉选择\",\"read\":true,\"write\":false},{\"id\":\"billRuleField108\",\"name\":\"单据组件\",\"read\":true,\"write\":false}],\"circulatePosition\":[],\"circulateUser\":[],\"progress\":\"50\",\"rejectStep\":\"0\",\"description\":\"\",\"hasApproverFunc\":false,\"approverInterfaceUrl\":\"\",\"approverInterfaceType\":\"GET\",\"hasRecallFunc\":false,\"recallInterfaceUrl\":\"\"},\"nodeId\":\"EGVI4L\",\"prevId\":\"P0DI4L\"}},{\"type\":\"condition\",\"content\":\"[单行输入 == 15] \\n\",\"properties\":{\"title\":\"条件\",\"conditions\":[{\"fieldName\":\"单行输入\",\"symbolName\":\"等于\",\"filedValue\":\"15\",\"logicName\":\"并且\",\"field\":\"comInputField101\",\"symbol\":\"==\",\"logic\":\"&&\"}],\"initiator\":null,\"priority\":2,\"isDefault\":false},\"nodeId\":\"LKQI4L\",\"prevId\":\"G5BI4L\",\"childNode\":{\"type\":\"approver\",\"content\":\"蒲安筠/101005,常青香/101006\",\"properties\":{\"title\":\"审批节点5\",\"approvers\":[\"6b97f5b0-06ba-4f9d-a93d-876abbaaa767\",\"a133c2e4-212d-414e-b9d1-5ca443b1e95d\"],\"approverPos\":[],\"assigneeType\":6,\"formOperates\":[{\"id\":\"comInputField101\",\"name\":\"单行输入\",\"read\":true,\"write\":false},{\"id\":\"textareaField102\",\"name\":\"多行输入\",\"read\":true,\"write\":false},{\"id\":\"numInputField103\",\"name\":\"数字输入\",\"read\":true,\"write\":false},{\"id\":\"switchField104\",\"name\":\"开关\",\"read\":true,\"write\":false},{\"id\":\"radioField105\",\"name\":\"单选框组\",\"read\":true,\"write\":false},{\"id\":\"checkboxField106\",\"name\":\"多选框组\",\"read\":true,\"write\":false},{\"id\":\"selectField107\",\"name\":\"下拉选择\",\"read\":true,\"write\":false},{\"id\":\"billRuleField108\",\"name\":\"单据组件\",\"read\":true,\"write\":false}],\"circulatePosition\":[],\"circulateUser\":[],\"progress\":\"50\",\"rejectStep\":\"0\",\"description\":\"\",\"hasApproverFunc\":false,\"approverInterfaceUrl\":\"\",\"approverInterfaceType\":\"GET\",\"hasRecallFunc\":false,\"recallInterfaceUrl\":\"\"},\"nodeId\":\"CSVI4L\",\"prevId\":\"LKQI4L\"}}],\"conditionType\":\"condition\",\"childNode\":{\"type\":\"approver\",\"content\":\"部门经理\",\"properties\":{\"title\":\"审批节点6\",\"approvers\":[],\"approverPos\":[],\"assigneeType\":2,\"formOperates\":[{\"id\":\"comInputField101\",\"name\":\"单行输入\",\"read\":true,\"write\":false},{\"id\":\"textareaField102\",\"name\":\"多行输入\",\"read\":true,\"write\":false},{\"id\":\"numInputField103\",\"name\":\"数字输入\",\"read\":true,\"write\":false},{\"id\":\"switchField104\",\"name\":\"开关\",\"read\":true,\"write\":false},{\"id\":\"radioField105\",\"name\":\"单选框组\",\"read\":true,\"write\":false},{\"id\":\"checkboxField106\",\"name\":\"多选框组\",\"read\":true,\"write\":false},{\"id\":\"selectField107\",\"name\":\"下拉选择\",\"read\":true,\"write\":false},{\"id\":\"billRuleField108\",\"name\":\"单据组件\",\"read\":true,\"write\":false}],\"circulatePosition\":[],\"circulateUser\":[],\"progress\":\"50\",\"rejectStep\":\"0\",\"description\":\"\",\"hasApproverFunc\":false,\"approverInterfaceUrl\":\"\",\"approverInterfaceType\":\"GET\",\"hasRecallFunc\":false,\"recallInterfaceUrl\":\"\"},\"nodeId\":\"3ZWI4L\",\"prevId\":\"G5BI4L\"}}}}";
        String nan = nan2;
        ChildNode childNode = JsonUtil.getJsonToBean(nan, ChildNode.class);
        System.out.println("ss");
        List<ChildNodeList> childNodeListAll = new ArrayList<>();
        List<ConditionList> conditionListAll = new ArrayList<>();
        getTemplateAll(childNode, childNodeListAll, conditionListAll);
        Map<String, Object> data = new HashMap<>(16);
        data.put("comInputField101", 1);
//        String datas1 = "{\"comInputField101\":\"5\",\"numInputField102\":5,\"billRuleField103\":null}";
        String datas2 = "{\"billRuleField102\":\"WF_LTS202103180003\",\"comInputField101\":\"0\"}";
        String datas = datas2;
        ss(datas, childNodeListAll, conditionListAll);
        System.out.println("sdf");
    }

    public static void ss(String data, List<ChildNodeList> childNodeListAll, List<ConditionList> conditionListAll) {
        Map<String, Object> objectMap = new HashMap<>(16);
        for (ChildNodeList childNode : childNodeListAll) {
            String nodeId = childNode.getCustom().getNodeId();
            String next = nextNodeId(data, nodeId, childNodeListAll, conditionListAll);
            objectMap.put(nodeId, next);
        }
        System.out.println("s");
    }

    /**
     * 外层节点
     **/
    private static String cusNum = "0";

    /**
     * 获取下一节点
     **/
    public static String getNextNode(String nodeId, String data, List<ChildNodeList> childNodeListAll, List<ConditionList> conditionListAll) {
        String next = nextNodeId(data, nodeId, childNodeListAll, conditionListAll);
        return next;
    }

    /**
     * 下一节点id
     **/
    private static String nextNodeId(String data, String nodeId, List<ChildNodeList> childNodeListAll, List<ConditionList> conditionListAll) {
        String nextId = "";
        boolean flag = false;
        ChildNodeList childNode = childNodeListAll.stream().filter(t -> t.getCustom().getNodeId().equals(nodeId)).findFirst().orElse(null);
        String contextType = childNode.getConditionType();
        //条件、分流的判断
        if (StringUtils.isNotEmpty(contextType)) {
            if (FlowCondition.CONDITION.equals(contextType)) {
                List<String> nextNodeId = new ArrayList<>();
                getContionNextNode(data, conditionListAll, nodeId, nextNodeId);
                nextId = String.join(",", nextNodeId);
                if (StringUtils.isNotEmpty(nextId)) {
                    flag = true;
                }
            } else if (FlowCondition.INTERFLOW.equals(contextType)) {
                nextId = childNode.getCustom().getFlowId();
                flag = true;
            }
        }
        //子节点
        if (!flag) {
            if (childNode.getCustom().getFlow()) {
                nextId = childNode.getCustom().getFlowId();
            } else {
                //不是外层的下一节点
                if (!cusNum.equals(childNode.getCustom().getNum())) {
                    nextId = childNode.getCustom().getFirstId();
                    if (childNode.getCustom().getChild()) {
                        nextId = childNode.getCustom().getChildNode();
                    }
                } else {
                    //外层的子节点
                    if (childNode.getCustom().getChild()) {
                        nextId = childNode.getCustom().getChildNode();
                    }
                }
            }
        }
        return nextId;
    }

    //---------------------------------------------------递归获取当前的上节点和下节点----------------------------------------------

    /**
     * 获取当前已完成节点
     **/
    public static void upList(List<FlowTaskNodeEntity> flowTaskNodeList, String node, Set<String> upList, String[] tepId) {
        FlowTaskNodeEntity entity = flowTaskNodeList.stream().filter(t -> t.getNodeCode().equals(node)).findFirst().orElse(null);
        if (entity != null) {
            List<String> list = flowTaskNodeList.stream().filter(t -> t.getSortCode() != null && t.getSortCode() < entity.getSortCode()).map(t -> t.getNodeCode()).collect(Collectors.toList());
            list.removeAll(Arrays.asList(tepId));
            upList.addAll(list);
        }
    }

    /**
     * 获取当前未完成节点
     **/
    public static void nextList(List<FlowTaskNodeEntity> flowTaskNodeList, String node, Set<String> nextList, String[] tepId) {
        FlowTaskNodeEntity entity = flowTaskNodeList.stream().filter(t -> t.getNodeCode().equals(node)).findFirst().orElse(null);
        if (entity != null) {
            List<String> list = flowTaskNodeList.stream().filter(t -> t.getSortCode() != null && t.getSortCode() > entity.getSortCode()).map(t -> t.getNodeCode()).collect(Collectors.toList());
            list.removeAll(Arrays.asList(tepId));
            nextList.addAll(list);
        }
    }

    //---------------------------------------------------条件----------------------------------------------

    /**
     * 递归条件
     **/
    private static void getContionNextNode(String data, List<ConditionList> conditionListAll, String nodeId, List<String> nextNodeId) {
        List<ConditionList> conditionAll = conditionListAll.stream().filter(t -> t.getPrevId().equals(nodeId)).collect(Collectors.toList());
        for (int i = 0; i < conditionAll.size(); i++) {
            ConditionList condition = conditionAll.get(i);
            List<ProperCond> conditions = condition.getConditions();
            boolean flag = nodeConditionDecide(data, conditions, new HashMap<>(100), new HashMap<>(100));
            //判断条件是否成立或者其他情况条件
            if (flag || condition.getIsDefault()) {
                String conditionId = condition.getNodeId();
                List<ConditionList> collect = conditionListAll.stream().filter(t -> t.getPrevId().equals(conditionId)).collect(Collectors.toList());
                if (collect.size() > 0) {
                    getContionNextNode(data, conditionListAll, conditionId, nextNodeId);
                } else {
                    if (nextNodeId.size() == 0) {
                        //先获取条件下的分流节点
                        if (condition.getFlow()) {
                            nextNodeId.add(condition.getFlowId());
                        } else {
                            //条件的子节点
                            if (condition.getChild()) {
                                nextNodeId.add(condition.getChildNodeId());
                            } else {
                                nextNodeId.add(condition.getFirstId());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 节点条件判断
     **/
    private static boolean nodeConditionDecide(String formDataJson, List<ProperCond> conditionList, Map<String, String> jnpfKey, Map<String, Object> keyList) {
        boolean flag = false;
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("nashorn");
        Map<String, Object> map = JsonUtil.stringToMap(formDataJson);
        StringBuilder expression = new StringBuilder();
        for (int i = 0; i < conditionList.size(); i++) {
            String logic = conditionList.get(i).getLogic();
            String field = conditionList.get(i).getField();
            Object form = map.get(field);
            if (form == null) {
                form = "";
            }
            String formValue = stringToAscii(String.valueOf(form));
            String symbol = conditionList.get(i).getSymbol();
            if ("<>".equals(symbol)) {
                symbol = "!=";
            }
            String value = conditionList.get(i).getFiledValue();
            String jnpfkey = jnpfKey.get(field);
            String filedValue = stringToAscii(String.valueOf(value));
            expression.append(formValue + symbol + filedValue);
            if (!StringUtils.isEmpty(logic)) {
                if (i != conditionList.size() - 1) {
                    expression.append(" " + logic + " ");
                }
            }
        }
        try {
            String result = String.valueOf(scriptEngine.eval(expression.toString()));
            flag = Boolean.valueOf(result);
        } catch (ScriptException e) {
            System.out.println(e.getMessage());
        }
        return flag;
    }

    /**
     * 转成ascii码
     **/
    private static String stringToAscii(String value) {
        StringBuffer sbu = new StringBuffer();
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            sbu.append((int) chars[i]);
        }
        return sbu.toString();
    }

    //---------------------------------------------------------------解析--------------------------------------------------------------------------

    /**
     * 递归外层的节点
     **/
    public static void childListAll(ChildNode childNode, List<ChildNode> chilNodeList) {
        if (childNode != null) {
            chilNodeList.add(childNode);
            boolean haschildNode = childNode.getChildNode() != null;
            if (haschildNode) {
                ChildNode nextNode = childNode.getChildNode();
                childListAll(nextNode, chilNodeList);
            }
        }
    }

    /**
     * 最外层的json
     **/
    public static void getTemplateAll(ChildNode childNode, List<ChildNodeList> childNodeListAll, List<ConditionList> conditionListAll) {
        List<ChildNode> chilNodeList = new ArrayList<>();
        childListAll(childNode, chilNodeList);
        if (childNode != null) {
            String nodeId = childNode.getNodeId();
            String prevId = childNode.getPrevId();
            boolean haschildNode = childNode.getChildNode() != null;
            boolean hasconditionNodes = childNode.getConditionNodes() != null;
            Properties properties = childNode.getProperties();
            //属性赋值
            assignment(properties);
            ChildNodeList childNodeList = new ChildNodeList();
            childNodeList.setProperties(properties);
            //定时器
            DateProperties model = JsonUtil.getJsonToBean(properties, DateProperties.class);
            childNodeList.setTimer(model);
            //自定义属性
            Custom customModel = new Custom();
            customModel.setType(childNode.getType());
            customModel.setNum("0");
            customModel.setFirstId("");
            customModel.setChild(haschildNode);
            customModel.setNodeId(nodeId);
            customModel.setPrevId(prevId);
            customModel.setChildNode(haschildNode == true ? childNode.getChildNode().getNodeId() : "");
            //判断子节点数据是否还有分流节点,有的话保存分流节点id
            if (hasconditionNodes) {
                childNodeList.setConditionType(FlowCondition.CONDITION);
                List<ChildNode> conditionNodes = childNode.getConditionNodes().stream().filter(t -> t.getIsInterflow() != null).collect(Collectors.toList());
                boolean isFlow = conditionNodes.size() > 0;
                if (isFlow) {
                    customModel.setFlow(isFlow);
                    childNodeList.setConditionType(FlowCondition.INTERFLOW);
                    List<String> flowIdAll = conditionNodes.stream().map(t -> t.getNodeId()).collect(Collectors.toList());
                    customModel.setFlowId(String.join(",", flowIdAll));
                }
            }
            childNodeList.setCustom(customModel);
            childNodeListAll.add(childNodeList);
            String firstId = "";
            if (haschildNode) {
                firstId = childNode.getChildNode().getNodeId();
            }
            if (hasconditionNodes) {
                conditionList(childNode, firstId, childNodeListAll, conditionListAll, chilNodeList);
            }
            if (haschildNode) {
                getchildNode(childNode, firstId, childNodeListAll, conditionListAll, chilNodeList);
            }
        }
    }

    /**
     * 递归子节点的子节点
     **/
    private static void getchildNode(ChildNode parentChildNodeTest, String firstId, List<ChildNodeList> childNodeListAll, List<ConditionList> conditionListAll, List<ChildNode> chilNodeList) {
        ChildNode childNode = parentChildNodeTest.getChildNode();
        if (childNode != null) {
            String nodeId = childNode.getNodeId();
            String prevId = childNode.getPrevId();
            boolean haschildNode = childNode.getChildNode() != null;
            boolean hasconditionNodes = childNode.getConditionNodes() != null;
            Properties properModel = childNode.getProperties();
            //属性赋值
            assignment(properModel);
            ChildNodeList childNodeList = new ChildNodeList();
            childNodeList.setProperties(properModel);
            //定时器
            DateProperties model = JsonUtil.getJsonToBean(properModel, DateProperties.class);
            childNodeList.setTimer(model);
            //自定义属性
            Custom customModel = new Custom();
            customModel.setType(childNode.getType());
            boolean isFirst = chilNodeList.stream().filter(t -> t.getNodeId().equals(nodeId)).count() > 0;
            customModel.setNum(isFirst ? "0" : "1");
            customModel.setFirstId(firstId);
            if (isFirst) {
                customModel.setFirstId(haschildNode ? childNode.getChildNode().getNodeId() : "");
            }
            customModel.setChild(haschildNode);
            customModel.setNodeId(nodeId);
            customModel.setPrevId(prevId);
            customModel.setChildNode(haschildNode == true ? childNode.getChildNode().getNodeId() : "");
            //判断子节点数据是否还有分流节点,有的话保存分流节点id
            if (hasconditionNodes) {
                childNodeList.setConditionType(FlowCondition.CONDITION);
                List<ChildNode> conditionNodes = childNode.getConditionNodes().stream().filter(t -> t.getIsInterflow() != null).collect(Collectors.toList());
                boolean isFlow = conditionNodes.size() > 0;
                if (isFlow) {
                    customModel.setFlow(isFlow);
                    childNodeList.setConditionType(FlowCondition.INTERFLOW);
                    List<String> flowIdAll = conditionNodes.stream().map(t -> t.getNodeId()).collect(Collectors.toList());
                    customModel.setFlowId(String.join(",", flowIdAll));
                }
            }
            childNodeList.setCustom(customModel);
            childNodeListAll.add(childNodeList);
            //条件或者分流递归
            if (hasconditionNodes) {
                conditionList(childNode, firstId, childNodeListAll, conditionListAll, chilNodeList);
            }
            //子节点递归
            if (haschildNode) {
                getchildNode(childNode, firstId, childNodeListAll, conditionListAll, chilNodeList);
            }
        }
    }

    /**
     * 条件、分流递归
     **/
    private static void conditionList(ChildNode childNode, String firstId, List<ChildNodeList> childNodeListAll, List<ConditionList> conditionListAll, List<ChildNode> chilNodeList) {
        List<ChildNode> conditionNodes = childNode.getConditionNodes();
        if (conditionNodes.size() > 0) {
            //判断是条件还是分流
            //判断父节点是否还有子节点,有的话替换子节点数据
            ChildNode childNodeModel = childNode.getChildNode();
            if (childNodeModel != null) {
                firstId = childNodeModel.getNodeId();
            } else {
                ChildNode nodes = chilNodeList.stream().filter(t -> t.getNodeId().equals(childNode.getNodeId())).findFirst().orElse(null);
                if (nodes != null) {
                    if (nodes.getChildNode() != null) {
                        firstId = childNode.getChildNode().getNodeId();
                    } else {
                        firstId = "";
                    }
                }
            }
            for (ChildNode node : conditionNodes) {
                boolean conditionType = node.getIsInterflow() == null ? true : false;
                if (conditionType) {
                    getCondition(node, firstId, childNodeListAll, conditionListAll, chilNodeList);
                } else {
                    getConditonFlow(node, firstId, childNodeListAll, conditionListAll, chilNodeList);
                }
            }
        }
    }

    /**
     * 条件递归
     **/
    private static void getCondition(ChildNode childNode, String firstId, List<ChildNodeList> childNodeListAll, List<ConditionList> conditionListAll, List<ChildNode> chilNodeList) {
        if (childNode != null) {
            String nodeId = childNode.getNodeId();
            String prevId = childNode.getPrevId();
            boolean haschildNode = childNode.getChildNode() != null;
            boolean hasconditionNodes = childNode.getConditionNodes() != null;
            boolean isDefault = childNode.getProperties().getIsDefault() != null ? childNode.getProperties().getIsDefault() : false;
            ConditionList conditionList = new ConditionList();
            conditionList.setNodeId(nodeId);
            conditionList.setPrevId(prevId);
            conditionList.setChild(haschildNode);
            conditionList.setTitle(childNode.getProperties().getTitle());
            List<ProperCond> proList = JsonUtil.getJsonToList(childNode.getProperties().getConditions(), ProperCond.class);
            conditionList.setConditions(proList);
            conditionList.setChildNodeId(haschildNode == true ? childNode.getChildNode().getNodeId() : "");
            conditionList.setIsDefault(isDefault);
            conditionList.setFirstId(firstId);
            //判断子节点数据是否还有分流节点,有的话保存分流节点id
            if (hasconditionNodes) {
                List<ChildNode> conditionNodes = childNode.getConditionNodes().stream().filter(t -> t.getIsInterflow() != null).collect(Collectors.toList());
                boolean isFlow = conditionNodes.size() > 0;
                if (isFlow) {
                    conditionList.setFlow(isFlow);
                    List<String> flowIdAll = conditionNodes.stream().map(t -> t.getNodeId()).collect(Collectors.toList());
                    conditionList.setFlowId(String.join(",", flowIdAll));
                }
            }
            conditionListAll.add(conditionList);
            //递归条件、分流
            if (hasconditionNodes) {
                conditionList(childNode, firstId, childNodeListAll, conditionListAll, chilNodeList);
            }
            //递归子节点
            if (haschildNode) {
                getchildNode(childNode, firstId, childNodeListAll, conditionListAll, chilNodeList);
            }
        }
    }

    /**
     * 条件递归
     **/
    private static void getConditonFlow(ChildNode childNode, String firstId, List<ChildNodeList> childNodeListAll, List<ConditionList> conditionListAll, List<ChildNode> chilNodeList) {
        if (childNode != null) {
            String nodeId = childNode.getNodeId();
            String prevId = childNode.getPrevId();
            boolean haschildNode = childNode.getChildNode() != null;
            boolean hasconditionNodes = childNode.getConditionNodes() != null;
            Properties properties = childNode.getProperties();
            //属性赋值
            assignment(properties);
            ChildNodeList childNodeList = new ChildNodeList();
            childNodeList.setProperties(properties);
            //定时器
            DateProperties model = JsonUtil.getJsonToBean(properties, DateProperties.class);
            childNodeList.setTimer(model);
            //自定义属性
            Custom customModel = new Custom();
            customModel.setType(childNode.getType());
            customModel.setNum("1");
            customModel.setFirstId(firstId);
            customModel.setChild(haschildNode);
            customModel.setChildNode(haschildNode == true ? childNode.getChildNode().getNodeId() : "");
            customModel.setNodeId(nodeId);
            customModel.setPrevId(prevId);
            //判断子节点数据是否还有分流节点,有的话保存分流节点id
            if (hasconditionNodes) {
                childNodeList.setConditionType(FlowCondition.CONDITION);
                List<ChildNode> conditionNodes = childNode.getConditionNodes().stream().filter(t -> t.getIsInterflow() != null).collect(Collectors.toList());
                boolean isFlow = conditionNodes.size() > 0;
                if (isFlow) {
                    customModel.setFlow(isFlow);
                    childNodeList.setConditionType(FlowCondition.INTERFLOW);
                    List<String> flowIdAll = conditionNodes.stream().map(t -> t.getNodeId()).collect(Collectors.toList());
                    customModel.setFlowId(String.join(",", flowIdAll));
                }
            }
            childNodeList.setCustom(customModel);
            childNodeListAll.add(childNodeList);
            if (hasconditionNodes) {
                conditionList(childNode, firstId, childNodeListAll, conditionListAll, chilNodeList);
            }
            if (haschildNode) {
                getchildNode(childNode, firstId, childNodeListAll, conditionListAll, chilNodeList);
            }
        }
    }

    /**
     * 属性赋值
     *
     * @param properties
     */
    public static void assignment(Properties properties) {
        //审批、传阅人赋值
        properties.setApproverPos(properties.getApproverPos() != null ? properties.getApproverPos() : new ArrayList<>());
        properties.setApprovers(properties.getApprovers() != null ? properties.getApprovers() : new ArrayList<>());
        properties.setApproverRole(properties.getApproverRole() != null ? properties.getApproverRole() : new ArrayList<>());
        properties.setCirculatePosition(properties.getCirculatePosition() != null ? properties.getCirculatePosition() : new ArrayList<>());
        properties.setCirculateUser(properties.getCirculateUser() != null ? properties.getCirculateUser() : new ArrayList<>());
        properties.setCirculateRole(properties.getCirculateRole() != null ? properties.getCirculateRole() : new ArrayList<>());
        properties.setHasEndfunc(properties.getHasEndfunc() != null ? properties.getHasEndfunc() : false);
        properties.setHasInitfunc(properties.getHasInitfunc() != null ? properties.getHasInitfunc() : false);
        //通过按钮权限
        properties.setHasAuditBtn(properties.getHasAuditBtn() != null ? properties.getHasAuditBtn() : true);
        properties.setAuditBtnText(properties.getAuditBtnText() != null ? properties.getAuditBtnText() : "通过");
        //拒绝按钮权限
        properties.setHasRejectBtn(properties.getHasAuditBtn() != null ? properties.getHasAuditBtn() : true);
        properties.setRejectBtnText(properties.getRejectBtnText() != null ? properties.getRejectBtnText() : "拒绝");
        //撤回按钮权限
        properties.setHasRevokeBtn(properties.getHasRevokeBtn() != null ? properties.getHasRevokeBtn() : true);
        properties.setRevokeBtnText(properties.getRevokeBtnText() != null ? properties.getRevokeBtnText() : "撤回");
        //转办按钮权限
        properties.setHasTransferBtn(properties.getHasTransferBtn() != null ? properties.getHasTransferBtn() : true);
        properties.setTransferBtnText(properties.getTransferBtnText() != null ? properties.getTransferBtnText() : "转办");
        //是否签名
        properties.setHasSign(properties.getHasSign() != null ? properties.getHasSign() : false);
        //是否加签
        properties.setHasFreeApprover(properties.getHasFreeApprover() != null ? properties.getHasFreeApprover() : false);
        //是否自定义抄送人
        properties.setIsCustomCopy(properties.getIsCustomCopy() != null ? properties.getIsCustomCopy() : false);
        //第几主管
        properties.setManagerLevel(properties.getManagerLevel() != null ? properties.getManagerLevel() : 1);
        //会签比例
        properties.setCountersignRatio(properties.getCountersignRatio() != null ? properties.getCountersignRatio() : 100);
        //超时
        properties.setTimeoutConfig(properties.getTimeoutConfig() != null ? properties.getTimeoutConfig() : new TimeOutConfig());
        //或签、会签
        properties.setCounterSign(properties.getCounterSign() != null ? properties.getCounterSign() : FlowNature.Fixedapprover);
    }

}
