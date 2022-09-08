package jnpf.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 10:10
 */
@Data
public class LoginLogVO {
    @ApiModelProperty(value = "id")
    private String id;
    @ApiModelProperty(value = "创建时间",example = "1")
    private Long creatorTime;
    @ApiModelProperty(value = "登陆用户")
    private String userName;
    @ApiModelProperty(value = "登陆IP")
    private String ipaddress;
    @ApiModelProperty(value = "登陆平台")
    private String platForm;
    @ApiModelProperty(value = "登陆日志摘要")
    private String abstracts;
}
