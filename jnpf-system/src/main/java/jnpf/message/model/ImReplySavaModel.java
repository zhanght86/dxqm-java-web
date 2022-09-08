package jnpf.message.model;

import lombok.Data;

import java.util.Date;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-05-29
 */
@Data
public class ImReplySavaModel {

    private String userId;

    private String receiveUserId;

    private Date receiveTime;

    public ImReplySavaModel(String userId, String receiveUserId, Date receiveTime) {
        this.userId = userId;
        this.receiveUserId = receiveUserId;
        this.receiveTime = receiveTime;
    }
}
