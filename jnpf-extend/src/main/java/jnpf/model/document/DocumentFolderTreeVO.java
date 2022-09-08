package jnpf.model.document;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:58
 */
@Data
public class DocumentFolderTreeVO {
    @ApiModelProperty(value = "名称")
    private String fullName;
    @ApiModelProperty(value = "图标")
    private String icon;
    @ApiModelProperty(value = "是否有下级菜单")
    private Boolean hasChildren;
    @ApiModelProperty(value = "主键")
    private String id;
    @ApiModelProperty(value = "父级主键")
    private String parentId;

    private List<DocumentFolderTreeVO> children;
}
