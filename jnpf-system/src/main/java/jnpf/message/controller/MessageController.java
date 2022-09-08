package jnpf.message.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.vo.PageListVO;
import jnpf.base.Pagination;
import jnpf.base.vo.PaginationVO;
import jnpf.message.entity.MessageEntity;
import jnpf.database.exception.DataException;
import jnpf.message.service.MessageService;
import jnpf.message.model.message.*;
import jnpf.permission.model.user.UserAllModel;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统公告
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Api(tags = "系统公告", value = "Message")
@RestController
@RequestMapping("/api/message")
public class  MessageController {

    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;

    /**
     * 列表（通知公告）
     *
     * @param pagination
     * @return
     */
    @ApiOperation("获取系统公告列表（带分页）")
    @GetMapping("/Notice")
    public ActionResult<PageListVO<MessageNoticeVO>> noticeList(Pagination pagination) {
        List<MessageEntity> list = messageService.getNoticeList(pagination);
        List<UserAllModel> data = userService.getAll();
        for (MessageEntity entity : list) {
            for (UserAllModel userAllVO : data) {
                if (userAllVO.getId().equals(entity.getCreatorUser())) {
                    entity.setCreatorUser(userAllVO.getRealName() + "/" + userAllVO.getAccount());
                }
            }
        }
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        List<MessageNoticeVO> voList = JsonUtil.getJsonToList(list, MessageNoticeVO.class);
        return ActionResult.page(voList, paginationVO);
    }

    /**
     * 添加系统公告
     *
     * @param noticeCrForm 实体对象
     * @return
     */
    @ApiOperation("添加系统公告")
    @PostMapping
    public ActionResult create(@RequestBody @Valid NoticeCrForm noticeCrForm) {
        MessageEntity entity = JsonUtil.getJsonToBean(noticeCrForm, MessageEntity.class);
        messageService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 修改系统公告
     *
     * @param id            主键值
     * @param messageUpForm 实体对象
     * @return
     */
    @ApiOperation("修改系统公告")
    @PutMapping("/{id}")
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid NoticeUpForm messageUpForm) {
        MessageEntity entity = JsonUtil.getJsonToBean(messageUpForm, MessageEntity.class);
        boolean flag = messageService.update(id, entity);
        if (flag == false) {
            return ActionResult.fail("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");
    }

    /**
     * 信息
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("获取/查看系统公告信息")
    @GetMapping("/{id}")
    public ActionResult<NoticeInfoVO> info(@PathVariable("id") String id) throws DataException {
        MessageEntity entity = messageService.getinfo(id);
        List<UserAllModel> data = userService.getAll();
        for (UserAllModel userAllVO : data) {
            if (userAllVO.getId().equals(entity.getCreatorUser())) {
                entity.setCreatorUser(userAllVO.getRealName() + "/" + userAllVO.getAccount());
            }
        }
        NoticeInfoVO vo = JsonUtilEx.getJsonToBeanEx(entity, NoticeInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 删除
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("删除系统公告")
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable("id") String id) {
        MessageEntity entity = messageService.getinfo(id);
        if (entity != null) {
            messageService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }


    /**
     * 发布公告
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("发布系统公告")
    @PutMapping("/{id}/Actions/Release")
    public ActionResult release(@PathVariable("id") String id) {
        MessageEntity entity = messageService.getinfo(id);
        if (entity != null) {
            List<UserAllModel> userList = userService.getAll();
            List<String> userIds = userList.stream().map(u -> u.getId()).collect(Collectors.toList());
            entity.setToUserIds(String.join(",", userIds));
            messageService.sentNotice(userIds, entity);
            return ActionResult.success("发布成功");
        }
        return ActionResult.fail("发布失败");
    }
//=======================================站内消息、消息中心=================================================


    /**
     * 获取消息中心列表
     *
     * @param pagination
     * @return
     */
    @ApiOperation("列表（通知公告/系统消息/私信消息）")
    @GetMapping
    public ActionResult<PageListVO<MessageInfoVO>> messageList(PaginationMessage pagination) {
        List<MessageEntity> list = messageService.getMessageList(pagination, pagination.getType());
        List<UserAllModel> data = userService.getAll();
        for (MessageEntity entity : list) {
            for (UserAllModel userAllVO : data) {
                if (userAllVO.getId().equals(entity.getCreatorUser())) {
                    entity.setCreatorUser(userAllVO.getRealName() + "/" + userAllVO.getAccount());
                }
            }
        }
        List<MessageInfoVO> listVO = JsonUtil.getJsonToList(list, MessageInfoVO.class);
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(listVO, paginationVO);
    }


    /**
     * 读取消息
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("读取消息")
    @GetMapping("/ReadInfo/{id}")
    public ActionResult readInfo(@PathVariable("id") String id) throws DataException {
        MessageEntity entity = messageService.getinfo(id);
        if (entity != null) {
            messageService.messageRead(id);
        }
        List<UserAllModel> data = userService.getAll();
        for (UserAllModel userAllVO : data) {
            if (userAllVO.getId().equals(entity.getCreatorUser())) {
                entity.setCreatorUser(userAllVO.getRealName() + "/" + userAllVO.getAccount());
            }
        }
        NoticeInfoVO vo = JsonUtilEx.getJsonToBeanEx(entity, NoticeInfoVO.class);
        return ActionResult.success(vo);
    }


    /**
     * 全部已读
     *
     * @return
     */
    @ApiOperation("全部已读")
    @PostMapping("/Actions/ReadAll")
    public ActionResult allRead() {
        messageService.messageRead();
        return ActionResult.success("已读成功");
    }

    /**
     * 删除记录
     *
     * @return
     */
    @ApiOperation("删除消息")
    @DeleteMapping("/Record")
    public ActionResult deleteRecord(@RequestBody MessageRecordForm recordForm) {
        String[] id = recordForm.getIds().split(",");
        List<String> list = Arrays.asList(id);
        messageService.deleteRecord(list);
        return ActionResult.success("删除成功");
    }
}
