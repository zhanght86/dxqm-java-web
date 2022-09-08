package jnpf.base;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:49
 */
@Data
public class MailModel {
    private String uid;
    private String from;
    private String fromName;
    private String recipient;
    private String toName;
    private String cc;
    private String ccName;
    private String bcc;
    private String bccName;
    private String subject;
    private String bodyText;
    private List<MailFile> attachment;
    private LocalDateTime date;
}
