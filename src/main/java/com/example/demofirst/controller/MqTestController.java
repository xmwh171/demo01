package com.example.demofirst.controller;

import com.example.demofirst.service.MqSenderService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class MqTestController {

    @Resource
    private MqSenderService senderService;

//    @GetMapping("/mq/send")
//    public String send() {
//        senderService.sendUserMsg("用户更新了个人信息，异步记录日志");
//        return "消息已发送";
//    }
}
