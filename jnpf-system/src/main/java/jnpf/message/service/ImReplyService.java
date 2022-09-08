package jnpf.message.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.message.entity.ImReplyEntity;
import jnpf.message.model.ImReplyListModel;

import java.util.List;

/**
 * 聊天会话
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-05-29
 */
public interface ImReplyService extends IService<ImReplyEntity> {

    /**
     * 获取消息会话列表
     *
     * @return
     */
    List<ImReplyEntity> getList();

    /**
     * 保存聊天会话
     *
     * @param entity
     * @return
     */
    boolean savaImReply(ImReplyEntity entity);

    /**
     * 获取聊天会话列表
     *
     * @return
     */
    List<ImReplyListModel> getImReplyList();

}
