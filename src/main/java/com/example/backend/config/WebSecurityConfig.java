package com.example.backend.config;

import com.example.backend.domain.vo.response.RestBean;
import com.example.backend.filter.JWTFilter;
import com.example.backend.service.AuthorizeService;
import com.example.backend.util.JWTUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Configuration
@EnableMethodSecurity //开启基于方法的授权
@Import(BCryptPasswordEncoder.class)
public class WebSecurityConfig {
    @Resource
    AuthorizeService authorizeService;
    @Resource
    JWTFilter jwtFilter;
    @Resource
    JWTUtil jwtUtil;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //所有请求都需要验证
                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers("/api/auth/**").permitAll() //放行验证相关的api
                            .anyRequest().authenticated();
                })
                //使用表单登录
                .formLogin(conf -> {
                    conf
                            .loginProcessingUrl("/api/auth/login") //前后端分离不需要配置登录页面地址（loginPage）
                            .successHandler(this::onAuthenticationSuccess)
                            .failureHandler(this::onAuthenticationFailure);
                })
                //退出登录
                .logout(conf -> {
                    conf
                            .logoutUrl("/api/auth/logout")
                            .addLogoutHandler(this::logoutHandler)
                            .logoutSuccessHandler(this::onLogoutSuccess);
                })
                //异常处理
                .exceptionHandling(conf -> {
                    //未验证时访问需要验证的资源
                    conf.authenticationEntryPoint(this::onAuthenticationFailure);
                })
                //自定义验证
                .userDetailsService(authorizeService)
                //添加JWT过滤器
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                //已配置JWT认证，使用无状态session
                .sessionManagement(conf -> {
                    conf.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                //已配置JWT认证，且使用Header传递令牌，csrf防护可以安全的关闭
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(RestBean.success(authorizeService.getToken(authentication.getName())).asJsonString());
    }

    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(RestBean.failure(401, "Unauthorized").asJsonString());
    }

    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(RestBean.success().asJsonString());
    }

    public void logoutHandler(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        //吊销JWT令牌
        String jwtToken = request.getHeader("Authorization");
        if (StringUtils.hasText(jwtToken)) {
            jwtUtil.revokeToken(jwtToken);
        }
    }
}
