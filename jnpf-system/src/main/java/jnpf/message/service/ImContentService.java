package jnpf.message.service;


import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.base.PageModel;
import jnpf.message.entity.ImContentEntity;
import jnpf.message.model.ImUnreadNumModel;

import java.util.List;

/**
 * 聊天内容
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
public interface ImContentService extends IService<ImContentEntity> {

    /**
     * 获取消息列表
     *
     * @param sendUserId    发送者
     * @param receiveUserId 接收者
     * @param pageModel
     * @return
     */
    List<ImContentEntity> getMessageList(String sendUserId, String receiveUserId, PageModel pageModel);

    /**
     * 获取未读消息
     *
     * @param receiveUserId 接收者
     * @return
     */
    List<ImUnreadNumModel> getUnreadList(String receiveUserId);

    /**
     * 获取未读消息
     *
     * @param receiveUserId 接收者
     * @return
     */
    int getUnreadCount(String sendUserId, String receiveUserId);

    /**
     * 发送消息
     *
     * @param sendUserId    发送者
     * @param receiveUserId 接收者
     * @param message       消息内容
     * @param messageType   消息类型
     * @return
     */
    void sendMessage(String sendUserId, String receiveUserId, String message, String messageType);

    /**
     * 已读消息
     *
     * @param sendUserId    发送者
     * @param receiveUserId 接收者
     * @return
     */
    void readMessage(String sendUserId, String receiveUserId);

}
