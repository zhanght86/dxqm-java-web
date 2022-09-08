package jnpf.permission.model.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * IM获取用户接口
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-05-29
 */
@Data
public class ImUserListVo {
    @ApiModelProperty(value = "主键")
    private String id;
    @ApiModelProperty(value = "名称")
    private String realName;
    @ApiModelProperty(value = "用户头像")
    private String headIcon;
    @ApiModelProperty(value = "部门")
    private String department;
    @ApiModelProperty(value = "账号")
    private String account;
}
