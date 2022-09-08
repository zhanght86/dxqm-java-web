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
public class ProjectGanttTreeVO {
    private String id;
    private String parentId;
    private String fullName;
    private String startTime;
    private String endTime;
    private String sign;
    private String signColor;
    private String schedule;
    private Boolean hasChildren;
    private List<ProjectGanttTreeVO> children;
}
