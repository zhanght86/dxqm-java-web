package jnpf.model.emailconfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:58
 */
@Data
public class EmailConfigCheckForm {
    @ApiModelProperty(value = "邮箱密码")
    private String password;

    @ApiModelProperty(value = "显示名称")
    private String senderName;

    @ApiModelProperty(value = "SMTP服务")
    private String smtpHost;

    @ApiModelProperty(value = "POP3端口")
    private Integer pop3Port;

    @ApiModelProperty(value = "主键id")
    private String id;

    @ApiModelProperty(value = "SMTP端口")
    private Integer smtpPort;

    @ApiModelProperty(value = "ssl登录")
    private Integer emailSsl;

    @ApiModelProperty(value = "邮箱地址")
    private String account;

    @ApiModelProperty(value = "POP3服务")
    private String pop3Host;
}
