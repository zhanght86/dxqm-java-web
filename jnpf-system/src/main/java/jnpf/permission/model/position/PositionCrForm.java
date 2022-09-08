package jnpf.permission.model.position;

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
public class PositionCrForm {
    @NotBlank(message = "必填")
    @ApiModelProperty(value="岗位编码")
    private String enCode;
    @NotBlank(message = "必填")
    @ApiModelProperty(value="所属部门(id)")
    private String organizeId;
    @NotNull(message = "必填")
    @ApiModelProperty(value="岗位状态")
    private Integer enabledMark;
    @NotBlank(message = "必填")
    @ApiModelProperty(value="岗位名称")
    private String fullName;

    private String description;
    @NotNull(message = "必填")
    @ApiModelProperty(value="岗位类型(id)")
    private Integer type;
    @ApiModelProperty(value = "排序")
    private Long sortCode;
}
