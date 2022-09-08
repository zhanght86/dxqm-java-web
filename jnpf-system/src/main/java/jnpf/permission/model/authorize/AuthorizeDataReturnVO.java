package jnpf.permission.model.authorize;

import lombok.Data;

import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:27
 */
@Data
public class AuthorizeDataReturnVO {
    List<AuthorizeDataReturnModel> list;
    List<String> ids;
    /**
     * all字段里面不包括菜单id
     */
    List<String> all;
}
