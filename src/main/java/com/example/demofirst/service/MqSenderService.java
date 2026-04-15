package com.example.demofirst.service;

import com.example.demofirst.config.RabbitMQConfig;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class MqSenderService {

//    @Resource
//    private RabbitTemplate rabbitTemplate;

//    public void sendUserMsg(String content) {
//        rabbitTemplate.convertAndSend(
//                RabbitMQConfig.USER_EXCHANGE,
//                RabbitMQConfig.USER_ROUTING_KEY,
//                content
//        );
//        System.out.println("【发送成功】" + content);
//    }

}
