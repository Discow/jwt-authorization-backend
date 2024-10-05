package com.example.backend.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Map;

public interface AuthorizeService extends UserDetailsService {
    //获取新令牌
    Map<String, String> getNewToken(String username);

    //刷新令牌
    Map<String, String> refreshToken(String refreshToken, String accessToken);
}
