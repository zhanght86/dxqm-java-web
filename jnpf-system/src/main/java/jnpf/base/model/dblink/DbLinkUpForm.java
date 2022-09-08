package jnpf.base.model.dblink;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class DbLinkUpForm {
    @NotBlank(message = "必填")
    private String password;
    @NotBlank(message = "必填")
    private String port;
    @NotBlank(message = "必填")
    private String host;
    @NotBlank(message = "必填")
    private String dbType;
    @NotBlank(message = "必填")
    private String fullName;
    @NotBlank(message = "必填")
    private String userName;
    @NotNull(message = "必填")
    private boolean enabledMark;

    private String serviceName;
    private String dbSchema;
    private String tableSpace;

    @ApiModelProperty(value = "排序码")
    private long sortCode;
}
