package com.example.backend.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Map;

public interface AuthorizeService extends UserDetailsService {
    Map<String, String> getToken(String username);
}
