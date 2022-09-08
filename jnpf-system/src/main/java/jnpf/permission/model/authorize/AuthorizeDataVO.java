package jnpf.permission.model.authorize;

import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:27
 */
@Data
public class AuthorizeDataVO {
    private AuthorizeDataReturnVO module;
    private AuthorizeDataReturnVO button;
    private AuthorizeDataReturnVO column;
    private AuthorizeDataReturnVO resource;

}
