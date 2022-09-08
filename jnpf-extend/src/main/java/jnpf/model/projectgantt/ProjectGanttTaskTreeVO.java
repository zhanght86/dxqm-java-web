package jnpf.model.projectgantt;

import lombok.Data;

import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 10:21
 */
@Data
public class ProjectGanttTaskTreeVO {
    private String id;
    private String parentId;
    private Boolean hasChildren;
    private String fullName;
    private Integer schedule;
    private String projectId;
    private Long startTime;
    private Long endTime;
    private String signColor;
    private String sign;
    private List<ProjectGanttTaskTreeVO> children;
}
