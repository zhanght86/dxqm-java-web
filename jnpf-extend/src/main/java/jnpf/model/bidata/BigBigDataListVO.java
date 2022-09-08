package jnpf.model.bidata;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:58
 */
@Data
public class BigBigDataListVO {
    @ApiModelProperty(value = "编码")
    private String enCode;
    @ApiModelProperty(value = "名称")
    private String fullName;
    @ApiModelProperty(value = "主键id")
    private String id;
    @ApiModelProperty(value = "创建时间")
    private Long creatorTime;
}
