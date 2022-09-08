package jnpf.scheduletask.model;

import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class TaskLogVO {
   private String description;

  private String id;

  private Integer runResult;

   private Long runTime;
}
