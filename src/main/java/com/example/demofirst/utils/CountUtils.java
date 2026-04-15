package com.example.demofirst.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 高并发计数工具类（基于Atomic原子类，线程安全）
 * 用途：接口访问量、用户登录次数、订单创建数等计数场景
 */
public class CountUtils {

    // 存储各接口的访问量（ConcurrentHashMap保证并发安全）
    private static final ConcurrentHashMap<String, AtomicLong> API_COUNT_MAP = new ConcurrentHashMap<>();

    /**
     * 接口访问量+1
     */
    public static void incrApiCount(String apiUri) {
        // 核心：computeIfAbsent - 不存在则创建，存在则直接使用（原子操作）
        API_COUNT_MAP.computeIfAbsent(apiUri, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * 获取接口访问量
     */
    public static long getApiCount(String apiUri) {
        AtomicLong count = API_COUNT_MAP.get(apiUri);
        return count == null ? 0 : count.get();
    }

    /**
     * 重置接口访问量
     */
    public static void resetApiCount(String apiUri) {
        API_COUNT_MAP.put(apiUri, new AtomicLong(0));
    }
}
