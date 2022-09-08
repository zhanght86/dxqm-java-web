package jnpf.portal.model;

import com.alibaba.fastjson.annotation.JSONField;
import jnpf.util.treeutil.SumTree;
import lombok.Data;

/**
 *
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 * @date 2021/3/16
 */
@Data
public class PortalSelectModel extends SumTree {
    private String fullName;
    @JSONField(name="category")
    private String  parentId;
}
