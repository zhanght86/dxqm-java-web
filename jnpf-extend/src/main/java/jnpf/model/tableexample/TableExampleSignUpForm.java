package jnpf.model.tableexample;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 更新标签
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 10:21
 */
@Data
public class TableExampleSignUpForm {
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "项目标记")
    private String sign;
}
