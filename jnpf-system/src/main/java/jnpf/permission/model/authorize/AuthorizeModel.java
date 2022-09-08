package jnpf.permission.model.authorize;

import jnpf.util.treeutil.SumTree;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:27
 */
@Data
public class AuthorizeModel extends SumTree {
    private String id;
    private String fullName;
    private String icon;

    private long sortCode;
}
