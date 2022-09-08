package jnpf.base.model.dictionarydata;

import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class DictionaryDataInfoVO {
    private String id;
    private String parentId;
    private String description;
    private String fullName;
    private String enCode;
    private Integer enabledMark;
    private String dictionaryTypeId;
    private Long sortCode;
}
