package com.example.demofirst.config;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZkConfig {

//    // 后面用 Docker 启动后换成你的地址
//    private static final String ZK_ADDRESS = "127.0.0.1:2181";
//
//
//    @Bean
//    public CuratorFramework curatorFramework() {
//        // 重试策略
//        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
//
//        CuratorFramework client = CuratorFrameworkFactory.builder()
//                .connectString(ZK_ADDRESS)
//                .retryPolicy(retryPolicy)
//                .namespace("demo") // 命名空间，自动挂在 /demo 下
//                .build();
//
//        client.start();
//        return client;
//    }

}
