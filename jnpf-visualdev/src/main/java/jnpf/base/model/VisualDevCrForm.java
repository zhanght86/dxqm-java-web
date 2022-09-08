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
public class VisualDevCrForm {
    private String fullName;
    private String enCode;
    private String type;
    private String description;
    private String formData;
    private String columnData;
    private String tables;
    private String category;
    private Integer state=0;
    private String dbLinkId;
    private String webType;
    private String flowTemplateJson;
}
