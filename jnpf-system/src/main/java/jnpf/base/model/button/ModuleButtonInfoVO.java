package jnpf.base.model.button;

import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class ModuleButtonInfoVO {
    private String enCode;
    private Integer enabledMark;
    private String fullName;
    private String icon;
    private String id;
    private String parentId;
    private String description;
    private String moduleId;
    private Long sortCode;
}
