package jnpf.message.util;

import jnpf.message.entity.MessageEntity;
import jnpf.message.entity.MessageReceiveEntity;
import jnpf.util.RandomUtil;

import java.util.Date;

/**
 * 消息实体类
 *
 * @版本： V3.2.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/4/22 9:06
 */
public class JnpfMessageUtil {
    public static MessageEntity setMessageEntity(String userId, String title, String bodyText, Integer recType) {
        MessageEntity entity = new MessageEntity();
        entity.setTitle(title);
        entity.setBodyText(bodyText);
        entity.setId(RandomUtil.uuId());
        entity.setType(recType+1);
        entity.setCreatorUser(userId);
        entity.setCreatorTime(new Date());
        entity.setLastModifyTime(entity.getCreatorTime());
        entity.setLastModifyUserId(entity.getCreatorUser());
        return entity;
    }

    public static MessageReceiveEntity setMessageReceiveEntity(String messageId, String toUserId){
        MessageReceiveEntity entity = new MessageReceiveEntity();
        entity.setId(RandomUtil.uuId());
        entity.setMessageId(messageId);
        entity.setUserId(toUserId);
        entity.setIsRead(0);
        return entity;
    }
}
