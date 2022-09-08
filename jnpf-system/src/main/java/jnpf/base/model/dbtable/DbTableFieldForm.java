package jnpf.base.model.dbtable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class DbTableFieldForm{
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "字段名")
    private String field;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "字段说明")
    private String fieldName;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "数据类型")
    private String dataType;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "数据长度")
    private String dataLength;
    @NotNull(message = "必填")
    @ApiModelProperty(value = "允许空")
    private Integer allowNull;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "插入位置")
    private String index;
    @ApiModelProperty(value = "主键")
    private Integer primaryKey;

}
