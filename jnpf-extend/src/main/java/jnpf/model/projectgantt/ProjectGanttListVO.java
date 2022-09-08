package jnpf.model.projectgantt;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 10:21
 */
@Data
public class ProjectGanttListVO {
    private String id;
    private String enCode;
    private String fullName;
    private BigDecimal timeLimit;
    private Long startTime;
    private Long endTime;
    private Integer schedule;
    private String managerIds;
    private Integer state;
    private List<ProjectGanttManagerModel> managersInfo;
}
