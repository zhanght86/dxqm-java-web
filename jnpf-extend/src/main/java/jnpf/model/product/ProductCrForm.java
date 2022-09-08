package jnpf.model.product;

import io.swagger.annotations.ApiModelProperty;
import jnpf.model.productEntry.ProductEntryInfoVO;
import lombok.Data;

import java.math.BigDecimal;
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
public class ProductCrForm  {
    @ApiModelProperty(value = "订单编号")
    private String code;
    @ApiModelProperty(value = "客户Id")
    private String customerId;
    @ApiModelProperty(value = "客户名称")
    private String customerName;
    @ApiModelProperty(value = "审核人")
    private String auditName;
    @ApiModelProperty(value = "审核日期")
    private Long auditDate;
    @ApiModelProperty(value = "发货仓库")
    private String goodsWarehouse;
    @ApiModelProperty(value = "发货通知时间")
    private Long goodsDate;
    @ApiModelProperty(value = "发货通知人")
    private String goodsName;
    @ApiModelProperty(value = "收款方式")
    private String gatheringType;
    @ApiModelProperty(value = "业务员")
    private String business;
    @ApiModelProperty(value = "送货地址")
    private String address;
    @ApiModelProperty(value = "联系方式")
    private String contactTel;
    @ApiModelProperty(value = "收货消息")
    private Integer harvestMsg;
    @ApiModelProperty(value = "收货仓库")
    private String harvestWarehouse;
    @ApiModelProperty(value = "代发客户")
    private String issuingName;
    @ApiModelProperty(value = "让利金额")
    private BigDecimal partPrice;
    @ApiModelProperty(value = "优惠金额")
    private BigDecimal reducedPrice;
    @ApiModelProperty(value = "折后金额")
    private BigDecimal discountPrice;
    @ApiModelProperty(value = "备注")
    private String description;
    @ApiModelProperty(value = "子表数据")
    private List<ProductEntryInfoVO> productEntryList;

}