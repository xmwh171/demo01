package com.example.demofirst.annotation;

import java.lang.annotation.*;

/**
 * 防重复提交注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoRepeatSubmit {
    // 锁过期时间（秒），默认2秒（用户正常点击的间隔一般大于2秒）
    long expire() default 2;
}
