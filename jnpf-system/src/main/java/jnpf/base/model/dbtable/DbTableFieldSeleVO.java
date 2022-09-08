package jnpf.base.model.dbtable;

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
public class DbTableFieldSeleVO {
    @ApiModelProperty(value = "字段名")
    private String field;
    @ApiModelProperty(value = "字段说明")
    private String fieldName;
}
