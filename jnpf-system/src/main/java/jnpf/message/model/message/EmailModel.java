package jnpf.message.model.message;

import lombok.Data;

/**
 * 发送邮件配置模型
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/4/20 14:27
 */
@Data
public class EmailModel {
    private String emailPop3Host;
    private String emailPop3Port;
    private String emailSmtpHost;
    private String emailSmtpPort;
    private String emailSenderName;
    private String emailAccount;
    private String emailPassword;
    private String emailSsl;

    private String emailToUsers;
    private String emailContent;
    private String emailTitle;
}
