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
public class DictionaryTypeListVO {
    private String id;
    private String parentId;
    private Boolean hasChildren;
    private Integer isTree;
    private List<DictionaryTypeListVO> children;
    private String fullName;
    private String enCode;
    private Long sortCode;
}
