package jnpf.engine.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.util.FlowNature;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.engine.entity.FlowEngineEntity;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.entity.FlowTaskOperatorEntity;
import jnpf.engine.enums.FlowMessageEnum;
import jnpf.engine.model.FlowHandleModel;
import jnpf.engine.model.flowengine.FlowModel;
import jnpf.engine.model.flowlaunch.FlowLaunchListVO;
import jnpf.engine.model.flowtask.PaginationFlowTask;
import jnpf.engine.service.*;
import jnpf.exception.WorkFlowException;
import jnpf.message.service.MessageService;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 流程发起
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Api(tags = "流程发起", value = "FlowLaunch")
@RestController
@RequestMapping("/api/workflow/Engine/FlowLaunch")
public class FlowLaunchController {

    @Autowired
    private FlowEngineService flowEngineService;
    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private FlowTaskOperatorService flowTaskOperatorService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private FlowTaskNewService flowTaskNewService;

    /**
     * 获取流程发起列表
     *
     * @param paginationFlowTask
     * @return
     */
    @ApiOperation("获取流程发起列表(带分页)")
    @GetMapping
    public ActionResult<PageListVO<FlowLaunchListVO>> list(PaginationFlowTask paginationFlowTask) {
        List<FlowEngineEntity> engineList = flowEngineService.getList();
        List<FlowTaskEntity> data = flowTaskService.getLaunchList(paginationFlowTask);
        List<FlowLaunchListVO> listVO = new LinkedList<>();
        for (FlowTaskEntity taskEntity : data) {
            //用户名称赋值
            FlowLaunchListVO vo = JsonUtil.getJsonToBean(taskEntity, FlowLaunchListVO.class);
            FlowEngineEntity entity = engineList.stream().filter(t -> t.getId().equals(taskEntity.getFlowId())).findFirst().orElse(null);
            if (entity != null) {
                vo.setFormData(entity.getFormData());
                vo.setFormType(entity.getFormType());
            }
            listVO.add(vo);
        }
        PaginationVO paginationVO = JsonUtil.getJsonToBean(paginationFlowTask, PaginationVO.class);
        return ActionResult.page(listVO, paginationVO);
    }

    /**
     * 删除流程发起
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("删除流程发起")
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable("id") String id) throws WorkFlowException {
        FlowTaskEntity entity = flowTaskService.getInfo(id);
        if (entity != null) {
            if (entity.getFlowType() == 1) {
                return ActionResult.fail("功能流程不能删除");
            }
            if (!FlowNature.ParentId.equals(entity.getParentId()) && StringUtil.isNotEmpty(entity.getParentId())) {
                return ActionResult.fail("子表数据不能删除");
            }
            flowTaskService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }

    /**
     * 待我审核催办
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("发起催办")
    @PostMapping("/Press/{id}")
    public ActionResult press(@PathVariable("id") String id) throws WorkFlowException {
        FlowTaskEntity flowTaskEntity = flowTaskService.getInfo(id);
        //单前流程节点
        String[] stepId = flowTaskEntity.getThisStepId().split(",");
        List<FlowTaskOperatorEntity> press = flowTaskOperatorService.press(stepId, id);
        List<String> processId = press.stream().map(t -> t.getHandleId()).distinct().collect(Collectors.toList());
        if (processId.size() > 0) {
            Map<String, Object> message = new HashMap<>(16);
            message.put("type", FlowMessageEnum.wait.getCode());
            message.put("id", processId);
            String bodyText = JsonUtilEx.getObjectToString(message);
            messageService.sentMessage(processId, flowTaskEntity.getFullName() + "【催办】", bodyText);
            return ActionResult.success("催办成功");
        }
        return ActionResult.fail("未找到催办人");
    }

    /**
     * 撤回流程发起
     * 注意：在撤销流程时要保证你的下一节点没有处理这条记录；如已处理则无法撤销流程。
     *
     * @param id              主键值
     * @param flowHandleModel 经办记录
     * @return
     */
    @ApiOperation("撤回流程发起")
    @PutMapping("/{id}/Actions/Withdraw")
    public ActionResult revoke(@PathVariable("id") String id, @RequestBody FlowHandleModel flowHandleModel) throws WorkFlowException {
        FlowTaskEntity flowTaskEntity = flowTaskService.getInfo(id);
        FlowModel flowModel = JsonUtil.getJsonToBean(flowHandleModel, FlowModel.class);
        flowTaskNewService.revoke(flowTaskEntity, flowModel);
        return ActionResult.success("撤回成功");
    }
}
