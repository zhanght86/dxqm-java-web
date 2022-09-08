package jnpf.config.jwt;


import jnpf.base.UserInfo;
import jnpf.database.data.DataSourceContextHolder;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.service.LogService;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 根据需要配置Jwt内容增强器
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 * @date 2021/3/16
 */
@Component
public class JwtTokenEnhancer implements TokenEnhancer {

    @Autowired
    private LogService logService;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private UserService userService;

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken oAuth2AccessToken, OAuth2Authentication oAuth2Authentication) {
        UserEntity userEntity=userService.getUserEntity(oAuth2Authentication.getName());
        UserInfo userInfo=userProvider.get(userEntity.getId(), DataSourceContextHolder.getDatasourceId());

        //创建map，将需要增加的内容放置到map中
        Map<String,Object> map = new HashMap<>(16);
        //移除在线
        userProvider.removeWebSocket(userInfo);
        map.put("token",userInfo.getId());
        //写入日志
        logService.writeLogAsync(userInfo.getUserId(),userInfo.getUserName()+"/"+userInfo.getUserAccount(),"登录成功");
        ((DefaultOAuth2AccessToken)oAuth2AccessToken).setAdditionalInformation(map);
        return oAuth2AccessToken;
    }
}
