package jnpf.permission.model.role;

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
public class RoleCrForm {
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "角色名称")
    private String fullName;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "角色编号")
    private String enCode;
    @NotNull(message = "必填")
    @ApiModelProperty(value = "角色类型(id)")
    private String type;
    @NotNull(message = "必填")
    @ApiModelProperty(value = "状态")
    private int enabledMark;
    private String description;
    @ApiModelProperty(value = "排序")
    private long sortCode;
}
