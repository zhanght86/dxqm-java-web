package jnpf.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.PageModel;
import jnpf.message.entity.ImContentEntity;
import jnpf.message.entity.ImReplyEntity;
import jnpf.message.mapper.ImContentMapper;
import jnpf.message.model.ImReplySavaModel;
import jnpf.message.service.ImContentService;
import jnpf.message.model.ImUnreadNumModel;
import jnpf.message.service.ImReplyService;
import jnpf.util.DateUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 聊天内容
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Service
public class ImContentServiceImpl extends ServiceImpl<ImContentMapper, ImContentEntity> implements ImContentService {

    @Autowired
    private ImReplyService imReplyService;

    @Override
    public List<ImContentEntity> getMessageList(String sendUserId, String receiveUserId, PageModel pageModel) {
        QueryWrapper<ImContentEntity> queryWrapper = new QueryWrapper<>();
        //发件人、收件人
        if (!StringUtil.isEmpty(sendUserId) && !StringUtil.isEmpty(receiveUserId)) {
            queryWrapper.lambda().and(wrapper -> {
                wrapper.eq(ImContentEntity::getSendUserId, sendUserId);
                wrapper.eq(ImContentEntity::getReceiveUserId, receiveUserId);
                wrapper.or().eq(ImContentEntity::getSendUserId, receiveUserId);
                wrapper.eq(ImContentEntity::getReceiveUserId, sendUserId);
            });
        }
        //关键字查询
        if (pageModel != null && pageModel.getKeyword() != null) {
            queryWrapper.lambda().like(ImContentEntity::getContent, pageModel.getKeyword());
        }
        //排序
        pageModel.setSidx("F_SendTime");
        if (StringUtil.isEmpty(pageModel.getSidx())) {
            queryWrapper.lambda().orderByDesc(ImContentEntity::getSendTime);
        } else {
            queryWrapper = "asc".equals(pageModel.getSord().toLowerCase()) ? queryWrapper.orderByAsc(pageModel.getSidx()) : queryWrapper.orderByDesc(pageModel.getSidx());
        }
        Page<ImContentEntity> page = new Page<>(pageModel.getPage(), pageModel.getRows());
        IPage<ImContentEntity> iPage = this.page(page, queryWrapper);
        return pageModel.setData(iPage.getRecords(), page.getTotal());
    }

    @Override
    public List<ImUnreadNumModel> getUnreadList(String receiveUserId) {
        List<ImUnreadNumModel> list = this.baseMapper.getUnreadList(receiveUserId);
        List<ImUnreadNumModel> list1 = this.baseMapper.getUnreadLists(receiveUserId);
        for (ImUnreadNumModel item : list) {
            ImUnreadNumModel defaultItem = list1.stream().filter(q -> q.getSendUserId().equals(item.getSendUserId())).findFirst().get();
            item.setDefaultMessage(defaultItem.getDefaultMessage());
            item.setDefaultMessageType(defaultItem.getDefaultMessageType());
            item.setDefaultMessageTime(defaultItem.getDefaultMessageTime());
        }
        return list;
    }

    @Override
    public int getUnreadCount(String sendUserId, String receiveUserId) {
        QueryWrapper<ImContentEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ImContentEntity::getSendUserId, sendUserId).eq(ImContentEntity::getReceiveUserId, receiveUserId).eq(ImContentEntity::getState, 0);
        return this.count(queryWrapper);
    }

    @Override
    @Transactional
    public void sendMessage(String sendUserId, String receiveUserId, String message, String messageType) {
        ImContentEntity entity = new ImContentEntity();
        entity.setId(RandomUtil.uuId());
        entity.setSendUserId(sendUserId);
        entity.setSendTime(new Date());
        entity.setReceiveUserId(receiveUserId);
        entity.setState(0);
        entity.setContent(message);
        entity.setContentType(messageType);
        this.save(entity);

        //写入到会话表中
        ImReplySavaModel imReplySavaModel = new ImReplySavaModel(sendUserId, receiveUserId, entity.getSendTime());
        ImReplyEntity imReplyEntity = JsonUtil.getJsonToBean(imReplySavaModel, ImReplyEntity.class);
        imReplyService.savaImReply(imReplyEntity);
    }

    @Override
    public void readMessage(String sendUserId, String receiveUserId) {
        QueryWrapper<ImContentEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ImContentEntity::getSendUserId, sendUserId);
        queryWrapper.lambda().eq(ImContentEntity::getReceiveUserId, receiveUserId);
        queryWrapper.lambda().eq(ImContentEntity::getState, 0);
        List<ImContentEntity> list = this.list(queryWrapper);
        for (ImContentEntity entity : list) {
            entity.setState(1);
            entity.setReceiveTime(new Date());
            this.updateById(entity);
        }
    }

//    @Override
//    public ImContentEntity getList(String userId, String receiveUserId) {
//        QueryWrapper<ImContentEntity> queryWrapper = new QueryWrapper<>();
//        queryWrapper.lambda().eq(ImContentEntity::getSendUserId, userId)
//                .and(t -> t.eq(ImContentEntity::getReceiveUserId, receiveUserId)).orderByDesc(ImContentEntity::getReceiveTime);
//        List<ImContentEntity> list = this.list(queryWrapper);
//        return list.size() > 0 ? list.get(0) : null;
//    }
}
