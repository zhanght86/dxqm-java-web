package jnpf.base.model.monitor;

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
public class SystemModel {
    @ApiModelProperty(value = "系统")
    private String os;
    @ApiModelProperty(value = "服务器IP")
    private String ip;
    @ApiModelProperty(value = "运行时间")
    private String day;
}
