package jnpf.permission.model.organize;

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
public class OrganizeCrForm {
    @NotBlank(message = "公司上级不能为空")
    private String parentId;
    @NotBlank(message = "公司名称不能为空")
    private String fullName;
    @NotBlank(message = "公司编码不能为空")
    private String enCode;
    private String description;
    @NotNull(message = "公司状态不能为空")
    private Integer enabledMark;
    private OrganizeCrModel propertyJson;
    @ApiModelProperty(value = "排序")
    private Long sortCode;
}
