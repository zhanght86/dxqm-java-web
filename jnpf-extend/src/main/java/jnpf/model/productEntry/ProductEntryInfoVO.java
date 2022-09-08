package jnpf.model.productEntry;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * Product模型
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021-07-10 10:40:59
 */
@Data
public class ProductEntryInfoVO {
    @ApiModelProperty(value = "产品编号")
    private String productCode;
    @ApiModelProperty(value = "产品名称")
    private String productName;
    @ApiModelProperty(value = "产品规格")
    private String productSpecification;
    @ApiModelProperty(value = "数量")
    private Long qty;
    @ApiModelProperty(value = "订货类型")
    private String type;
    @ApiModelProperty(value = "单价")
    private String money;
    @ApiModelProperty(value = "折后单价")
    private String price;
    @ApiModelProperty(value = "金额")
    private String amount;
    @ApiModelProperty(value = "备注")
    private String description;
}
