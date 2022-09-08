package jnpf.base.model.dataInterface;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class DataInterfaceTreeVO {
    @ApiModelProperty(value = "主键Id")
    private String categoryId;
    @ApiModelProperty(value = "接口名称")
    private String fullName;
    private String id;
    private Boolean hasChildren;
    private List<DataInterfaceTreeModel> children;
}
