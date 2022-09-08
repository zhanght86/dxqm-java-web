package jnpf.model.login;

import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:49
 */
@Data
public class LoginForm {
    private String account;
    private String password;

    public LoginForm() {
    }

    public LoginForm(String account, String password) {
        this.account = account;
        this.password = password;
    }
}
