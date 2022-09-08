package jnpf.permission.model.user;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import jnpf.util.treeutil.SumTree;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class UserSelectorModel extends SumTree {
    @JSONField(name="category")
    private String type;
    private String fullName;
    @ApiModelProperty(value = "状态")
    private Integer enabledMark;
    @ApiModelProperty(value = "图标")
    private String icon;
}
