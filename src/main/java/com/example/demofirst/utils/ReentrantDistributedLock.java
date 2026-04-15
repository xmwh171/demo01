package com.example.demofirst.utils;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 可重入分布式锁（ReentrantLock + Redis）
 * 核心：本地锁减少Redis请求，ReentrantLock支持可重入、超时等待
 */
@Component
public class ReentrantDistributedLock {
    private static final Logger log = LoggerFactory.getLogger(ReentrantDistributedLock.class);

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 本地可重入锁（key: 锁名称，value: ReentrantLock）
    private final ConcurrentHashMap<String, ReentrantLock> localLockMap = new ConcurrentHashMap<>();

    /**
     * 获取分布式锁（支持超时等待）
     * @param lockKey 锁名称
     * @param waitTime 等待时间（秒）
     * @param expireTime 锁过期时间（秒）
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, long waitTime, long expireTime) {
        // 1. 获取本地可重入锁（避免同一线程重复请求Redis）
        ReentrantLock localLock = localLockMap.computeIfAbsent(lockKey, k -> new ReentrantLock());
        try {
            // 2. 尝试获取本地锁（支持超时等待）
            if (!localLock.tryLock(waitTime, TimeUnit.SECONDS)) {
                log.warn("获取本地锁失败：{}", lockKey);
                return false;
            }

            // 3. 尝试获取Redis分布式锁
            Boolean redisLock = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", expireTime, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(redisLock)) {
                return true;
            } else {
                // 释放本地锁
                localLock.unlock();
                log.warn("获取Redis锁失败：{}", lockKey);
                return false;
            }
        } catch (InterruptedException e) {
            log.error("获取锁中断：{}", lockKey, e);
            Thread.currentThread().interrupt(); // 恢复中断状态
            return false;
        }
    }

    /**
     * 释放分布式锁
     */
    public void unlock(String lockKey) {
        ReentrantLock localLock = localLockMap.get(lockKey);
        if (localLock != null && localLock.isHeldByCurrentThread()) {
            // 1. 删除Redis锁
            redisTemplate.delete(lockKey);
            // 2. 释放本地锁
            localLock.unlock();
            log.info("释放锁成功：{}", lockKey);
        }
    }

}
