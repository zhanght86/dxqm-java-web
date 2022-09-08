package jnpf.base.model.dictionarytype;

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
public class DictionaryTypeCrForm {
    @NotBlank(message = "必填")
   private String parentId;
    @NotBlank(message = "必填")
    private String fullName;
    @NotBlank(message = "必填")
    private String enCode;
    @NotNull(message = "必填")
    private Integer isTree;
    private String description;
    private long sortCode;
}
