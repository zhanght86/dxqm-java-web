package jnpf.model;


import io.swagger.annotations.ApiModelProperty;
import jnpf.model.login.UserPositionVO;
import lombok.Data;

import java.util.List;

/**
 * 用户
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021-07-08
 */
@Data
public class AppUsersVO {
    @ApiModelProperty(value = "用户id")
    private String userId;
    @ApiModelProperty(value = "用户账号")
    private String userAccount;
    @ApiModelProperty(value = "用户姓名")
    private String userName;
    @ApiModelProperty(value = "用户头像")
    private String headIcon;
    @ApiModelProperty(value = "部门主键")
    private String departmentId;
    @ApiModelProperty(value = "部门名称")
    private String departmentName;
    @ApiModelProperty(value = "组织主键")
    private String organizeId;
    @ApiModelProperty(value = "组织名称")
    private String organizeName;
    @ApiModelProperty(value = "角色主键")
    private String roleId;
    @ApiModelProperty(value = "角色名称")
    private String roleName;
    @ApiModelProperty(value = "性别")
    private Integer gender;
    @ApiModelProperty(value = "岗位")
    private List<UserPositionVO> positionIds;
    @ApiModelProperty(value = "生日")
    private Long birthday;
    @ApiModelProperty(value = "手机")
    private String mobilePhone;
    @ApiModelProperty(value = "邮箱")
    private String email;

}
