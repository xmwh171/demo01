package com.example.demofirst.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密/校验工具类（BCrypt算法，不可逆）
 */
public class PasswordUtils {

    // BCrypt加密器（线程安全，全局单例）
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 加密密码（明文→密文）
     */
    public static String encrypt(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 校验密码（明文 vs 密文）
     * @param rawPassword 前端传入的明文密码
     * @param encodedPassword 数据库存储的密文密码
     * @return 密码是否匹配
     */
    public static boolean match(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}