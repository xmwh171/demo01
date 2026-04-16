package com.example.demofirst.service.impl;


import com.example.demofirst.service.HelloService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService  // 暴露为 Dubbo 服务
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello " + name + ", 来自 Dubbo 服务";
    }
}