package com.example.backend.service.impl;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.backend.domain.entity.User;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.AuthorizeService;
import com.example.backend.util.JWTUtil;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static com.example.backend.util.JWTUtil.TokenType;

@Service
public class AuthorizeServiceImpl implements AuthorizeService {
    @Resource
    UserMapper userMapper;
    @Resource
    JWTUtil jwtUtil;

    @Override
    public UserDetails loadUserByUsername(String emailOrName) throws UsernameNotFoundException {
        if (!StringUtils.hasText(emailOrName)) {
            throw new UsernameNotFoundException("请输入用户名");
        }
        User user = userMapper.findByEmailOrUsername(emailOrName);
        if (user == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    @Override
    public Map<String, String> getNewToken(String emailOrName) {
        User user = userMapper.findByEmailOrUsername(emailOrName);
        HashMap<String, String> map = new HashMap<>();
        //设置claim
        HashMap<String, String> accessTokenClaim = new HashMap<>();
        accessTokenClaim.put("uid", user.getUid().toString());
        accessTokenClaim.put("uname", user.getUsername());
        accessTokenClaim.put("role", user.getRole().name());
        HashMap<String, String> refreshTokenClaim = new HashMap<>();
        refreshTokenClaim.put("uname", user.getUsername());
        map.put("accessToken", jwtUtil.generateToken(accessTokenClaim, TokenType.ACCESS));
        map.put("refreshToken", jwtUtil.generateToken(refreshTokenClaim, TokenType.REFRESH));
        return map;
    }

    @Override
    public Map<String, String> refreshToken(String refreshToken, String accessToken) {
        //校验refresh token
        DecodedJWT decodedJWT = jwtUtil.verifyToken(refreshToken, TokenType.REFRESH);
        String username = decodedJWT.getClaim("uname").asString();
        //注销旧token
        jwtUtil.revokeToken(refreshToken);
        jwtUtil.revokeToken(accessToken);
        return getNewToken(username);
    }
}
