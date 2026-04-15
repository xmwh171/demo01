package com.example.demofirst.config;

import com.example.demofirst.plugin.DataPermissionPlugin;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.demofirst.mapper") // 扫描Mapper包
public class MyBatisConfig {

    // 注册数据权限插件
    @Bean
    public DataPermissionPlugin dataPermissionPlugin() {
        return new DataPermissionPlugin();
    }

}
