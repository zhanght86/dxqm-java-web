package jnpf.base.model.dictionarydata;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DictionaryDataSelectVO {
    private String id;
    private String parentId;
    private Boolean hasChildren;
    private List<DictionaryDataSelectVO> children;
    private String fullName;
    private String icon;
    private String dictionaryTypeId;
}
