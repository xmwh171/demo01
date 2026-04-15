package com.example.demofirst.service;

import com.example.demofirst.utils.ReentrantDistributedLock;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


/**
 * 订单服务（演示分布式锁使用）
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Resource
    private ReentrantDistributedLock distributedLock;

    /**
     * 创建订单（高并发场景，需分布式锁保护）
     */
    public String createOrder(String userId, String goodsId) {
        // 锁名称：order:create:用户ID:商品ID（细粒度锁，避免锁冲突）
        String lockKey = "order:create:" + userId + ":" + goodsId;

        try {
            // 尝试获取锁（等待3秒，锁过期5秒）
            boolean lockSuccess = distributedLock.tryLock(lockKey, 3, 5);
            if (!lockSuccess) {
                return "创建订单失败：请勿重复下单！";
            }

            // 核心业务逻辑（模拟库存扣减、订单创建）
            log.info("【订单创建】用户ID：{}，商品ID：{}，执行线程：{}",
                    userId, goodsId, Thread.currentThread().getName());
            Thread.sleep(500); // 模拟业务耗时

            return "创建订单成功！订单ID：" + System.currentTimeMillis();
        } catch (Exception e) {
            log.error("创建订单失败", e);
            return "创建订单失败：系统异常！";
        } finally {
            // 释放锁（必须在finally中，避免死锁）
            distributedLock.unlock(lockKey);
        }
    }


}
