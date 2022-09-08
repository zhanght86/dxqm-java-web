package jnpf.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * app常用数据
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021-07-08
 */
@Data
public class AppDataCrForm {
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "应用类型")
    private String objectType;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "应用主键")
    private String objectId;
    @ApiModelProperty(value = "数据")
    private String objectData;
}
