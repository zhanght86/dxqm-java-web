package jnpf.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * app常用数据
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021-07-08
 */
@Data
public class AppDataListAllVO {
    @ApiModelProperty(value = "主键")
    private String id;
    @ApiModelProperty(value = "是否有下级菜单")
    private Boolean hasChildren;
    @ApiModelProperty(value = "菜单名称")
    private String fullName;
    @ApiModelProperty(value = " 图标")
    private String icon;
    @ApiModelProperty(value = "链接地址")
    private String urlAddress;
    @ApiModelProperty(value = "父级id")
    private String parentId;
    @ApiModelProperty(value = "菜单类型",example = "1")
    private Integer type;
    @ApiModelProperty(value = "扩展字段")
    private String propertyJson;
    @ApiModelProperty(value = "是否常用")
    private Boolean isData;
    @ApiModelProperty(value = "下级菜单列表")
    private List<AppDataListAllVO> children;
}
