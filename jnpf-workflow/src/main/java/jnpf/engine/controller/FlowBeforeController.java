package jnpf.engine.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.util.FlowNature;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.engine.entity.*;
import jnpf.engine.enums.FlowNodeEnum;
import jnpf.engine.model.FlowHandleModel;
import jnpf.engine.model.flowbefore.FlowBeforeInfoVO;
import jnpf.engine.model.flowbefore.FlowBeforeListVO;
import jnpf.engine.model.flowengine.FlowModel;
import jnpf.engine.model.flowtask.PaginationFlowTask;
import jnpf.engine.service.*;
import jnpf.exception.WorkFlowException;
import jnpf.permission.model.user.UserAllModel;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 待我审核
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Api(tags = "待我审核", value = "FlowBefore")
@RestController
@RequestMapping("/api/workflow/Engine/FlowBefore")
public class FlowBeforeController {

    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private FlowTaskOperatorService flowTaskOperatorService;
    @Autowired
    private FlowTaskOperatorRecordService flowTaskOperatorRecordService;
    @Autowired
    private FlowTaskNodeService flowTaskNodeService;
    @Autowired
    private FlowEngineService flowEngineService;
    @Autowired
    private UserService userService;
    @Autowired
    private FlowTaskNewService flowTaskNewService;

    /**
     * 获取待我审核列表
     *
     * @param category           分类
     * @param paginationFlowTask
     * @return
     */
    @ApiOperation("获取待我审核列表(有带分页)，1-待办事宜，2-已办事宜，3-抄送事宜")
    @GetMapping("/List/{category}")
    public ActionResult<PageListVO<FlowBeforeListVO>> list(@PathVariable("category") String category, PaginationFlowTask paginationFlowTask) {
        List<FlowTaskEntity> data = new ArrayList<>();
        if (FlowNature.WAIT.equals(category)) {
            data = flowTaskService.getWaitList(paginationFlowTask);
        } else if (FlowNature.TRIAL.equals(category)) {
            data = flowTaskService.getTrialList(paginationFlowTask);
        } else if (FlowNature.CIRCULATE.equals(category)) {
            data = flowTaskService.getCirculateList(paginationFlowTask);
        }
        List<FlowBeforeListVO> listVO = new LinkedList<>();
        if (data.size() > 0) {
            List<FlowEngineEntity> engineList = flowEngineService.getList();
            List<UserAllModel> userList = userService.getAll();
            for (FlowTaskEntity taskEntity : data) {
                //用户名称赋值
                FlowBeforeListVO vo = JsonUtil.getJsonToBean(taskEntity, FlowBeforeListVO.class);
                UserAllModel user = userList.stream().filter(t -> t.getId().equals(taskEntity.getCreatorUserId())).findFirst().orElse(null);
                if (user != null) {
                    vo.setUserName(user.getRealName() + "/" + user.getAccount());
                } else {
                    vo.setUserName("");
                }
                FlowEngineEntity engine = engineList.stream().filter(t -> t.getId().equals(taskEntity.getFlowId())).findFirst().orElse(null);
                if (engine != null) {
                    vo.setFormType(engine.getFormType());
                }
                listVO.add(vo);
            }
        }
        PaginationVO paginationVO = JsonUtil.getJsonToBean(paginationFlowTask, PaginationVO.class);
        return ActionResult.page(listVO, paginationVO);
    }

    /**
     * 获取待我审批信息
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("获取待我审批信息")
    @GetMapping("/{id}")
    public ActionResult<FlowBeforeInfoVO> info(@PathVariable("id") String id, String taskNodeId) throws WorkFlowException {
        FlowBeforeInfoVO vo = flowTaskNewService.getBeforeInfo(id, taskNodeId);
        return ActionResult.success(vo);
    }

    /**
     * 待我审核审核
     *
     * @param id              待办主键值
     * @param flowHandleModel 流程经办
     * @return
     */
    @ApiOperation("待我审核审核")
    @PostMapping("/Audit/{id}")
    public ActionResult audit(@PathVariable("id") String id, @RequestBody FlowHandleModel flowHandleModel) throws WorkFlowException {
        FlowTaskOperatorEntity flowTaskOperatorEntity = flowTaskOperatorService.getInfo(id);
        if (flowTaskOperatorEntity == null) {
            return ActionResult.fail("审核失败");
        } else {
            FlowTaskEntity flowTaskEntity = flowTaskService.getInfo(flowTaskOperatorEntity.getTaskId());
            if (flowTaskOperatorEntity.getCompletion() == 0) {
                FlowModel flowModel = JsonUtil.getJsonToBean(flowHandleModel, FlowModel.class);
                flowTaskNewService.audit(flowTaskEntity, flowTaskOperatorEntity, flowModel);
                return ActionResult.success("审核成功");
            } else {
                return ActionResult.fail("已审核完成");
            }
        }
    }

