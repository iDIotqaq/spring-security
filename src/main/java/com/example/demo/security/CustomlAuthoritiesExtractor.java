package com.example.demo.security;

import com.example.demo.dao.PermissionDao;
import com.example.demo.dao.UserDao;
import com.example.demo.domain.Permission;
import com.example.demo.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.FixedAuthoritiesExtractor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: zxx
 * @Date: 2018/12/10 15:55
 * @Version 1.0
 */
@Component
public class CustomlAuthoritiesExtractor extends FixedAuthoritiesExtractor {

    Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private UserDao userDao;

    @Autowired
    private PermissionDao permissionDao;
    @Override
    public List<GrantedAuthority> extractAuthorities(Map<String, Object> map) {
        // 默认设置为ADMIN
        String loginName = (String)map.get("login");
        User user = userDao.findByUserName(loginName);

        System.out.println(user);
        log.info(loginName);
        String authorities = "ROLE_ADMIN";
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);
        List<Permission> permissions = permissionDao.findByAdminUserId(user.getId());
        grantedAuthorities = new ArrayList<>();
        for (Permission permission : permissions) {
            if (permission != null && permission.getPermission_name() != null) {

                GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(permission.getPermission_name());
                grantedAuthorities.add(grantedAuthority);
            }
        }
        return grantedAuthorities;
    }
}