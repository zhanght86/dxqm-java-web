package jnpf.base.model.dbtable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class DbTableInfoVO {
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "表名")
    private String table;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "表说明")
    private String tableName;
    private String newTable;
}
