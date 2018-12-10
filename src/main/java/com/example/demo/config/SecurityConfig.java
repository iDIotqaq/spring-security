package com.example.demo.config;


import com.example.demo.security.CustomFilterSecurityInterceptor;
import com.example.demo.security.CustomlAuthoritiesExtractor;
import com.example.demo.security.EmailAuthenticationProvider;
import com.example.demo.security.exception.CustomAccessDeniedHandler;
import com.example.demo.security.exception.CustomAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.Filter;

@Configuration
@EnableWebSecurity
//@EnableOAuth2Sso
@EnableOAuth2Client
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private CustomFilterSecurityInterceptor customFilterSecurityInterceptor;
    @Autowired
    private OAuth2ClientContext oauth2ClientContext;
    @Autowired
    private CustomlAuthoritiesExtractor customlAuthoritiesExtractor;
    @Autowired
    private GithubClientResource githubClientResource;

    @Bean
    public FilterRegistrationBean<OAuth2ClientContextFilter> oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
        FilterRegistrationBean<OAuth2ClientContextFilter> registration = new FilterRegistrationBean<OAuth2ClientContextFilter>();
        registration.setFilter(filter);
        registration.setOrder(-100);
        return registration;
    }
    private Filter ssoFilter() {
        OAuth2ClientAuthenticationProcessingFilter githubFilter = new OAuth2ClientAuthenticationProcessingFilter(
                "/login/github");
        //自定义获取accesstoken的接口
        OAuth2RestTemplate githubTemplate = new OAuth2RestTemplate(githubClientResource.getClient()
                , oauth2ClientContext);
        githubFilter.setRestTemplate(githubTemplate);
        //自定义获取user资源属性的接口
        UserInfoTokenServices tokenServices = new UserInfoTokenServices(
                githubClientResource.getResource().getUserInfoUri(),
                githubClientResource.getClient().getClientId());
        tokenServices.setAuthoritiesExtractor(customlAuthoritiesExtractor);
        tokenServices.setRestTemplate(githubTemplate);
        githubFilter.setTokenServices(tokenServices);
        return githubFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        HttpSecurity httpSecurity = http
                .authorizeRequests()
                .antMatchers("/send","/css/**", "/js/**", "/fonts/**","/code/**", "/login").permitAll()
                .anyRequest().authenticated()
                .and().formLogin()
                .loginPage("/login")
                .failureUrl("/login?error").permitAll()
                //注销行为任意访问
                .and().logout().permitAll()
//                .and().exceptionHandling().accessDeniedPage("/403")
                .and().exceptionHandling().accessDeniedHandler(customAccessDeniedHandler()).and()
//                .authenticationEntryPoint(customAuthenticationEntryPoint())
                .addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(customFilterSecurityInterceptor, FilterSecurityInterceptor.class)
                ;



    }

//    @Autowired
//    private CustomUserService customUserService ;
    @Autowired
    private EmailAuthenticationProvider EmailAuthenticationProvider;
    /**
     * 认证信息管理
     *
     * @param builder
     * @throws Exception
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder builder) throws Exception {
        //builder.userDetailsService(customUserService).passwordEncoder(passwordEncoder());
        builder.authenticationProvider(EmailAuthenticationProvider);
    }

//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }

    @Bean
    public CustomAccessDeniedHandler customAccessDeniedHandler() {
        CustomAccessDeniedHandler cadh= new CustomAccessDeniedHandler();
        cadh.setErrorPage("/403");
        return cadh;
    }

    @Bean
    public CustomAuthenticationEntryPoint customAuthenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint();
    }

}

