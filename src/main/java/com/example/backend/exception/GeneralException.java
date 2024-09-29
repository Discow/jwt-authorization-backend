package com.example.backend.exception;

/**
 * 通用（可预测异常）
 */
public class GeneralException extends RuntimeException {
    public GeneralException(String massage) {
        super(massage);
    }
}
