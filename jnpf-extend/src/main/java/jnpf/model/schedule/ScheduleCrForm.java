package jnpf.model.schedule;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 10:21
 */
@Data
public class ScheduleCrForm {
    @ApiModelProperty(value = "必填")
    private long startTime;
    @NotNull(message = "必填")
    @ApiModelProperty(value = "结束时间(时间戳)")
    private long endTime;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "日程内容")
    private String content;
    @ApiModelProperty(value = "提醒设置")
    private Integer early;
    @ApiModelProperty(value = "APP提醒(1-提醒，0-不提醒)")
    private Integer appAlert;
    @ApiModelProperty(value = "日程颜色")
    private String colour;
    @ApiModelProperty(value = "颜色样式")
    private String colourCss;
    @ApiModelProperty(value = "微信提醒(1-提醒，0-不提醒)")
    private Integer weChatAlert;
    @ApiModelProperty(value = "邮件提醒(1-提醒，0-不提醒)")
    private Integer mailAlert;
    @ApiModelProperty(value = "短信提醒(1-提醒，0-不提醒)")
    private Integer mobileAlert;

}
