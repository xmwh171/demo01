package com.example.demofirst.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 企业级异步线程池配置
 * 核心：合理设置参数、自定义拒绝策略、线程命名、生命周期管理
 */
@Slf4j
@Configuration
@EnableAsync // 开启异步支持
public class ThreadPoolConfig {

    // 核心线程数 = CPU核心数 * 2 + 磁盘数（生产级经验值）
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2 + 1;
    // 最大线程数 = 核心线程数 * 2
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;
    // 队列容量（有界队列，避免OOM）
    private static final int QUEUE_CAPACITY = 1000;
    // 线程空闲时间（60秒）
    private static final int KEEP_ALIVE_SECONDS = 60;
    // 线程名称前缀（便于日志排查）
    private static final String THREAD_NAME_PREFIX = "demo-async-";

    /**
     * 自定义异步线程池
     */
    @Bean(name = "asyncTaskExecutor")
    public Executor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 1. 核心参数配置
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);

        // 2. 拒绝策略（生产级：调用者执行+日志记录）
        executor.setRejectedExecutionHandler((r, executor1) -> {
            log.error("异步线程池已满！核心线程数：{}，最大线程数：{}，队列容量：{}，当前任务数：{}",
                    CORE_POOL_SIZE, MAX_POOL_SIZE, QUEUE_CAPACITY, executor1.getTaskCount());
            // 调用者线程执行任务（避免任务丢失）
            try {
                r.run();
            } catch (Exception e) {
                log.error("调用者执行异步任务失败", e);
            }
        });

        // 3. 生命周期管理（项目停止时等待任务完成）
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60); // 等待60秒后强制关闭

        // 4. 初始化
        executor.initialize();

        log.info("异步线程池初始化完成！核心线程数：{}，最大线程数：{}", CORE_POOL_SIZE, MAX_POOL_SIZE);
        return executor;

    }

}
