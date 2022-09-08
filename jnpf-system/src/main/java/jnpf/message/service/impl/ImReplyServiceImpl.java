package jnpf.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.message.entity.ImReplyEntity;
import jnpf.message.mapper.ImReplyMapper;
import jnpf.message.model.ImReplyListModel;
import jnpf.message.service.ImReplyService;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-05-29
 */
@Service
public class ImReplyServiceImpl extends ServiceImpl<ImReplyMapper, ImReplyEntity> implements ImReplyService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private UserService userService;

    @Override
    public List<ImReplyEntity> getList() {
        QueryWrapper<ImReplyEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ImReplyEntity::getUserId, userProvider.get().getUserId()).or()
                .eq(ImReplyEntity::getReceiveUserId, userProvider.get().getUserId())
                .orderByDesc(ImReplyEntity::getUserId);
        return this.list();
    }

    @Override
    public boolean savaImReply(ImReplyEntity entity) {
        QueryWrapper<ImReplyEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ImReplyEntity::getUserId, entity.getUserId()).eq(ImReplyEntity::getReceiveUserId, entity.getReceiveUserId());
        //判断数据是否存在
        ImReplyEntity imReplyEntity = this.getOne(queryWrapper);
        if (imReplyEntity != null) {
            entity.setId(imReplyEntity.getId());
            this.updateById(entity);
            return true;
        }
        this.save(entity);
        return true;
    }

    @Override
    public List<ImReplyListModel> getImReplyList() {
        List<ImReplyListModel> imReplyList = this.baseMapper.getImReplyList();
        //我发给别人
        List<ImReplyListModel> collect = imReplyList.stream().filter(t -> t.getUserId().equals(userProvider.get().getUserId())).collect(Collectors.toList());
        //头像替换成对方的
        for (ImReplyListModel imReplyListModel : collect) {
            UserEntity entity = userService.getInfo(imReplyListModel.getId());
            imReplyListModel.setHeadIcon(entity.getHeadIcon()!=null?entity.getHeadIcon():"");
        }
        //别人发给我
        List<ImReplyListModel> list = imReplyList.stream().filter(t -> t.getId().equals(userProvider.get().getUserId())).collect(Collectors.toList());
        for (ImReplyListModel model : list) {
            //移除掉互发的
            List<ImReplyListModel> collect1 = collect.stream().filter(t -> t.getId().equals(model.getUserId())).collect(Collectors.toList());
            if (collect1.size() > 0) {
                //判断我发给别人的时间和接收的时间大小
                //接收的大于发送的
                if (model.getLatestDate().getTime() > collect1.get(0).getLatestDate().getTime()) {
                    collect.remove(collect1.get(0));
                } else { //发送的大于接收的则跳过
                    continue;
                }
            }
            ImReplyListModel imReplyListModel = new ImReplyListModel();
            UserEntity entity = userService.getInfo(model.getUserId());
            imReplyListModel.setHeadIcon(entity.getHeadIcon());
            imReplyListModel.setUserId(userProvider.get().getUserId());
            imReplyListModel.setId(entity.getId());
            imReplyListModel.setLatestDate(model.getLatestDate());
            imReplyListModel.setLatestMessage(model.getLatestMessage());
            imReplyListModel.setMessageType(model.getMessageType());
            collect.add(imReplyListModel);
        }
        return collect;
    }
}
