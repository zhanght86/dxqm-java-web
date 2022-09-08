package jnpf.model.projectgantt;

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
public class ProjectGanttTaskInfoVO {
    @ApiModelProperty(value = "开始时间")
    private Long startTime;
    @ApiModelProperty(value = "完成进度")
    private String schedule;
    @ApiModelProperty(value = "项目工期")
    private String timeLimit;
    @ApiModelProperty(value = "项目名称")
    private String fullName;
    @ApiModelProperty(value = "父级id")
    private String parentId;
    @ApiModelProperty(value = "主键id")
    private String id;
    @ApiModelProperty(value = "结束时间")
    private Long endTime;
    @ApiModelProperty(value = "参与人员")
    private String managerIds;
    @ApiModelProperty(value = "项目描述")
    private String description;
    private String projectId;
    @ApiModelProperty(value = "标记颜色")
    private String signColor;
    @ApiModelProperty(value = "标记")
    private String sign;
}
