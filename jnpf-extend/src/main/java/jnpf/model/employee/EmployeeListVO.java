package jnpf.model.employee;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 10:06
 */
@Data
public class EmployeeListVO {
    @ApiModelProperty(value = "主鍵")
    private String id;
    @ApiModelProperty(value = "工号")
    private String enCode;
    @ApiModelProperty(value = "姓名")
    private String fullName;
    @ApiModelProperty(value = "性别ID")
    private String gender;
    @ApiModelProperty(value = "部门")
    private String departmentName;
    @ApiModelProperty(value = "岗位")
    private String positionName;
    @ApiModelProperty(value = "用工性质")
    private String workingNature;
    @ApiModelProperty(value = "身份证")
    private String idNumber;
    @ApiModelProperty(value = "联系电话")
    private String telephone;
    @ApiModelProperty(value = "生日")
    private Long birthday;
    @ApiModelProperty(value = "参加工作时间")
    private Long attendWorkTime;
    @ApiModelProperty(value = "学历")
    private String education;
    @ApiModelProperty(value = "所学专业")
    private String major;
    @ApiModelProperty(value = "毕业院校")
    private String graduationAcademy;
    @ApiModelProperty(value = "毕业时间")
    private Long graduationTime;
    @ApiModelProperty(value = "创建时间")
    private Long creatorTime;

}
