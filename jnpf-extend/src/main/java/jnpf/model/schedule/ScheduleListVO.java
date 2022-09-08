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
public class ScheduleListVO{
    @ApiModelProperty(value = "日程主键")
    private String id;
    @ApiModelProperty(value = "开始时间")
    private Long startTime;
    @ApiModelProperty(value = "开始时间")
    private Long endTime;
    @ApiModelProperty(value = "颜色")
    private String colour;
    @ApiModelProperty(value = "日程内容")
    private String content;
}