package jnpf.service.impl;


import jnpf.permission.entity.UserEntity;
import jnpf.model.password.PassContextHolder;
import jnpf.permission.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 * @date 2021/3/16
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        UserDetails user = null;
        UserEntity userEntity = userService.getUserEntity(username);
        Collection<SimpleGrantedAuthority> list = new ArrayList();
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority("aa");
        list.add(simpleGrantedAuthority);
        user = new User(username, userEntity.getPassword(), list);
        PassContextHolder.setUserName(username);
        return user;
    }

}
