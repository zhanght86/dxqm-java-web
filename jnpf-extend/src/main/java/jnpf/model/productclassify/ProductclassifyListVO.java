package jnpf.model.productclassify;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 *
 * 产品分类
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021-07-10 14:34:04
 */
@Data
public class ProductclassifyListVO{
    @ApiModelProperty(value = "主键")
    private String id;
    @ApiModelProperty(value = "名称")
    private String fullName;
    @ApiModelProperty(value = "是否子节点")
    private String hasChildren;
    @ApiModelProperty(value = "子节点")
    private List<ProductclassifyListVO> children;

}