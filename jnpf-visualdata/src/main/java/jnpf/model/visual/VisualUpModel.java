package jnpf.model.visual;

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
public class VisualUpModel extends VisualCrModel{
    @ApiModelProperty(value = "背景url")
    private String backgroundUrl;
    @ApiModelProperty(value = "发布状态")
    private String status;
    @ApiModelProperty(value = "主键")
    private String id;
}
