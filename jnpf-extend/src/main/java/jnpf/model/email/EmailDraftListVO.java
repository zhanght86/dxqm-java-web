package jnpf.model.email;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 10:06
 */
@Data
public class EmailDraftListVO {
    @ApiModelProperty(value = "附件")
    private String attachment;
    @ApiModelProperty(value = "发件人")
    private String id;
    @ApiModelProperty(value = "主题")
    private String subject;
    @ApiModelProperty(value = "收件人")
    private String recipient;
    @ApiModelProperty(value = "创建时间")
    private Long creatorTime;
}
