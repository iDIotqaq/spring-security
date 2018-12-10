package com.example.demo.security;

import com.example.demo.dao.PermissionDao;
import com.example.demo.dao.UserDao;
import com.example.demo.domain.Permission;
import com.example.demo.domain.User;
import com.example.demo.service.SmsCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 外接数据库用户源
 */
@Component
public class EmailAuthenticationProvider implements AuthenticationProvider {
    private static final Logger log = LoggerFactory.getLogger(EmailAuthenticationProvider.class);
    @Autowired
    private UserDao userDao;

    @Autowired
    private PermissionDao permissionDao;

    @Autowired
    SmsCodeService smsCodeService;


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();

        if (StringUtils.isEmpty(username)) {
            throw new AuthenticationServiceException("User name can't be null");
        }
        String Code = smsCodeService.getCode();
        User user = userDao.findByUserEmail(username);
        if (user == null) {
            throw new AuthenticationServiceException("没有该邮箱的用户，请重新输入");
        }else if(password==null) {
            throw new AuthenticationServiceException("验证码不正确，请重新输入");
        }else if(!password.equals(Code)){
            throw new AuthenticationServiceException("验证码不正确，请重新输入");
        }

        List<Permission> permissions = permissionDao.findByAdminUserId(user.getId());
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (Permission permission : permissions) {
            if (permission != null && permission.getPermission_name() != null) {

                GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(permission.getPermission_name());
                grantedAuthorities.add(grantedAuthority);
            }
        }
        Authentication auth = new UsernamePasswordAuthenticationToken(username, password, grantedAuthorities);
        return auth;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}


