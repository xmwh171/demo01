package com.example.demofirst.config;

import com.example.demofirst.Result;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ResultConfig {

    @Resource
    public void setEnvironment(Environment environment) {
        // 把Spring的Environment赋值给Result类的静态变量
        Result.setEnvironment(environment);
    }
}
