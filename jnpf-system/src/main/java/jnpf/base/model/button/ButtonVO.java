package jnpf.base.model.button;

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
public class ButtonVO {
    @ApiModelProperty(value = "按钮主键")
    private String id;
    @ApiModelProperty(value = "按钮上级")
    private String parentId;
    @ApiModelProperty(value = "按钮名称")
    private String fullName;
    @ApiModelProperty(value = "按钮编码")
    private String enCode;
    @ApiModelProperty(value = "按钮图标")
    private String icon;
    @ApiModelProperty(value = "请求地址")
    private String urlAddress;
    @ApiModelProperty(value = "功能主键")
    private String moduleId;
}
