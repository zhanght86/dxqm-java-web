package jnpf.permission.model.user;

import lombok.Data;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-05-29
 */
@Data
public class UserIdListVo {
    /**
     * 用户名称
     */
    private String fullName;

    /**
     * 用户id
     */
    private String id;
}
