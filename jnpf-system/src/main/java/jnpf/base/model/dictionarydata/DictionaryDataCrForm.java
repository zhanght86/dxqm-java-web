package jnpf.base.model.dictionarydata;

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
public class DictionaryDataCrForm {
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "项目代码")
    private String enCode;

    private Integer enabledMark;

    @NotBlank(message = "必填")
    @ApiModelProperty(value = "上级项目名称")
    private String fullName;

    private String description;

    @NotBlank(message = "必填")
    @ApiModelProperty(value = "上级id,没有传0")
    private String parentId;
    private String dictionaryTypeId;
    private long sortCode;
}
