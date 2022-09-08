package jnpf.model.visual;

import io.swagger.annotations.ApiModelProperty;
import jnpf.util.treeutil.SumTree;
import lombok.Data;

import java.util.List;

/**
 *
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年6月15日
 */
@Data
public class VisualSelectorVO  {
    @ApiModelProperty(value = "主键")
    private String id;
    @ApiModelProperty(value = "名称")
    private String fullName;
    @ApiModelProperty(value = "是否有下级")
    private Boolean hasChildren;
    @ApiModelProperty(value = "下级")
    private List<VisualSelectorVO> children;
}
