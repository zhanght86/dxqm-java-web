package jnpf.permission.model.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
@Builder
public class UserSubordinateVO {
    @ApiModelProperty(value = "头像")
    private String avatar;
    @ApiModelProperty(value = "用户名")
    private String userName;
    @ApiModelProperty(value = "部门")
    private String department;
}
