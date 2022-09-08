package jnpf.model.visual;

import io.swagger.annotations.ApiModelProperty;
import jnpf.model.visualconfig.VisualConfigCrForm;
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
public class VisualCrModel {
    @ApiModelProperty(value = "标题")
    private String title;
    @ApiModelProperty(value = "密码")
    private String password;
    @ApiModelProperty(value = "分类")
    private String category;
}
