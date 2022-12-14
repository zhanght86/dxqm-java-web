package jnpf.portal.model;


import lombok.Data;

/**
 *
 *
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @author 管理员/admin
 * @date 2020-10-21 14:23:30
 */
@Data
public class PortalCrForm  {
     private String fullName;
     private String enCode;
     private Integer enabledMark;
     private String description;
     private String formData;
     private String category;
     private Long sortCode;

}
