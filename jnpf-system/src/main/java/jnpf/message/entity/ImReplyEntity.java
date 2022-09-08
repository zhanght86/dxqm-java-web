package jnpf.message.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 聊天会话表
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-05-28
 */
@Data
@TableName("base_imreply")
public class ImReplyEntity {
    /**
     * 聊天主键
     */
    @TableId("F_ID")
    private String id;

    /**
     * 发送者
     */
    @TableField("F_USERID")
    private String userId;

    /**
     * 接收者
     */
    @TableField("F_RECEIVEUSERID")
    private String receiveUserId;

    /**
     * 发送时间
     */
    @TableField("F_RECEIVETIME")
    private Date receiveTime;

}
