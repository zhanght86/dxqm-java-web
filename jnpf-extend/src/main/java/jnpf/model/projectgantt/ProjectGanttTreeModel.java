package jnpf.model.projectgantt;

import jnpf.util.treeutil.SumTree;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 10:21
 */
@Data
public class ProjectGanttTreeModel extends SumTree {

    private Integer schedule;

    private String fullName;

    private long startTime;

    private long endTime;

    private String signColor;
    private String sign;
}
