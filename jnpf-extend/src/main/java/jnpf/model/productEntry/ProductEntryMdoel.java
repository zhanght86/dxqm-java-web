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
public class ProductEntryMdoel {
    @ApiModelProperty(value = "产品规格")
    private String productSpecification;
    @ApiModelProperty(value = "数量")
    private String qty;
    @ApiModelProperty(value = "单价")
    private String money;
    @ApiModelProperty(value = "折后单价")
    private String price;
    @ApiModelProperty(value = "单位")
    private String util;
    @ApiModelProperty(value = "控制方式")
    private String commandType;
}
