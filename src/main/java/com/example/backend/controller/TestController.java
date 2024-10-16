package com.example.backend.controller;

import com.example.backend.domain.vo.response.RestBean;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    @PreAuthorize("hasAnyAuthority('USER')")
    @GetMapping("/user")
    public RestBean<String> testUser() {
        return RestBean.success("test success! role: user");
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/admin")
    public RestBean<String> testAdmin() {
        return RestBean.success("test success! role: admin");
    }

    @GetMapping("/public")
    public RestBean<String> testPublic() {
        return RestBean.success("test success! public");
    }
}
