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
public class ErrorLogVO {
    @ApiModelProperty(value = "创建用户")
    private String userName;
    @ApiModelProperty(value = "创建时间",example = "1")
    private Long creatorTime;
    @ApiModelProperty(value = "IP")
    private String ipaddress;
    @ApiModelProperty(value = "id")
    private String id;
    @ApiModelProperty(value = "异常功能")
    private String moduleName;
    @ApiModelProperty(value = "异常描述")
    private String json;
}
