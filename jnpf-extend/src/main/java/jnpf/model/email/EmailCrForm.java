package jnpf.model.email;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 发邮件
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:58
 */
@Data
public class EmailCrForm {
    @JsonIgnore
    @ApiModelProperty(value = "id",hidden = true)
    private String id;
    @ApiModelProperty(value = "抄送人")
    private String cc;

    @ApiModelProperty(value = "密送人")
    private String bcc;

    @ApiModelProperty(value = "正文")
    private String bodyText;

    @ApiModelProperty(value = "附件")
    private String attachment;

    @NotBlank(message = "必填")
    @ApiModelProperty(value = "主题")
    private String subject;

    @NotBlank(message = "必填")
    @ApiModelProperty(value = "收件人")
    private String recipient;

}
