package jnpf.model.document;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:58
 */
@Data
public class DocumentCrForm {
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "文件夹名称")
    private String fullName;
    @NotNull(message = "必填")
    @ApiModelProperty(value = "文档分类")
    private Integer type;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "文档父级")
    private String parentId;
}
