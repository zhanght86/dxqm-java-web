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
public class UserLogVO {
    @ApiModelProperty(value = "登录时间")
    private Long creatorTime;
    @ApiModelProperty(value = "登录用户")
    private String userName;
    @ApiModelProperty(value = "登录IP")
    private String ipaddress;
    @ApiModelProperty(value = "摘要")
    private String platForm;
    private String requestURL;
    private String requestMethod;
    private String requestDuration;
}
