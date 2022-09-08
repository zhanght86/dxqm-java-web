package jnpf.model.worklog;

import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 10:21
 */
@Data
public class WorkLogListVO {
    private String id;
    private String title;
    private String question;
    private Long creatorTime;
    private String todayContent;
    private String tomorrowContent;
    private String toUserId;
}