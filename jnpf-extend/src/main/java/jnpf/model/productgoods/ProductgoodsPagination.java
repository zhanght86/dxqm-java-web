package jnpf.model.productgoods;

import io.swagger.annotations.ApiModelProperty;
import jnpf.base.Pagination;
import lombok.Data;

/**
 *
 * 产品商品
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021-07-10 15:57:50
 */
@Data
public class ProductgoodsPagination extends Pagination {
    @ApiModelProperty(value = "分类主键")
    private String classifyId;
    @ApiModelProperty(value = "产品编号")
    private String code;
    @ApiModelProperty(value = "产品名称")
    private String fullName;

}