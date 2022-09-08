package jnpf.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.config.ConfigValueUtil;
import jnpf.database.data.DataSourceContextHolder;
import jnpf.permission.entity.UserEntity;
import jnpf.exception.LoginException;
import jnpf.model.login.LoginForm;
import jnpf.model.login.LoginVO;
import jnpf.model.login.PcUserVO;
import jnpf.permission.service.UserService;
import jnpf.service.LoginService;
import jnpf.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录控制器
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Api(tags = "登陆数据", value = "oauth")
@Slf4j
@RestController
@RequestMapping("/api/oauth")
public class LoginController {

    @Autowired
    private UserService userService;
    @Autowired
    private TokenEndpoint tokenEndpoint;
    @Autowired
    private LoginService loginService;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private ConfigValueUtil configValueUtil;


    @ApiOperation("登陆")
    @PostMapping(value = "/Login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ActionResult<LoginVO> login(Principal principal, @RequestParam Map<String, String> parameters) throws LoginException {
        UserInfo userInfo = new UserInfo();
        LoginForm loginForm = JsonUtil.getJsonToBean(parameters, LoginForm.class);
        if (Boolean.parseBoolean(configValueUtil.getMultiTenancy())) {
            userInfo = loginService.checkTenant(loginForm);
            //设置租户
            DataSourceContextHolder.setDatasource(userInfo.getTenantId(), userInfo.getTenantDbConnectionString());
        }
        loginService.isExistUser(loginForm.getAccount().trim());
        UserEntity entity = userService.getUserEntity(loginForm.getAccount());
        userInfo = loginService.userInfo(userInfo, entity);
        //写入会话
        userProvider.add(userInfo);
        //验证账号密码
        parameters.put("username", loginForm.getAccount());
        OAuth2AccessToken oAuth2AccessToken;
        try {
            oAuth2AccessToken = tokenEndpoint.postAccessToken(principal, parameters).getBody();
        } catch (HttpRequestMethodNotSupportedException e) {
            throw new LoginException("账号密码错误");
        }
        //修改前一次登陆时间等信息
        entity.setPrevLogIp(IpUtil.getIpAddr());
        entity.setPrevLogTime(DateUtil.getNowDate());
        entity.setLastLogIp(IpUtil.getIpAddr());
        entity.setLastLogTime(DateUtil.getNowDate());
        entity.setLogSuccessCount(entity.getLogSuccessCount() != null ? entity.getLogSuccessCount() + 1 : 1);
        userService.update(entity.getId(), entity);
        //登陆日志记录在JwtTokenEnhancer类中
        //获取主题
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(oAuth2AccessToken.getTokenType() + " " + oAuth2AccessToken.getValue());
        loginVO.setTheme(entity.getTheme() == null ? "classic" : entity.getTheme());
        return ActionResult.success(loginVO);
    }


    /**
     * 验证密码
     *
     * @return
     */
    @ApiOperation("锁屏解锁登录")
    @PostMapping("/LockScreen")
    public ActionResult lockScreen(@RequestBody LoginForm loginForm) throws LoginException {
        UserEntity userEntity = userService.getUserEntity(loginForm.getAccount());
        if (!Md5Util.getStringMd5(loginForm.getPassword().toLowerCase() + userEntity.getSecretkey().toLowerCase()).equals(userEntity.getPassword())) {
            throw new LoginException("账户或密码错误，请重新输入。");
        }
        return ActionResult.success("验证成功");
    }

    /**
     * 登录注销
     *
     * @return
     */
    @ApiOperation("退出")
    @GetMapping("/Logout")
    public ActionResult logout() {
        if (userProvider.get() != null) {
            userProvider.removeCurrent();
        }
        return ActionResult.success("注销成功");
    }

    /**
     * 获取用户登录信息
     *
     * @return
     */
    @ApiOperation("获取用户登录信息")
    @GetMapping("/CurrentUser")
    public ActionResult<PcUserVO> currentUser() throws LoginException {
        PcUserVO pcUserVO = loginService.getCurrentUser();
        if (pcUserVO == null) {
            throw new LoginException("账户异常");
        }
        return ActionResult.success(pcUserVO);
    }

}
