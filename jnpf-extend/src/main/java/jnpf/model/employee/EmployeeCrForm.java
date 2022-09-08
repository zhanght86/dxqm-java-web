package jnpf.model.employee;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 10:06
 */
@Data
public class EmployeeCrForm {
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "姓名")
    private String fullName;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "工号")
    private String enCode;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "性别")
    private String gender;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "部门")
    private String departmentName;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "岗位")
    private String positionName;
    @ApiModelProperty(value = "用工性质")
    private String workingNature;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "身份证号")
    private String idNumber;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "联系电话")
    private String telephone;
    @ApiModelProperty(value = "参加工作")
    private long attendWorkTime;
    @ApiModelProperty(value = "出生年月")
    private long birthday;
    @ApiModelProperty(value = "最高学历")
    private String education;
    @ApiModelProperty(value = "所学专业")
    private String major;
    @ApiModelProperty(value = "毕业院校")
    private String graduationAcademy;
    @ApiModelProperty(value = "毕业时间")
    private long graduationTime;
}
