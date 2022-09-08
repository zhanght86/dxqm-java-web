package jnpf.service;


import jnpf.base.UserInfo;
import jnpf.permission.entity.UserEntity;
import jnpf.exception.LoginException;
import jnpf.model.login.LoginForm;
import jnpf.model.login.PcUserVO;

/**
 *
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 * @date 2021/3/16
 */
public interface LoginService {

    /**
     * 租戶登录验证
     * @param loginForm
     * @return
     * @throws LoginException
     */
    UserInfo checkTenant(LoginForm loginForm) throws LoginException;

    /**
     * 信息
     * @param userInfoVo
     * @return
     */
    UserInfo userInfo(UserInfo userInfo, UserEntity userInfoVo) throws LoginException;

    /**
     * 验证账号是否可以使用
     * @param account
     * @return
     * @throws LoginException
     */
    boolean isExistUser(String account) throws LoginException;

    /**
     * 获取用户登陆信息
     * @return
     */
    PcUserVO getCurrentUser();

}
