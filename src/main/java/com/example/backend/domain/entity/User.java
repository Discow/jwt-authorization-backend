package com.example.backend.domain.entity;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    private Integer uid;
    private String email;
    private String username;
    private String password;
    private Role role;
    private Date regDate;

    public enum Role {
        USER,
        ADMIN
    }
}
