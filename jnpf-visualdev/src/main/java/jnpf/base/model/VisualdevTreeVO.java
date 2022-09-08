package jnpf.base.model;

import jnpf.model.visiual.VisualdevTreeChildModel;
import lombok.Data;

import java.util.List;

/**
 *
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 * @date 2021/3/16
 */
@Data
public class VisualdevTreeVO {
    private String id;
    private String fullName;
    private Boolean hasChildren;
    private List<VisualdevTreeChildModel> children;
}
