package jnpf.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:59
 */
@Data
@Builder
public class UploaderVO {
    @ApiModelProperty(value = "名称")
    private String name;
    @ApiModelProperty(value = "请求接口")
    private String url;
}
