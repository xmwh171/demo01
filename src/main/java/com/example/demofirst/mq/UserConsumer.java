package com.example.demofirst.mq;

import com.example.demofirst.config.RabbitMQConfig;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UserConsumer {

//    @RabbitListener(queues = RabbitMQConfig.USER_QUEUE)
//    public void receive(String msg, Channel channel, Message message) throws IOException {
//
//        long tag = message.getMessageProperties().getDeliveryTag();
//
//        try {
//            System.out.println("【消费消息】" + msg);
//
//            // 业务处理：记录日志 / 发短信 / 发邮件
//
//            // 手动确认
//            channel.basicAck(tag, false);
//
//        } catch (Exception e) {
//            // 消费失败，重回队列
//            channel.basicNack(tag, false, true);
//        }
//    }
}
