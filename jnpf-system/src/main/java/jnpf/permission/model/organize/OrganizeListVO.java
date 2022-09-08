package jnpf.permission.model.organize;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:15
 */
@Data
public class OrganizeListVO {
    @ApiModelProperty(value = "主键")
    private String id;
    @ApiModelProperty(value = "父主键")
    private String parentId;
    @ApiModelProperty(value = "名称")
    private String fullName;
    @ApiModelProperty(value = "编码")
    private String enCode;
    @ApiModelProperty(value = "备注")
    private String description;
    @ApiModelProperty(value = "状态")
    private Integer enabledMark;
    private Long creatorTime;
    @ApiModelProperty(value = "是否有下级菜单")
    private Boolean hasChildren;
    @ApiModelProperty(value = "下级菜单列表")
    private List<OrganizeListVO> children;
    @ApiModelProperty(value = "排序")
    private Long sortCode;
}
