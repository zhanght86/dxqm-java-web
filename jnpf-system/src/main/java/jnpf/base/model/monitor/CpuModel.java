package jnpf.base.model.monitor;

import com.alibaba.fastjson.annotation.JSONField;
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
public class CpuModel {
    @ApiModelProperty(value = "cpu名称")
    private String name;
    @ApiModelProperty(value = "物理CPU个数")
    @JSONField(name="package")
    private String packageName;
    @ApiModelProperty(value = "CPU内核个数")
    private String core;
    @ApiModelProperty(value = "内核个数")
    private int coreNumber;
    @ApiModelProperty(value = "逻辑CPU个数")
    private String logic;
    @ApiModelProperty(value = "CPU已用百分比")
    private String used;
    @ApiModelProperty(value = "未用百分比")
    private String idle;
}
