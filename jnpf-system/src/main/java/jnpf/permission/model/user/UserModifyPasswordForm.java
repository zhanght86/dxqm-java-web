package jnpf.permission.model.user;

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
public class UserModifyPasswordForm {
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "旧密码,需要 MD5 加密后传输")
    private String oldPassword;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "新密码")
    private String password;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "验证码")
    private String code;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "验证码标识")
    private String timestamp;
}
