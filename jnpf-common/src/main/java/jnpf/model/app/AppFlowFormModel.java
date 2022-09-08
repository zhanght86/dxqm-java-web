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
public class AppFlowFormModel {
    @ApiModelProperty(value = "主键id")
    private String id;
    @ApiModelProperty(value = "流程名称")
    private String fullName;
    @ApiModelProperty(value = "流程分类")
    private String category;
    @ApiModelProperty(value = "图标")
    private String icon;
    @ApiModelProperty(value = "编码")
    private String enCode;
    @ApiModelProperty(value = "图标背景色")
    private String iconBackground;
    @ApiModelProperty(value = "表单类型")
    private Integer formType;
    @ApiModelProperty(value = "是否常用")
    private Boolean isData;

}
