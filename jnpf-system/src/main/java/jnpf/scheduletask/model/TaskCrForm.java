package jnpf.scheduletask.model;

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
public class TaskCrForm {
    @NotBlank(message = "必填")
    private String fullName;
    @NotBlank(message = "必填")
    private String executeType;
    private String description;
    @NotBlank(message = "必填")
    private String executeContent;
    private long sortCode;
    private String enCode;
}
