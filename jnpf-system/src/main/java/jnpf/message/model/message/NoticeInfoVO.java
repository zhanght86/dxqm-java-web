package jnpf.message.model.message;

import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class NoticeInfoVO {
    private String id;
    private String title;
    private String bodyText;
    private String creatorUser;
    private Long lastModifyTime;
}
