package com.example.backend.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Map;
import java.util.UUID;

@Component
public class JWTUtil {
    @Value("${spring.jwt.secret}")
    private String jwtSecret;
    @Value("${spring.jwt.expire}")
    private String expire;

    //生成jwt token (header.payload.signature)
    public String generateToken(Map<String, String> claims) {
        JWTCreator.Builder JWTBuilder = JWT.create();
        //设置JWT ID
        JWTBuilder.withJWTId(UUID.randomUUID().toString());
        //设置JWT payload
        if (claims != null) {
            claims.forEach(JWTBuilder::withClaim);
        }
        //设置JWT expire
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, Integer.parseInt(expire));
        JWTBuilder.withExpiresAt(calendar.getTime());
        //设置JWT signature并返回token
        return JWTBuilder.sign(Algorithm.HMAC256(jwtSecret));
    }

    public String generateToken() {
        return this.generateToken(null);
    }

    //校验并解析jwt token，校验失败将抛出异常
    public DecodedJWT verifyToken(String token) {
        return JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(token);
    }
}
