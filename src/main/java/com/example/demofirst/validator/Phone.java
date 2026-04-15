package com.example.demofirst.validator;


//import javax.validation.Constraint;
//import javax.validation.Payload;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;


/**
 * 自定义手机号校验注解
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = PhoneValidator.class) // 关联校验器
public @interface Phone {

    // 默认提示信息
    String message() default "手机号格式错误";

    // 分组（可选）
    Class<?>[] groups() default {};

    // 负载（可选）
    Class<? extends Payload>[] payload() default {};
}
