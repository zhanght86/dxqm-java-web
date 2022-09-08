package jnpf.message.model;

import lombok.Data;

import java.util.Date;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-05-31
 */
@Data
public class ImReplyListModel {
    /**
     * id
     */
    private String id;

    /**
     * 名称
     */
    private String realName;

    /**
     * 头像
     */
    private String headIcon;

    /**
     * 最新消息
     */
    private String latestMessage;

    /**
     * 最新时间
     */
    private Date latestDate;

    /**
     * 未读消息
     */
    private Integer unreadMessage;

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 账号
     */
    private String account;

    /**
     * UserId
     */
    private String userId;

}
