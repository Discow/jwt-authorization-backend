package com.example.backend.exception;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.backend.domain.vo.response.RestBean;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(GeneralException.class)
    public RestBean<String> onGeneralException(Exception e) {
        return RestBean.failure(400, e.getMessage());
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public RestBean<String> onAuthorizationDeniedException(Exception e) {
        return RestBean.failure(403, e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public RestBean<String> onNoResourceFoundException(Exception e) {
        return RestBean.failure(404, e.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public RestBean<String> onHttpRequestMethodNotSupportedException(Exception e) {
        return RestBean.failure(405, e.getMessage());
    }

    @ExceptionHandler(JWTVerificationException.class)
    public RestBean<String> onJWTVerificationException(Exception e) {
        return RestBean.failure(401, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public RestBean<String> onOtherException(Exception e) {
        e.printStackTrace();
        return RestBean.failure(500, "未知异常，请联系管理员");
    }
}
