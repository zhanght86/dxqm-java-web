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
public class MonitorListVO {
    @ApiModelProperty(value = "系统信息")
    private SystemModel system;
    @ApiModelProperty(value = "CPU信息")
    private CpuModel cpu;
    @ApiModelProperty(value = "内存信息")
    private MemoryModel memory;
    @ApiModelProperty(value = "硬盘信息")
    private DiskModel disk;
    @ApiModelProperty(value = "当前时间")
    private Long time;
}
