package jnpf.base.model;

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
public class RequestParameterModel {
    @ApiModelProperty(value = "参数名称")
    private String parameter;
    @ApiModelProperty(value = "绑定字段")
    private String field;
    @ApiModelProperty(value = "参数类型")
    private String type;
    @ApiModelProperty(value = "操作符")
    private String opt;
    @ApiModelProperty(value = "1-必填 ，0-非必填")
    private Integer required;
    @ApiModelProperty(value = "默认值")
    private String defaultVal;
}
