package jnpf.model.email;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:58
 */
@Data
public class EmailCheckForm {
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "POP3端口")
    private String pop3Port;
    @ApiModelProperty(value = "ssl登录")
    private String emailSsl;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "邮箱地址")
    private String account;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "POP3服务")
    private String pop3Host;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "邮箱密码")
    private String password;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "SMTP服务")
    private String smtpHost;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "SMTP端口")
    private String smtpPort;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "显示名称")
    private String senderName;
}
