package com.example.demofirst.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 手机号校验器实现
 */
public class PhoneValidator implements ConstraintValidator<Phone, String> {

    // 手机号正则（11位，以1开头）
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        // 空值不校验（如需必填，搭配@NotBlank）
        if (phone == null || phone.isEmpty()) {
            return true;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }
}
