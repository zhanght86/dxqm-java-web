package jnpf.model.schedule;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 10:21
 */
@Data
public class ScheduleInfoVO {
    @ApiModelProperty(value = "日程主键")
    private String id;
    @ApiModelProperty(value = "开始时间(时间戳)")
    private Long startTime;
    @ApiModelProperty(value = "结束时间(时间戳)")
    private Long endTime;
    @ApiModelProperty(value = "日程内容")
    private String content;
    @ApiModelProperty(value = "提醒设置",example = "1")
    private Integer early;
    @ApiModelProperty(value = "APP提醒(1-提醒，0-不提醒)",example = "1")
    private Integer appAlert;
    @ApiModelProperty(value = "日程颜色")
    private String colour;
    @ApiModelProperty(value = "颜色样式")
    private String colourCss;
    @ApiModelProperty(value = "微信提醒(1-提醒，0-不提醒)",example = "1")
    private Integer weChatAlert;
    @ApiModelProperty(value = "邮件提醒(1-提醒，0-不提醒)",example = "1")
    private Integer mailAlert;
    @ApiModelProperty(value = "短信提醒(1-提醒，0-不提醒)",example = "1")
    private Integer mobileAlert;
}