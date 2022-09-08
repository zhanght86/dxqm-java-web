package jnpf.model.productEntry;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 *
 * Product模型
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021-07-10 10:40:59
 */
@Data
public class ProductEntryListVO {
    @ApiModelProperty(value = "产品编号")
    private String productCode;
    @ApiModelProperty(value = "产品名称")
    private String productName;
    @ApiModelProperty(value = "数量")
    private Long qty;
    @ApiModelProperty(value = "订货类型")
    private String type;
    @ApiModelProperty(value = "活动")
    private String activity;
    @ApiModelProperty(value = "数据")
    private List<ProductEntryMdoel> dataList;
}
