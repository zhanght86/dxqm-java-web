package jnpf.model.visualdb;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年6月15日
 */
@Data
public class VisualDbSelectVO {
    @ApiModelProperty(value = "驱动")
    private String driverClass;
    @ApiModelProperty(value = "名称")
    private String name;
    @ApiModelProperty(value = "主键")
    private String id;

}
