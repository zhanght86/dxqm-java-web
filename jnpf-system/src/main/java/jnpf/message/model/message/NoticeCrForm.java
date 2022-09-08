package jnpf.message.model.message;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class NoticeCrForm {
    @ApiModelProperty(value = "内容")
    private String bodyText;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "标题")
    private String title;

}
