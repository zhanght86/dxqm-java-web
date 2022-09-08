package jnpf.engine.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.engine.enums.FlowStatusEnum;
import jnpf.database.exception.DataException;
import jnpf.exception.WorkFlowException;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.model.flowtask.FlowTaskForm;
import jnpf.engine.model.flowtask.FlowTaskInfoVO;
import jnpf.engine.service.FlowDynamicService;
import jnpf.engine.service.FlowTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.Map;

/**
 * 流程引擎
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Api(tags = "流程引擎", value = "FlowTask")
@RestController
@RequestMapping("/api/workflow/Engine/FlowTask")
public class FlowTaskController {

    @Autowired
    private FlowDynamicService flowDynamicService;
    @Autowired
    private FlowTaskService flowTaskService;

    /**
     * 动态表单信息
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("动态表单信息")
    @GetMapping("/{id}")
    public ActionResult<FlowTaskInfoVO> info(@PathVariable("id") String id) throws WorkFlowException {
        FlowTaskEntity entity = flowTaskService.getInfo(id);
        FlowTaskInfoVO vo = flowDynamicService.info(entity);
        return ActionResult.success(vo);
    }

    /**
     * 保存
     *
     * @param flowTaskForm 动态表单
     * @return
     */
    @ApiOperation("保存")
    @PostMapping
    public ActionResult save(@RequestBody FlowTaskForm flowTaskForm) throws WorkFlowException {
        if (FlowStatusEnum.save.getMessage().equals(flowTaskForm.getStatus())) {
            flowDynamicService.save(null, flowTaskForm.getFlowId(), flowTaskForm.getData());
            return ActionResult.success("保存成功");
        }
        flowDynamicService.submit(null, flowTaskForm.getFlowId(), flowTaskForm.getData(), flowTaskForm.getFreeApproverUserId());
        return ActionResult.success("提交成功，请耐心等待");
    }

    /**
     * 提交
     *
     * @param flowTaskForm 动态表单
     * @return
     */
    @ApiOperation("提交")
    @PutMapping("/{id}")
    public ActionResult submit(@RequestBody FlowTaskForm flowTaskForm, @PathVariable("id") String id) throws WorkFlowException {
        if (FlowStatusEnum.save.getMessage().equals(flowTaskForm.getStatus())) {
            flowDynamicService.save(id, flowTaskForm.getFlowId(), flowTaskForm.getData());
            return ActionResult.success("保存成功");
        }
        flowDynamicService.submit(id, flowTaskForm.getFlowId(), flowTaskForm.getData(), flowTaskForm.getFreeApproverUserId());
        return ActionResult.success("提交成功，请耐心等待");
    }

    /**
     * 动态表单详情
     *
     * @param flowId 引擎主键值
     * @param id     主键值
     * @return
     */
    @ApiOperation("动态表单信息")
    @GetMapping("/{flowId}/{id}")
    public ActionResult<FlowTaskInfoVO> info(@PathVariable("flowId") String flowId, @PathVariable("id") String id) throws WorkFlowException {
        Map<String, Object> data = flowDynamicService.getData(flowId, id);
        return ActionResult.success(data);
    }

}
