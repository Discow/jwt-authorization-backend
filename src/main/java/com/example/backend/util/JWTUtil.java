package com.example.backend.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JWTUtil {
    @Value("${spring.jwt.secret}")
    private String jwtSecret;
    @Value("${spring.jwt.expire}")
    private String expire;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final String JWT_BLACKLIST_PREFIX = "revoked_jwt_token";

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
        DecodedJWT decodedJWT = JWT.decode(token);
        String jwtId = decodedJWT.getId();
        String redisKey = JWT_BLACKLIST_PREFIX + ":" + jwtId;
        //校验令牌是否已吊销
        String revokedToken = stringRedisTemplate.opsForValue().get(redisKey);
        if (revokedToken != null) {
            throw new JWTVerificationException("The Token has been revoked");
        }
        return JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(token);
    }

    //吊销JWT令牌
    public void revokeToken(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        String jwtId = decodedJWT.getId();
        Date expiresAt = decodedJWT.getExpiresAt();
        //将token存入吊销名单
        String redisKey = JWT_BLACKLIST_PREFIX + ":" + jwtId;
        Date now = new Date();
        long timeout = expiresAt.getTime() - now.getTime();
        if (timeout < 0) return; //如果令牌已过期，则无需再吊销
        stringRedisTemplate.opsForValue().set(redisKey, "", timeout, TimeUnit.MILLISECONDS);
    }
}
