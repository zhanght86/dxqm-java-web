package jnpf.model.customer;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * 客户信息
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021-07-10 14:09:05
 */
@Data
public class CustomerCrForm  {
    @ApiModelProperty(value = "编码")
    private String code;
    @ApiModelProperty(value = "客户名称")
    private String customerName;
    @ApiModelProperty(value = "地址")
    private String address;
    @ApiModelProperty(value = "名称")
    private String name;
    @ApiModelProperty(value = "联系方式")
    private String contactTel;

}