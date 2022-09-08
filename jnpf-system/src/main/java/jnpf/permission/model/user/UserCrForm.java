package jnpf.permission.model.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class UserCrForm {

    @NotBlank(message = "必填")
    @ApiModelProperty("账户")
    private String account;

    @NotBlank(message = "必填")
    @ApiModelProperty("户名")
    private String realName;

    @NotBlank(message = "必填")
    @ApiModelProperty("部门")
    private String organizeId;

    @ApiModelProperty("主管")
    private String managerId;

    @ApiModelProperty("岗位")
    private String positionId;

    @ApiModelProperty("角色")
    private String roleId;

    private String description;

    @NotNull(message = "性别不能为空")
    @ApiModelProperty("性别")
    private int gender;

    @ApiModelProperty("民族")
    private String nation;

    @ApiModelProperty("籍贯")
    private String nativePlace;

    @ApiModelProperty("证件类型")
    private String certificatesType;

    @ApiModelProperty("证件号码")
    private String certificatesNumber;

    @ApiModelProperty("文化程度")
    private String education;

    @ApiModelProperty("生日")
    private String birthday;

    @ApiModelProperty("电话")
    private String telePhone;

    @ApiModelProperty("Landline")
    private String landline;

    @ApiModelProperty("手机")
    private String mobilePhone;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("UrgentContacts")
    private String urgentContacts;

    @ApiModelProperty("紧急电话")
    private String urgentTelePhone;

    @ApiModelProperty("通讯地址")
    private String postalAddress;

    @ApiModelProperty("头像")
    private String headIcon;

    @ApiModelProperty(value = "排序")
    private Long sortCode;

    @ApiModelProperty("入职日期")
    private long entryDate;

    @ApiModelProperty(value = "状态")
    private Integer enabledMark;
}
