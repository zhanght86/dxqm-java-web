package jnpf.permission.model.role;

import io.swagger.annotations.ApiModelProperty;
import jnpf.util.treeutil.SumTree;
import lombok.Data;

import java.util.Date;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class RoleModel extends SumTree {
    @ApiModelProperty(value = "名称")
    private String fullName;
    @ApiModelProperty(value = "编码")
    private String enCode;
    @ApiModelProperty(value = "角色类型")
    private String type;
    @ApiModelProperty(value = "备注")
    private String description;
    @ApiModelProperty(value = "状态")
    private Integer enabledMark;
    @ApiModelProperty(value = "创建时间")
    private Date creatorTime;
    @ApiModelProperty(value = "排序")
    private Long sortCode;
    @ApiModelProperty(value = "数量")
    private Long num;

}
