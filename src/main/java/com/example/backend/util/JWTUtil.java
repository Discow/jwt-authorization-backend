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
import org.springframework.util.StringUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JWTUtil {
    @Value("${spring.jwt.secret}")
    private String jwtSecret;
    @Value("${spring.jwt.access-token-expire}")
    private String accessTokenExpire;
    @Value("${spring.jwt.refresh-token-expire}")
    private String refreshTokenExpire;

    public enum TokenType {
        ACCESS,
        REFRESH
    }

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final String JWT_BLACKLIST_PREFIX = "revoked_jwt_token";

    //生成jwt token (header.payload.signature)
    public String generateToken(Map<String, String> claims, TokenType tokenType) {
        JWTCreator.Builder JWTBuilder = JWT.create();
        //设置JWT ID
        JWTBuilder.withJWTId(UUID.randomUUID().toString());
        //设置JWT payload
        if (claims != null) {
            claims.forEach(JWTBuilder::withClaim);
        }
        //设置token类型（access、refresh）
        JWTBuilder.withClaim("typ", tokenType.name());
        //设置JWT expire
        Calendar calendar = Calendar.getInstance();
        switch (tokenType) {
            case ACCESS -> calendar.add(Calendar.MINUTE, Integer.parseInt(accessTokenExpire));
            case REFRESH -> calendar.add(Calendar.MINUTE, Integer.parseInt(refreshTokenExpire));
            default -> throw new IllegalArgumentException("Unexpected token type: " + tokenType);
        }
        JWTBuilder.withExpiresAt(calendar.getTime());
        //设置JWT signature并返回token
        return JWTBuilder.sign(Algorithm.HMAC256(jwtSecret));
    }

    //校验并解析jwt token，校验失败将抛出异常
    public DecodedJWT verifyToken(String token, TokenType tokenType) {
        DecodedJWT decodedJWT = JWT.decode(token);
        //判断token类型
        if (!tokenType.equals(getTokenType(token))) {
            throw new JWTVerificationException("The token type is incorrect");
        }
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

    //获取token类型
    public TokenType getTokenType(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        String typ = decodedJWT.getClaim("typ").asString();
        if (StringUtils.hasText(typ)) {
            return typ.equals(TokenType.ACCESS.name()) ? TokenType.ACCESS : TokenType.REFRESH;
        }
        return null;
    }
}
