package jnpf.permission.model.position;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class PositionSelectorVO {
    private String id;
    @ApiModelProperty(value = "父级ID")
    private String  parentId;
    @ApiModelProperty(value = "名称")
    private String  fullName;
    @ApiModelProperty(value = "是否有下级菜单")
    private Boolean hasChildren;
    @ApiModelProperty(value = "状态")
    private Integer enabledMark;
    @ApiModelProperty(value = "下级菜单列表")
    private List<PositionSelectorVO> children;
    @JSONField(name="category")
    private String  type;
    @ApiModelProperty(value = "图标")
    private String icon;
}
