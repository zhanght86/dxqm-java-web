package jnpf.base.model.billrule;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:30
 */
@Data
public class BillRuleListVO {
    @ApiModelProperty(value = "id")
    private String id;
    @ApiModelProperty(value = "业务名称")
    private String fullName;
    @ApiModelProperty(value = "业务编码")
    private Integer digit;
    @ApiModelProperty(value = "流水位数")
    private String enCode;
    @ApiModelProperty(value = "流水起始")
    private String startNumber;
    @ApiModelProperty(value = "当前流水号")
    private String outputNumber;
    @ApiModelProperty(value = "状态(0-禁用，1-启用)")
    private Integer enabledMark;
    @ApiModelProperty(value = "排序码")
    private Long sortCode;
}
