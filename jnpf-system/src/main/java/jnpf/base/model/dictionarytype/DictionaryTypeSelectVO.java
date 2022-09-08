package jnpf.base.model.dictionarytype;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class DictionaryTypeSelectVO {
    private String id;
    private String parentId;
    private Boolean hasChildren;
    private List<DictionaryTypeSelectVO> children;
    private String fullName;
    private String enCode;
}
