package jnpf.model.app;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:47
 */
@Data
public class AppMenuModel {
    @ApiModelProperty(value = "扩展字段")
    private String propertyJson;
    @ApiModelProperty(value = "菜单编码")
    private String enCode;
    @ApiModelProperty(value = "菜单名称")
    private String fullName;
    @ApiModelProperty(value = "图标")
    private String icon;
    @ApiModelProperty(value = "主键id")
    private String id;
    @ApiModelProperty(value = "链接地址")
    private String urlAddress;

}
