package com.example.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 自定义application属性
 */

@Component
@ConfigurationProperties(prefix = "spring.jwt")
@Data
public class JWTPropertiesConfig {
    private String secret; //jwt签名密钥
    private String accessTokenExpire; //access token过期时间（minute）
    private String refreshTokenExpire; //refresh token过期时间（minute）
}
