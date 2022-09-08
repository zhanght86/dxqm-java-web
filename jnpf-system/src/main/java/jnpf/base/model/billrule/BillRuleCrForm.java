package jnpf.base.model.billrule;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:28
 */
@Data
public class BillRuleCrForm {
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "业务名称")
    private String fullName;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "流水位数")
    private String enCode;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "流水前缀")
    private String prefix;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "流水日期格式")
    private String dateFormat;
    @NotNull(message = "必填")
    @ApiModelProperty(value = "流水位数")
    private Integer digit;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "流水起始")
    private String startNumber;
    @ApiModelProperty(value = "流水范例")
    private String example;
    @ApiModelProperty(value = "状态(0-禁用，1-启用)")
    private Integer enabledMark;
    @ApiModelProperty(value = "流水说明")
    private String description;
    private long sortCode;
}
