package jnpf.portal.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.vo.ListVO;
import jnpf.engine.service.FlowDelegateService;
import jnpf.engine.service.FlowTaskService;
import jnpf.message.service.MessageService;
import jnpf.model.home.*;
import jnpf.service.EmailReceiveService;
import jnpf.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 主页控制器
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Api(tags = "主页控制器", value = "Home")
@RestController
@RequestMapping("api/visualdev/Dashboard")
public class DashboardController {
    @Autowired
    private FlowTaskService flowTaskService;

    @Autowired
    private FlowDelegateService flowDelegateService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private EmailReceiveService emailReceiveService;

    /**
     * 获取我的待办
     *
     * @return
     */
    @ApiOperation("获取我的待办")
    @GetMapping("/FlowTodoCount")
    public ActionResult<FlowTodoCountVO> getFlowTodoCount() {
        FlowTodoCountVO vo = new FlowTodoCountVO();
        vo.setToBeReviewed(flowTaskService.getWaitList().size());
        vo.setEntrust(flowDelegateService.getList().size());
        vo.setFlowDone(flowTaskService.getTrialList().size());
        return ActionResult.success(vo);
    }

    /**
     * 获取通知公告
     *
     * @return
     */
    @ApiOperation("获取通知公告")
    @GetMapping("/Notice")
    public ActionResult<NoticeVO> getNotice() {
        List<NoticeVO> list= JsonUtil.getJsonToList(messageService.getNoticeList(),NoticeVO.class);

        ListVO<NoticeVO> voList = new ListVO();
        voList.setList(list);
        return ActionResult.success(voList);
    }

    /**
     * 获取未读邮件
     *
     * @return
     */
    @ApiOperation("获取未读邮件")
    @GetMapping("/Email")
    public ActionResult<EmailVO> getEmail() {
        List<EmailVO> list = JsonUtil.getJsonToList(emailReceiveService.getReceiveList(),EmailVO.class);
        ListVO<EmailVO> voList = new ListVO();
        voList.setList(list);
        return ActionResult.success(voList);
    }

    /**
     * 获取待办事项
     *
     * @return
     */
    @ApiOperation("获取待办事项")
    @GetMapping("/FlowTodo")
    public ActionResult<FlowTodoVO> getFlowTodo() {
        List<FlowTodoVO> list = JsonUtil.getJsonToList(flowTaskService.getAllWaitList(),FlowTodoVO.class);
        ListVO<FlowTodoVO> voList = new ListVO();
        voList.setList(list);
        return ActionResult.success(voList);
    }

    /**
     * 获取我的待办事项
     *
     * @return
     */
    @ApiOperation("获取我的待办事项")
    @GetMapping("/MyFlowTodo")
    public ActionResult<MyFlowTodoVO> getMyFlowTodo() {
        List<MyFlowTodoVO> list = JsonUtil.getJsonToList(flowTaskService.getWaitList(), MyFlowTodoVO.class);
        ListVO<MyFlowTodoVO> voList = new ListVO();
        voList.setList(list);
        return ActionResult.success(voList);
    }
}
