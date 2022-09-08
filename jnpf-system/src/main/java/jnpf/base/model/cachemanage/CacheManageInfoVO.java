package jnpf.base.model.cachemanage;

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
public class CacheManageInfoVO {
    @ApiModelProperty(value = "名称")
    private String name;
    @ApiModelProperty(value = "值")
    private String value;
}
