package com.example.backend.exception;

import com.example.backend.domain.vo.response.RestBean;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(GeneralException.class)
    public RestBean<String> generalException(Exception e) {
        return RestBean.failure(400, e.getMessage());
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public RestBean<String> authorizationDeniedException(Exception e) {
        return RestBean.failure(403, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public RestBean<String> exceptionHandler(Exception e) {
        e.printStackTrace();
        return RestBean.failure(500, "未知异常，请联系管理员");
    }
}
