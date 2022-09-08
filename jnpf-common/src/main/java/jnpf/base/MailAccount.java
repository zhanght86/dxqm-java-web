package jnpf.base;

import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:49
 */
@Data
public class MailAccount {
    /**
     * pop3服务
     */
    private String pop3Host;
    /**
     * pop3端口
     */
    private int pop3Port;
    /**
     * smtp服务
     */
    private String smtpHost;
    /**
     * smtp端口
     */
    private int smtpPort;
    /**
     * 账户
     */
    private String account;
    /**
     * 账户名称
     */
    private String accountName;
    /**
     * 密码
     */
    private String password;
    /**
     * SSL
     */
    private Boolean ssl;;
}
