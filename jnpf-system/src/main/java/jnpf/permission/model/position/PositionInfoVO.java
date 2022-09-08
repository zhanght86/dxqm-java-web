package jnpf.permission.model.position;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class PositionInfoVO {
    @ApiModelProperty(value = "id")
    private String id;
    @ApiModelProperty(value = "上级id")
    private String organizeId;
    @ApiModelProperty(value = "岗位名称")
    private String fullName;
    @ApiModelProperty(value = "岗位编码")
    private String enCode;
    @ApiModelProperty(value = "岗位类型")
    private String type;
    @ApiModelProperty(value = "岗位状态")
    private Integer enabledMark;
    @ApiModelProperty(value = "岗位说明")
    private String description;
    @ApiModelProperty(value = "排序")
    private Long sortCode;

}
