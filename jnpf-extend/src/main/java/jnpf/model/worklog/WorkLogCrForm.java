package jnpf.model.worklog;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 10:21
 */
@Data
public class WorkLogCrForm {
    @NotBlank(message = "必填")
    private String title;
    @NotBlank(message = "必填")
    private String question;
    @NotBlank(message = "必填")
    private String todayContent;
    @NotBlank(message = "必填")
    private String tomorrowContent;
    @NotBlank(message = "必填")
    private String toUserId;
}
