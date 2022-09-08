package jnpf.message.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.Pagination;
import jnpf.base.UserInfo;
import jnpf.config.ConfigValueUtil;
import jnpf.database.data.DataSourceContextHolder;
import jnpf.database.model.DataSourceUtil;
import jnpf.database.util.JdbcUtil;
import jnpf.message.entity.MessageEntity;
import jnpf.message.entity.MessageReceiveEntity;
import jnpf.message.mapper.MessageMapper;
import jnpf.message.service.MessageService;
import jnpf.message.service.MessagereceiveService;
import jnpf.model.OnlineUserModel;
import jnpf.model.OnlineUserProvider;
import jnpf.util.*;
import lombok.Cleanup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 消息实例
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, MessageEntity> implements MessageService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private DataSourceUtil dataSourceUtils;
    @Autowired
    private MessagereceiveService messagereceiveService;

    @Override
    public List<MessageEntity> getNoticeList(Pagination pagination) {
        QueryWrapper<MessageEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MessageEntity::getType, 1);
        //关键词（消息标题）
        if (!StringUtil.isEmpty(pagination.getKeyword())) {
            queryWrapper.lambda().like(MessageEntity::getTitle, pagination.getKeyword());
        }
        //默认排序
        queryWrapper.lambda().orderByDesc(MessageEntity::getCreatorTime);
        Page<MessageEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
        IPage<MessageEntity> iPage = this.page(page, queryWrapper);
        return pagination.setData(iPage.getRecords(), page.getTotal());
    }

    @Override
    public List<MessageEntity> getNoticeList() {
        QueryWrapper<MessageEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MessageEntity::getType, 1);

        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<MessageEntity> getMessageList(Pagination pagination, String type) {
        String userId = userProvider.get().getUserId();
        Map<String, String> map = new HashMap<>(16);
        map.put("userId", userId);
        StringBuilder sql = new StringBuilder();
        //关键词（消息标题）
        if (!StringUtil.isEmpty(pagination.getKeyword())) {
            sql.append(" AND m.F_Title like '%" + pagination.getKeyword() + "%' ");
        }
        //消息类别
        if (!StringUtil.isEmpty(type)) {
            sql.append(" AND m.F_Type = '" + type + "'");
        }
        sql.append(" ORDER BY  F_LastModifyTime desc");
        map.put("sql", sql.toString());
        List<MessageEntity> lists = this.baseMapper.getMessageList(map);
        return pagination.setData(PageUtil.getListPage((int) pagination.getCurrentPage(), (int) pagination.getPageSize(), lists), lists.size());
    }

    @Override
    public List<MessageEntity> getMessageList(Pagination pagination) {
        return this.getMessageList(pagination, null);
    }

    @Override
    public MessageEntity getinfo(String id) {
        QueryWrapper<MessageEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MessageEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public MessageEntity getInfoDefault(int type) {
        List<MessageEntity> list = this.baseMapper.getInfoDefault(type);
        if (type == 1) {
            list = list.stream().filter(t -> t.getEnabledMark()!=null && t.getEnabledMark() == 1).collect(Collectors.toList());
        }
        MessageEntity entity = new MessageEntity();
        if (list.size() > 0) {
            entity = list.get(0);
        }
        return entity;
    }

    @Override
    @Transactional
    public void delete(MessageEntity entity) {
        this.removeById(entity.getId());
        QueryWrapper<MessageReceiveEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MessageReceiveEntity::getMessageId, entity.getId());
        messagereceiveService.remove(queryWrapper);
    }

    @Override
    public void create(MessageEntity entity) {
        entity.setId(RandomUtil.uuId());
        entity.setBodyText(entity.getBodyText());
        entity.setType(1);
        entity.setEnabledMark(0);
        entity.setCreatorUser(userProvider.get().getUserId());
        this.save(entity);
    }

    @Override
    public boolean update(String id, MessageEntity entity) {
        entity.setId(id);
        entity.setBodyText(entity.getBodyText());
        entity.setCreatorUser(userProvider.get().getUserId());
        return this.updateById(entity);
    }

    @Override
    public void messageRead(String messageId) {
        String userId = userProvider.get().getUserId();
        QueryWrapper<MessageReceiveEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MessageReceiveEntity::getUserId, userId).eq(MessageReceiveEntity::getMessageId, messageId);
        MessageReceiveEntity entity = messagereceiveService.getOne(queryWrapper);
        if (entity != null) {
            entity.setIsRead(1);
            entity.setReadCount(entity.getReadCount() == null ? 1 : entity.getReadCount() + 1);
            entity.setReadTime(new Date());
            messagereceiveService.updateById(entity);
        }
    }

    @Override
    @Transactional
    public void messageRead() {
        String userId = userProvider.get().getUserId();
        QueryWrapper<MessageReceiveEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MessageReceiveEntity::getUserId, userId).eq(MessageReceiveEntity::getIsRead, 0);
        List<MessageReceiveEntity> entitys = messagereceiveService.list(queryWrapper);
        if (entitys.size() > 0) {
            for (MessageReceiveEntity entity : entitys) {
                entity.setIsRead(1);
                entity.setReadCount(entity.getReadCount() == null ? 1 : entity.getReadCount() + 1);
                entity.setReadTime(new Date());
                messagereceiveService.updateById(entity);
            }
        }
    }

    @Override
    @Transactional
    public void deleteRecord(List<String> messageIds) {
        String userId = userProvider.get().getUserId();
        QueryWrapper<MessageReceiveEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MessageReceiveEntity::getUserId, userId).in(MessageReceiveEntity::getMessageId, messageIds);
        messagereceiveService.remove(queryWrapper);
    }

    @Override
    public int getUnreadCount(String userId) {
        QueryWrapper<MessageReceiveEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MessageReceiveEntity::getUserId, userId).eq(MessageReceiveEntity::getIsRead, 0);
        return messagereceiveService.count(queryWrapper);
    }

    @Override
    public int getUnreadNoticeCount(String userId) {
        int result = this.baseMapper.getUnreadNoticeCount(userId);
        return result;
    }

    @Override
    public int getUnreadMessageCount(String userId) {
        int result = this.baseMapper.getUnreadMessageCount(userId);
        return result;
    }

    @Override
    @Transactional
    public void sentNotice(List<String> toUserIds, MessageEntity entity) {
        UserInfo userInfo = userProvider.get();
        entity.setEnabledMark(1);
        entity.setLastModifyTime(new Date());
        entity.setLastModifyUserId(userInfo.getUserId());
        this.updateById(entity);
        try {
            String dbName = "true".equals(configValueUtil.getMultiTenancy())?
                    userProvider.get().getTenantDbConnectionString():dataSourceUtils.getDbName();
            @Cleanup Connection conn = JdbcUtil.getConn(dataSourceUtils,dbName);
            @Cleanup PreparedStatement pstm = null;
            String sql = "INSERT INTO base_messagereceive(F_ID,F_MESSAGEID,F_USERID,F_ISREAD)  VALUES (?,?,?,?)";
            pstm = conn.prepareStatement(sql);
            conn.setAutoCommit(false);
            for (int i = 0; i < toUserIds.size(); i++) {
                pstm.setString(1, RandomUtil.uuId());
                pstm.setString(2, entity.getId());
                pstm.setString(3, toUserIds.get(i));
                pstm.setInt(4, 0);
                pstm.addBatch();
            }
            pstm.executeBatch();
            conn.commit();
            pstm.close();
            conn.close();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        //消息推送 - PC端
        for (int i = 0; i < toUserIds.size(); i++) {
            for (OnlineUserModel item : OnlineUserProvider.getOnlineUserList()) {
                if (toUserIds.get(i).equals(item.getUserId()) && userInfo.getTenantId().equals(item.getTenantId())) {
                    JSONObject map = new JSONObject();
                    map.put("method", "messagePush");
                    map.put("unreadNoticeCount", 1);
                    map.put("messageType", 1);
                    map.put("userId", userInfo.getTenantId());
                    map.put("toUserId", toUserIds);
                    map.put("title", entity.getTitle());
                    synchronized (map) {
                        item.getWebSocket().getAsyncRemote().sendText(map.toJSONString());
                    }
                }
            }
        }
    }

    @Override
    public void sentMessage(List<String> toUserIds, String title) {
        this.sentMessage(toUserIds, title, null);
    }

    @Override
    @Transactional
    public void sentMessage(List<String> toUserIds, String title, String bodyText) {
        UserInfo userInfo = userProvider.get();
        MessageEntity entity = new MessageEntity();
        entity.setTitle(title);
        entity.setBodyText(bodyText);
        entity.setId(RandomUtil.uuId());
        entity.setType(2);
        entity.setCreatorUser(userInfo.getUserId());
        entity.setCreatorTime(new Date());
        entity.setLastModifyTime(entity.getCreatorTime());
        entity.setLastModifyUserId(entity.getCreatorUser());
        List<MessageReceiveEntity> receiveEntityList = new ArrayList<>();
        for (String item : toUserIds) {
            MessageReceiveEntity messageReceiveEntity = new MessageReceiveEntity();
            messageReceiveEntity.setId(RandomUtil.uuId());
            messageReceiveEntity.setMessageId(entity.getId());
            messageReceiveEntity.setUserId(item);
            messageReceiveEntity.setIsRead(0);
            receiveEntityList.add(messageReceiveEntity);
        }
        this.save(entity);
        for (MessageReceiveEntity messageReceiveEntity : receiveEntityList) {
            messagereceiveService.save(messageReceiveEntity);
        }
        //消息推送 - PC端
        for (int i = 0; i < toUserIds.size(); i++) {
            for (OnlineUserModel item : OnlineUserProvider.getOnlineUserList()) {
                if (toUserIds.get(i).equals(item.getUserId()) && userInfo.getTenantId().equals(item.getTenantId())) {
                    JSONObject map = new JSONObject();
                    map.put("method", "messagePush");
                    map.put("unreadNoticeCount", 1);
                    map.put("messageType", 2);
                    map.put("userId", userInfo.getTenantId());
                    map.put("toUserId", toUserIds);
                    map.put("title", entity.getTitle());
                    synchronized (map) {
                        item.getWebSocket().getAsyncRemote().sendText(map.toJSONString());
                    }
                }
            }
        }
    }
}
