package com.example.demofirst.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 异步任务服务（优化后：指定线程池、异常处理）
 */
@Slf4j
@Service
public class AsyncService {

    /**
     * 异步保存操作日志
     * @Async("asyncTaskExecutor")：指定使用自定义线程池
     */
    @Async("asyncTaskExecutor")
    public void saveOperationLog(String userId, String operateUri, long consumeTime) {
        try {
            // 模拟日志入库耗时操作（替换为实际DAO操作）
            Thread.sleep(100);
            log.info("【异步任务-日志入库】用户ID：{}，接口：{}，耗时：{}ms，执行线程：{}",
                    userId, operateUri, consumeTime, Thread.currentThread().getName());
        } catch (Exception e) {
            log.error("异步保存日志失败", e);
            // 生产环境可添加重试/告警逻辑
        }
    }

    /**
     * 异步发送短信（模拟）
     */
    @Async("asyncTaskExecutor")
    public void sendSms(String phone, String content) {
        try {
            // 模拟短信发送耗时
            Thread.sleep(200);
            log.info("【异步任务-短信发送】手机号：{}，内容：{}，执行线程：{}",
                    phone, content, Thread.currentThread().getName());
        } catch (Exception e) {
            log.error("异步发送短信失败", e);
        }
    }
}
