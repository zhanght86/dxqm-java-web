package jnpf.base.model.dblink;

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
public class DbLinkSelectorListVO {
    @ApiModelProperty(value = "主键")
    private String id;
    @ApiModelProperty(value = "数据库类型")
    private String fullName;
    @ApiModelProperty(value = "数据库名称")
    private String dbType;
}
