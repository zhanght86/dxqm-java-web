package jnpf.model.home;

import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:53
 */
@Data
public class MyFlowTodoVO {
    private String id;
    private Integer enabledMark;
    private Long startTime;
    private Long endTime;
    private String content;
}
