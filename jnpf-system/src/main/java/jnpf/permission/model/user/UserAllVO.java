package jnpf.permission.model.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class UserAllVO {
    @ApiModelProperty(value = "主键")
    private String id;
    @ApiModelProperty(value = "账号")
    private String account;
    @ApiModelProperty(value = "名称")
    private String realName;
    @ApiModelProperty(value = "用户头像")
    private String headIcon;
    /**
     * //1,男。2女
     */
    @ApiModelProperty(value = "性别")
    private String gender;
    @ApiModelProperty(value = "部门")
    private String department;
    @ApiModelProperty(value = "快速搜索")
    private String quickQuery;
}
