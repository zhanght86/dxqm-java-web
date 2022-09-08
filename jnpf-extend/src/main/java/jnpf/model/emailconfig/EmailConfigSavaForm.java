package jnpf.model.emailconfig;

import lombok.Data;

import java.util.Date;

/**
 * 保存邮箱配置
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:58
 */
@Data
public class EmailConfigSavaForm {

    private String creatorUserId;

    private String password;

    private String senderName;

    private String smtpHost;

    private Integer pop3Port;

    private String id;

    private Date creatorTime;

    private Integer smtpPort;

    private Integer emailSsl;

    private String account;

    private String pop3Host;
}
