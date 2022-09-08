package jnpf.base.model.dictionarytype;

import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class DictionaryTypeInfoVO {
    private String id;
    private String parentId;
    private String fullName;
    private String enCode;
    private Integer isTree;
    private String description;
    private Long sortCode;
}
