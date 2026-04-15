package com.example.demofirst.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus分页插件配置（SpringBoot3适配）
 * @Configuration 注解：告诉 SpringBoot “这是一个配置类，启动时要加载”；
 * MybatisPlusInterceptor：MyBatis-Plus 的插件拦截器，相当于 “拦截所有 MyBatis 的 SQL 执行”；
 * PaginationInnerInterceptor：分页插件，会在你调用 page() 方法时，自动给 SQL 拼接 LIMIT 和 COUNT 语句：
 * 比如你查 pageNum=1&pageSize=5，框架会自动执行 2 条 SQL：
 * SELECT COUNT(*) FROM user WHERE username LIKE '%zhang%'（查总条数）；
 * SELECT * FROM user WHERE username LIKE '%zhang%' LIMIT 0,5（查当前页数据）；
 * 最终把这两个结果封装到 IPage 对象里，返回给你。
 */
@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加MySQL分页插件，自动适配分页语法
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}