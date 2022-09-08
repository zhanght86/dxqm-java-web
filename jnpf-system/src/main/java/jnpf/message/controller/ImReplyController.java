package jnpf.message.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.vo.ListVO;
import jnpf.message.model.ImReplyListModel;
import jnpf.message.model.ImReplyListVo;
import jnpf.message.service.ImContentService;
import jnpf.message.service.ImReplyService;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import jnpf.util.UploaderUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息会话接口
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-05-29
 */
@Api(tags = "消息会话接口", value = "imreply")
@RestController
@RequestMapping("/api/message/imreply")
public class ImReplyController {
    @Autowired
    private ImReplyService imReplyService;
    @Autowired
    private ImContentService imContentService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserProvider userProvider;

    /**
     * 获取消息会话列表
     *
     * @return
     */
    @ApiOperation("获取消息会话列表")
    @GetMapping
    public ActionResult getList() {
        List<ImReplyListModel> imReplyList = imReplyService.getImReplyList();
        for (ImReplyListModel vo : imReplyList) {
            //头像路径拼接
            vo.setHeadIcon(UploaderUtil.uploaderImg(vo.getHeadIcon()));
            //获取未读消息
            vo.setUnreadMessage(imContentService.getUnreadCount(vo.getId(), userProvider.get().getUserId()));
            UserEntity entity = userService.getInfo(vo.getId());
            if (entity != null) {
                //拼接账号和名称
                vo.setRealName(entity.getRealName());
                vo.setAccount(entity.getAccount());
            }
        }
        //排序
        imReplyList = imReplyList.stream().sorted(Comparator.comparing(ImReplyListModel::getLatestDate).reversed())
                .collect(Collectors.toList());
        List<ImReplyListVo> imReplyListVoList = JsonUtil.getJsonToList(imReplyList, ImReplyListVo.class);
        ListVO listVO = new ListVO();
        listVO.setList(imReplyListVoList);
        return ActionResult.success(listVO);
    }

}
