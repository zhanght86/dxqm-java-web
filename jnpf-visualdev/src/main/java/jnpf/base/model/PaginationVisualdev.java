package jnpf.base.model;

import lombok.Data;

/**
 *
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 * @date 2021/3/16
 */
@Data
public class PaginationVisualdev{
   private Integer type=1;
   private String keyword="";
   /**
    *0-在线开发(无表)，1-表单设计(有表)
    */
   private String  model="0";
}
