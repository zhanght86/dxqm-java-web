package jnpf.base.model.dataInterface;

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
public class DataInterfaceListVO {
    @ApiModelProperty(value = "主键Id")
    private String id;
    @ApiModelProperty(value = "接口名称")
    private String fullName;
    @ApiModelProperty(value = "接口类型")
    private Integer dataType;
    @ApiModelProperty(value = "编码")
    private String enCode;
    @ApiModelProperty(value = "排序")
    private Long sortCode;
    @ApiModelProperty(value = "状态(0-默认，禁用，1-启用)")
    private Integer enabledMark;
    @ApiModelProperty(value = "创建人")
    private String creatorUser;
    @ApiModelProperty(value = "创建时间")
    private Long creatorTime;

}
