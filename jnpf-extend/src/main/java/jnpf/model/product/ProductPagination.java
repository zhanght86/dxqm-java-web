package jnpf.model.product;


import io.swagger.annotations.ApiModelProperty;
import jnpf.base.Pagination;
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
public class ProductPagination extends Pagination {
    @ApiModelProperty(value = "订单编号")
    private String code;
    @ApiModelProperty(value = "客户名称")
    private String customerName;
    @ApiModelProperty(value = "联系方式")
    private String contactTel;



}