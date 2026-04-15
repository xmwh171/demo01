package com.example.demofirst.dto;

import com.example.demofirst.validator.Phone;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// 在DTO中使用自定义注解
@Data
public class UserDTO {
    @NotBlank(message = "手机号不能为空")
    @Phone(message = "手机号格式错误（需11位）")
    private String phone;
}
