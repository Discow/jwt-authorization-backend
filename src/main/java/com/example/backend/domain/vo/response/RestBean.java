package com.example.backend.domain.vo.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.io.Serializable;

@Data
public class RestBean<T> implements Serializable {
    private int code;
    private String msg;
    private T data;

    private RestBean(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> RestBean<T> success() {
        return new RestBean<>(200, null, null);
    }

    public static <T> RestBean<T> success(String msg) {
        return new RestBean<>(200, msg, null);
    }

    public static <T> RestBean<T> success(T data) {
        return new RestBean<>(200, null, data);
    }

    public static <T> RestBean<T> failure(int code) {
        return new RestBean<>(code, null, null);
    }

    public static <T> RestBean<T> failure(int code, String msg) {
        return new RestBean<>(code, msg, null);
    }

    public static <T> RestBean<T> failure(int code, T data) {
        return new RestBean<>(code, null, data);
    }

    public static <T> RestBean<T> failure(int code, String msg, T data) {
        return new RestBean<>(code, msg, data);
    }

    public String asJsonString() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
