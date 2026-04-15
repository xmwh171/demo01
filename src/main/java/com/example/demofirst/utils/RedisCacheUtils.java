package com.example.demofirst.utils;

import com.example.demofirst.entity.User;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 优化后的Redis缓存工具类（Redis+本地ConcurrentHashMap双层缓存）
 */
@Component
public class RedisCacheUtils {

    // 本地并发缓存（解决高频访问Redis的性能问题）
    private final Map<String, CacheValue> localCache = CollectionUtils.newConcurrentMap();

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存前缀
    private static final String USER_CACHE_PREFIX = "user:info:";
    // 本地缓存过期时间（5分钟，单位：毫秒）
    private static final long LOCAL_CACHE_EXPIRE = 5 * 60 * 1000L;

    /**
     * 缓存值包装类（存储值+时间戳）
     */
    private static class CacheValue {
        private Object value;
        private long timestamp;

        public CacheValue(Object value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }

        // getter/setter
        public Object getValue() {
            return value;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    /**
     * 获取用户缓存（先查本地，再查Redis）
     */
    public User getUserCache(Long userId) {
        String cacheKey = USER_CACHE_PREFIX + userId;

        // 1. 查本地ConcurrentHashMap（并发安全）
        CacheValue localValue = localCache.get(cacheKey);
        if (localValue != null) {
            // 检查本地缓存是否过期
            if (System.currentTimeMillis() - localValue.getTimestamp() < LOCAL_CACHE_EXPIRE) {
                return (User) localValue.getValue();
            } else {
                // 过期则移除
                localCache.remove(cacheKey);
            }
        }

        // 2. 查Redis
        User redisValue = (User) redisTemplate.opsForValue().get(cacheKey);
        if (redisValue != null) {
            // 同步到本地缓存
            localCache.put(cacheKey, new CacheValue(redisValue, System.currentTimeMillis()));
            return redisValue;
        }

        return null;
    }

    /**
     * 删除用户缓存（同时删除Redis和本地）
     */
    public void deleteUserCache(Long userId) {
        String cacheKey = USER_CACHE_PREFIX + userId;
        redisTemplate.delete(cacheKey);
        localCache.remove(cacheKey);
    }

    //   ---------------------- 分布式锁 ---------------------
    /**
     * 加锁（原子操作：set + nx + ex）
     * @param lockKey 锁key
     * @param requestId 唯一标识，防止误释放
     * @param expireSeconds 过期秒
     */
    public boolean lock(String lockKey, String requestId, long expireSeconds) {
        // SET key value NX EX 过期时间（原子操作）
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, requestId, expireSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    /**
     * 解锁（Lua 原子判断 + 删除）
     */
    public boolean unlock(String lockKey, String requestId) {
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";

        RedisScript<Long> redisScript = RedisScript.of(script, Long.class);
        Long result = redisTemplate.execute(redisScript, Arrays.asList(lockKey), requestId);

        return Long.valueOf(1).equals(result);
    }

}