    /**
     * 待我审核驳回
     *
     * @param id              待办主键值
     * @param flowHandleModel 经办信息
     * @return
     */
    @ApiOperation("待我审核驳回")
    @PostMapping("/Reject/{id}")
    public ActionResult reject(@PathVariable("id") String id, @RequestBody FlowHandleModel flowHandleModel) throws WorkFlowException {
        FlowTaskOperatorEntity flowTaskOperatorEntity = flowTaskOperatorService.getInfo(id);
        if (flowTaskOperatorEntity == null) {
            return ActionResult.fail("驳回失败");
        } else {
            FlowTaskEntity flowTaskEntity = flowTaskService.getInfo(flowTaskOperatorEntity.getTaskId());
            if (flowTaskOperatorEntity.getCompletion() == 0) {
                FlowModel flowModel = JsonUtil.getJsonToBean(flowHandleModel, FlowModel.class);
                flowTaskNewService.reject(flowTaskEntity, flowTaskOperatorEntity, flowModel);
                return ActionResult.success("驳回成功");
            } else {
                return ActionResult.fail("已审核完成");
            }
        }
    }

    /**
     * 待我审核转办
     *
     * @param id              主键值
     * @param flowHandleModel 经办信息
     * @return
     */
    @ApiOperation("待我审核转办")
    @PostMapping("/Transfer/{id}")
    public ActionResult transfer(@PathVariable("id") String id, @RequestBody FlowHandleModel flowHandleModel) {
        FlowTaskOperatorEntity flowTaskOperatorEntity = flowTaskOperatorService.getInfo(id);
        if (flowTaskOperatorEntity == null) {
            return ActionResult.fail("转办失败");
        } else {
            flowTaskOperatorEntity.setHandleId(flowHandleModel.getFreeApproverUserId());
            flowTaskNewService.transfer(flowTaskOperatorEntity);
            return ActionResult.success("转办成功");
        }
    }


    /**
     * 待我审核撤回审核
     * 注意：在撤销流程时要保证你的下一节点没有处理这条记录；如已处理则无法撤销流程。
     *
     * @param id              主键值
     * @param flowHandleModel 实体对象
     * @return
     */
    @ApiOperation("待我审核撤回审核")
    @PostMapping("/Recall/{id}")
    public ActionResult recall(@PathVariable("id") String id, @RequestBody FlowHandleModel flowHandleModel) throws WorkFlowException {
        FlowTaskOperatorRecordEntity operatorRecord = flowTaskOperatorRecordService.getInfo(id);
        List<FlowTaskNodeEntity> nodeList = flowTaskNodeService.getList(operatorRecord.getTaskId()).stream().filter(t -> FlowNodeEnum.Process.getCode().equals(t.getState())).collect(Collectors.toList());
        FlowTaskNodeEntity taskNode = nodeList.stream().filter(t -> t.getId().equals(operatorRecord.getTaskNodeId())).findFirst().orElse(null);
        if (taskNode != null) {
            FlowModel flowModel = JsonUtil.getJsonToBean(flowHandleModel, FlowModel.class);
            flowTaskNewService.recall(id, operatorRecord, flowModel);
            return ActionResult.success("撤回成功");
        }
        return ActionResult.fail("撤回失败");
    }

    /**
     * 待我审核终止审核
     *
     * @param id              主键值
     * @param flowHandleModel 流程经办
     * @return
     */
    @ApiOperation("待我审核终止审核")
    @PostMapping("/Cancel/{id}")
    public ActionResult cancel(@PathVariable("id") String id, @RequestBody FlowHandleModel flowHandleModel) throws WorkFlowException {
        FlowTaskEntity flowTaskEntity = flowTaskService.getInfo(id);
        if (flowTaskEntity != null) {
            FlowModel flowModel = JsonUtil.getJsonToBean(flowHandleModel, FlowModel.class);
            flowTaskNewService.cancel(flowTaskEntity, flowModel);
            return ActionResult.success("终止成功");
        }
        return ActionResult.fail("终止失败，数据不存在");
    }

    /**
     * 指派人
     *
     * @param id              主键值
     * @param flowHandleModel 流程经办
     * @return
     */
    @ApiOperation("指派人")
    @PostMapping("/Assign/{id}")
    public ActionResult assign(@PathVariable("id") String id, @RequestBody FlowHandleModel flowHandleModel) {
        boolean isOk = flowTaskNewService.assign(id,flowHandleModel);
        return isOk ? ActionResult.success("指派成功") : ActionResult.fail("指派失败");
    }


}
