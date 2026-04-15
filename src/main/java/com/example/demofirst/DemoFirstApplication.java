package com.example.demofirst;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@MapperScan("com.example.demofirst.mapper")  // 扫描Mapper接口所在包
@EnableCaching
public class DemoFirstApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoFirstApplication.class, args);
    }
}
