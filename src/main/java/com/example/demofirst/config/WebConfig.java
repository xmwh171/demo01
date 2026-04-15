package com.example.demofirst.config;

import com.example.demofirst.interceptor.ApiCountInterceptor;
import com.example.demofirst.interceptor.TokenInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * Web配置：注册拦截器，指定拦截规则
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private TokenInterceptor tokenInterceptor;

    @Resource
    private ApiCountInterceptor apiCountInterceptor;

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 接口计数拦截器
        registry.addInterceptor(apiCountInterceptor).addPathPatterns("/**");

        // 用户接口拦截器
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/user/**")  // 拦截所有/user开头的接口
                .excludePathPatterns("/user/login", "/user/add","/user/register");  // 放行登录、新增用户接口
    }
}
