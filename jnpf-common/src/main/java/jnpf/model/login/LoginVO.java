package jnpf.model.login;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:49
 */
@Data
public class LoginVO {
    @ApiModelProperty(value = "token")
    private String token;
    @ApiModelProperty(value = "主题")
    private String theme;
}
