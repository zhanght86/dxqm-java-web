

package jnpf.model.product;

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
public class ProductListVO{
    @ApiModelProperty(value = "主键")
    private String id;
    @ApiModelProperty(value = "订单编号")
    private String code;
    @ApiModelProperty(value = "客户名称")
    private String customerName;
    @ApiModelProperty(value = "业务员")
    private String business;
    @ApiModelProperty(value = "送货地址")
    private String address;
    @ApiModelProperty(value = "联系方式")
    private String contactTel;
    @ApiModelProperty(value = "制单人")
    private String salesmanName;
    @ApiModelProperty(value = "审核状态")
    private Integer auditState;
    @ApiModelProperty(value = "发货状态")
    private Integer goodsState;
    @ApiModelProperty(value = "关闭状态")
    private Integer closeState;
    @ApiModelProperty(value = "关闭日期")
    private Long  closeDate;
    @ApiModelProperty(value = "联系人")
    private String contactName;
}