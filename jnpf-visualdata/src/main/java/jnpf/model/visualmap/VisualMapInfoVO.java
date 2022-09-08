package jnpf.model.visualmap;

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
public class VisualMapInfoVO {
    @ApiModelProperty(value = "地图名称")
    private String name;
    @ApiModelProperty(value = "地图数据")
    private String data;
    @ApiModelProperty(value = "主键")
    private String id;
}
