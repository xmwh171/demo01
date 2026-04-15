package com.example.demofirst.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    // 限流时间窗口（秒），默认60秒
    long window() default 60;
    // 时间窗口内最大请求次数，默认10次
    long limit() default 10;
    // 限流标识前缀（区分不同接口）
    String prefix() default "rate_limit:";
}
