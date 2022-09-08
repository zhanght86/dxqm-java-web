package jnpf.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:49
 */
@Data
public class LanguageVO {
    @ApiModelProperty(value = "语言编码")
    private String encode;
    @ApiModelProperty(value = "语言名称")
    private String fullName;
}
