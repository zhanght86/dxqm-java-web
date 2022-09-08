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
public class DiskModel {
    @ApiModelProperty(value = "硬盘总容量")
    private String total;
    @ApiModelProperty(value = "空闲硬盘")
    private String available;
    @ApiModelProperty(value = "已使用")
    private String used;
    @ApiModelProperty(value = "已使用百分比")
    private String usageRate;
}
