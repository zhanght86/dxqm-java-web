package jnpf.base.model.column;

import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class ModuleColumnInfoVO {
    private Integer enabledMark;

    private String fullName;

    private String description;

    private String enCode;

    private String id;

    private String bindTable;

    private String bindTableName;

    private String moduleId;
}
