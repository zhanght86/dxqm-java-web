package jnpf.model.email;

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
public class EmailCofigInfoVO {

    @ApiModelProperty(value = "账户")
    private String account;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "POP3服务")
    private String pop3Host;

    @ApiModelProperty(value = "POP3端口")
    private Integer pop3Port;

    @ApiModelProperty(value = "发件人名称")
    private String senderName;

    @ApiModelProperty(value = "SMTP服务")
    private String smtpHost;

    @ApiModelProperty(value = "SMTP端口")
    private Integer smtpPort;
    @ApiModelProperty(value = "创建时间")
    private Long creatorTime;
    @ApiModelProperty(value = "是否开户SSL登录(1-是,0否)")
    private Integer emailSsl;
}
