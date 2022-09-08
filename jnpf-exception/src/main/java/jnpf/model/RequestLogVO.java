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
public class RequestLogVO {
    @ApiModelProperty(value = "id")
    private String id;
    @ApiModelProperty(value = "请求时间",example = "1")
    private Long creatorTime;
    @ApiModelProperty(value = "请求用户名")
    private String userName;
    @ApiModelProperty(value = "请求IP")
    private String ipaddress;
    @ApiModelProperty(value = "请求设备")
    private String platForm;
    @ApiModelProperty(value = "请求地址")
    private String requestURL;
    @ApiModelProperty(value = "请求类型")
    private String requestMethod;
    @ApiModelProperty(value = "请求耗时",example = "1")
    private Long requestDuration;
}
