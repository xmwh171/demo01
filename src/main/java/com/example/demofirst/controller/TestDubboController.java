package com.example.demofirst.controller;

import com.example.demofirst.service.HelloService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestDubboController {

    @DubboReference  // 远程调用 Dubbo 服务
    private HelloService helloService;

    @GetMapping("/test")
    public String test() {
        return helloService.sayHello("同学");
    }
}
