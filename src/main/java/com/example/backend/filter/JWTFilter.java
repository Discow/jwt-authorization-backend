package com.example.backend.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.backend.domain.vo.response.RestBean;
import com.example.backend.util.JWTUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 用于解析并验证JWT令牌
 */

@Component
public class JWTFilter extends OncePerRequestFilter {
    @Resource
    JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        response.setContentType("application/json;charset=utf-8");
        String jwtToken = request.getHeader("Authorization");
        //如果存在JWT token则解析并验证令牌
        if (StringUtils.hasText(jwtToken)) {
            try {
                DecodedJWT decodedJWT = jwtUtil.verifyToken(jwtToken); //验证失败将会抛出异常
                String username = decodedJWT.getClaim("uname").asString();
                String role = decodedJWT.getClaim("role").asString();
                //将验证信息放入SecurityContextHolder
                UserDetails userDetails = User
                        .withUsername(username)
                        .password("") //使用token验证无需密码
                        .authorities(role)
                        .build();
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(token);
            } catch (JWTVerificationException e) {
                response.getWriter().write(RestBean.failure(401, e.getMessage()).asJsonString());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
