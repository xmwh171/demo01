package com.example.demofirst.utils;

import com.example.demofirst.service.OperateLogService;
import com.example.demofirst.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Token工具类：生成、校验、删除Token
 */
@Component  // 交给Spring管理，可注入
public class TokenUtils {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final long TOKEN_EXPIRE_TIME  = 7200;

    /**
     * 生成Token并存储到Redis
     * @param userId 用户ID（关联Token和用户）
     * @return 唯一Token字符串
     */
    public String generateToken(Long userId) {
        // 1. 生成UUID作为Token（唯一不重复）
        String token = "TOKEN_" + UUID.randomUUID().toString().replace("-", "");
        // 2. 存储到Redis：key=token，value=userId，设置过期时间
        stringRedisTemplate.opsForValue().set(token, userId.toString(), TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);
        return token;
    }

    /**
     * 校验Token是否有效
     * @param token 前端传入的Token
     * @return 有效返回用户ID，无效返回null
     */
    public Long validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        // 从Redis获取用户ID
        String userIdStr = stringRedisTemplate.opsForValue().get(token);
        if (userIdStr == null) {
            return null;
        }
        // 延长Token有效期（滑动过期：每次访问刷新过期时间）
        stringRedisTemplate.expire(token, TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);
        return Long.parseLong(userIdStr);
    }

    /**
     * 删除Token（退出登录）
     * @param token 前端传入的Token
     */
    public void deleteToken(String token) {
        if (token != null && !token.isEmpty()) {
            stringRedisTemplate.delete(token);
        }
    }
}
