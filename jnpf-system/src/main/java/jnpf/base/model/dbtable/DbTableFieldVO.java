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
public class DbTableFieldVO {
    @ApiModelProperty(value = "字段名")
    private String field;
    @ApiModelProperty(value = "字段说明")
    private String fieldName;
    @ApiModelProperty(value = "数据类型")
    private String dataType;
    @ApiModelProperty(value = "数据长度")
    private String dataLength;
    @ApiModelProperty(value = "主键")
    private Integer primaryKey;
    @ApiModelProperty(value = "允许空")
    private Integer allowNull;
}
