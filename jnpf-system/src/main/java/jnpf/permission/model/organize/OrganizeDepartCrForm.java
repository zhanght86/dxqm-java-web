package jnpf.permission.model.organize;

import com.alibaba.fastjson.annotation.JSONField;
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
public class OrganizeDepartCrForm {

    @JSONField(name="manager")
    private String managerId;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "上级ID")
    private String parentId;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "部门名称")
    private String fullName;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "部门编码")
    private String enCode;
    @ApiModelProperty(value = "状态")
    private int enabledMark;
    private String description;
    @ApiModelProperty(value = "排序")
    private Long sortCode;
}
