package jnpf.model.visual;

import io.swagger.annotations.ApiModelProperty;
import jnpf.model.VisualPagination;
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
public class VisualPaginationModel extends VisualPagination {
    @ApiModelProperty(value = "分类")
    private String category;


}
