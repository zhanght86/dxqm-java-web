package jnpf.message.model;

import lombok.Data;

/**
 * 未读消息模型
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Data
public class ImUnreadNumModel {
    /**
    *  发送者Id
    */
    private String sendUserId;

    /**
    * 租户id
    */
    private String tenantId;

    /**
    *  未读数量
    */
    private int unreadNum;

    /**
    *  默认消息
    */
    private String defaultMessage;

    /**
    *  默认消息类型
    */
    private String defaultMessageType;

    /**
    *  默认消息时间
    */
    private String defaultMessageTime;
}
