package jnpf.util.treeutil;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 需要实现树的类可以继承该类，手写set方法，在设定本身属性值时同时设置该类中的相关属性
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:58
 */
@Data
public class TreeModel<T> {
    @ApiModelProperty(value = "主键")
    private String id;
    @ApiModelProperty(value = "名称")
    private String fullName;
    @ApiModelProperty(value = "父主键")
    private String parentId;
    @ApiModelProperty(value = "是否有下级菜单")
    private Boolean hasChildren = true;
    @ApiModelProperty(value = "图标")
    private String icon;
    @ApiModelProperty(value = "下级菜单列表")
    private List<TreeModel<T>> children = new ArrayList<>();
}