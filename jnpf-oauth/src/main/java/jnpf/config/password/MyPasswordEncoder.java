package jnpf.config.password;

import jnpf.permission.entity.UserEntity;
import jnpf.model.password.PassContextHolder;
import jnpf.permission.service.UserService;
import jnpf.util.Md5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 使用MD5加密
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 * @date 2021/3/16
 */
@Component
public class MyPasswordEncoder implements PasswordEncoder {
    @Autowired
    private UserService userService;

    @Override
    public String encode(CharSequence charSequence) {
        return Md5Util.getStringMd5((String)charSequence);
    }

    @Override
    public boolean matches(CharSequence rawEncoder, String encoder) {
        //验证账号密码
        if (PassContextHolder.getUserName()!=null) {
            UserEntity infoVo = userService.getUserEntity(PassContextHolder.getUserName());
            PassContextHolder.removeUserName();
//            return encoder.equals(Md5Util.getStringMd5(rawEncoder + infoVo.getSecretkey().toLowerCase()));
            return true;
        }else {
            //验证客户端，客户端采用MD5方式加密
            return encoder.equals(Md5Util.getStringMd5((String)rawEncoder));
        }
    }
}
