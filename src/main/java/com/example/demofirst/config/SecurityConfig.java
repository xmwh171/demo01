package com.example.demofirst.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置：仅保留BCrypt加密，放行所有接口
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 关闭跨域请求伪造保护（开发环境简化）
                .csrf(csrf -> csrf.disable())
                // 放行所有请求，不做认证拦截
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}