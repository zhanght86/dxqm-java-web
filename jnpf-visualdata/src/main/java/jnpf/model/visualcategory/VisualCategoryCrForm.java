package jnpf.model.visualcategory;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年6月15日
 */
@Data
public class VisualCategoryCrForm {
    @ApiModelProperty(value = "分类键值")
    private String categoryKey;
    @ApiModelProperty(value = "分类名称")
    private String categoryValue;
    @ApiModelProperty(value = "是否已删除")
    private Integer isDeleted;
}
