package com.example.backend.service.impl;

import com.example.backend.domain.entity.User;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.AuthorizeService;
import com.example.backend.util.JWTUtil;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthorizeServiceImpl implements AuthorizeService {
    @Resource
    UserMapper userMapper;
    @Resource
    JWTUtil jwtUtil;

    @Override
    public UserDetails loadUserByUsername(String emailOrName) throws UsernameNotFoundException {
        if (!StringUtils.hasText(emailOrName)) {
            throw new UsernameNotFoundException("请输入用户名");
        }
        User user = userMapper.findByEmailOrUsername(emailOrName);
        if (user == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    @Override
    public Map<String, String> getToken(String emailOrName) {
        User user = userMapper.findByEmailOrUsername(emailOrName);
        HashMap<String, String> map = new HashMap<>();
        //设置claim
        HashMap<String, String> claim = new HashMap<>();
        claim.put("uid", user.getUid().toString());
        claim.put("uname", user.getUsername());
        claim.put("role", user.getRole().name());
        map.put("token", jwtUtil.generateToken(claim));
        return map;
    }
}
