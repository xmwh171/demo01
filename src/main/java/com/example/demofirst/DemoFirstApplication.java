package com.example.demofirst;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

@EnableDubbo
@SpringBootApplication
@MapperScan("com.example.demofirst.mapper")  // 扫描Mapper接口所在包
@EnableCaching
public class DemoFirstApplication {

    public static void main(String[] args) {

        SpringApplication.run(DemoFirstApplication.class, args);
    }
}
