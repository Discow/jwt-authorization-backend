package com.example.backend.controller;

import com.example.backend.domain.vo.response.RestBean;
import com.example.backend.service.AuthorizeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用于身份验证的控制器
 * 此时用户处于未验证状态
 */

@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {
    @Resource
    AuthorizeService authorizeService;

    @PostMapping("/refresh-token")
    public RestBean<?> refreshToken(String refreshToken, String accessToken) {
        return RestBean.success(authorizeService.refreshToken(refreshToken, accessToken));
    }
}
