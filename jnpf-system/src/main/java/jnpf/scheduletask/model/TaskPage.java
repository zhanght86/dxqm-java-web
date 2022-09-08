package jnpf.scheduletask.model;

import jnpf.base.Pagination;
import lombok.Data;

/**
 *
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date  2021/5/18
 */
@Data
public class TaskPage extends Pagination {
    private Integer runResult;

    private String startTime;
    private String endTime;
}
