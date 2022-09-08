package jnpf.model.login;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:59
 */
@Data
public class UserPositionVO {
    @ApiModelProperty(value = "岗位id")
    private String id;
    @ApiModelProperty(value = "岗位名称")
    private String name;
}
