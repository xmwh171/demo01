package com.example.demofirst.aspect;

import com.example.demofirst.annotation.NoRepeatSubmit;
import com.example.demofirst.annotation.RateLimit;
import com.example.demofirst.constant.ErrorCodeEnum;
import com.example.demofirst.exception.BusinessException;
import com.example.demofirst.utils.CollectionUtils;
import com.example.demofirst.utils.TokenUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 接口安全防护切面：防重复提交 + 接口限流
 */
@Aspect
@Component
public class SecurityAspect {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    // 本地锁（ConcurrentHashMap保证并发安全）
    private final Map<String, Long> localLockMap = CollectionUtils.newConcurrentMap();

    @Resource
    private TokenUtils tokenUtils;

    // 定时清理线程池（单线程，避免多线程竞争）
    private final ScheduledExecutorService cleanExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "local-lock-clean-thread");
        t.setDaemon(true); // 设置为守护线程，JVM退出时自动销毁
        return t;
    });


    /**
     * 初始化方法：启动定时清理任务
     * @PostConstruct：Bean初始化完成后执行
     */
    @PostConstruct
    public void initCleanTask() {
        // 每1分钟执行一次清理（可根据业务调整频率）
        cleanExecutor.scheduleAtFixedRate(this::cleanExpiredLocalLock, 1, 1, TimeUnit.MINUTES);
        System.out.println("本地锁过期数据清理任务已启动，清理频率：1分钟/次");
    }



    // ===================== 防重复提交 =====================
    @Pointcut("@annotation(com.example.demofirst.annotation.NoRepeatSubmit)")
    public void noRepeatSubmitPointcut(){}

    @Around("noRepeatSubmitPointcut()")
    public Object noRepeatSubmit(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        NoRepeatSubmit annotation = method.getAnnotation(NoRepeatSubmit.class);
        long expire = annotation.expire();

        // 2. 构建唯一锁标识（用户ID + 接口路径）
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Long userId = getUserId(request);
        String requestURI = request.getRequestURI();
        String lockKey = "no_repeat:" + userId + ":" + requestURI;

        // 2. 先查本地锁（减少Redis请求）
        Long localLockTime = localLockMap.get(lockKey);
        if (localLockTime != null && System.currentTimeMillis() - localLockTime < expire * 1000) {
            throw new BusinessException(ErrorCodeEnum.BUSINESS_ERROR, "请勿重复提交，请稍后再试！");
        }

        // 3. 再查Redis 分布式锁（setIfAbsent 原子操作）
        Boolean isLock = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", expire, TimeUnit.SECONDS);
        if (isLock == null || !isLock) {
            // 加锁失败 = 重复提交
            throw new BusinessException(ErrorCodeEnum.BUSINESS_ERROR, "请勿重复提交，请稍后再试！");
        }

        // 4. 更新本地锁
        localLockMap.put(lockKey, System.currentTimeMillis());

        // 5. 执行原方法
        try {
            return joinPoint.proceed();
        } finally {
            // 无需手动删除锁，依靠Redis过期自动释放（避免业务异常导致死锁）
        }

    }

    // ===================== 接口限流 =====================
    @Pointcut("@annotation(com.example.demofirst.annotation.RateLimit)")
    public void rateLimitPointcut() {}

    @Around("rateLimitPointcut()")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit annotation = method.getAnnotation(RateLimit.class);
        long window = annotation.window();
        long limit = annotation.limit();
        String prefix = annotation.prefix();

        // 2. 构建限流标识（用户ID + 接口名），匿名用户用IP
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String identifier = getIdentifier(request);
        String limitKey = prefix + identifier + ":" + method.getName();

        // 3. Redis 自增计数（原子操作）
        Long count = redisTemplate.opsForValue().increment(limitKey, 1);
        if (count == 1) {
            // 第一次请求，设置过期时间（时间窗口）
            redisTemplate.expire(limitKey, window, TimeUnit.SECONDS);
        }

        // 4. 判断是否超出限制
        if (count > limit) {
            throw new BusinessException(ErrorCodeEnum.RATE_LIMIT_ERROR, "接口请求过于频繁，请稍后再试！");
        }

        // 5. 执行原方法
        return joinPoint.proceed();

    }

    // ===================== 辅助方法 =====================
    /**
     * 获取用户唯一标识：已登录用用户ID，未登录用IP
     */
    private String getIdentifier(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId != null) {
            return "user:" + userId;
        }
        // 匿名用户用IP
        return "ip:" + request.getRemoteAddr();
    }

    /**
     * 从Token获取用户ID
     */
    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            return null; // 未登录
        }
        try {
            return tokenUtils.validateToken(token);
        } catch (Exception e) {
            return null; // Token无效
        }
    }

    /**
     * 清理过期的本地锁数据
     * 核心：遍历ConcurrentHashMap，删除过期条目（ConcurrentHashMap支持并发遍历+删除）
     */
    private void cleanExpiredLocalLock() {
        if (localLockMap.isEmpty()) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        int cleanCount = 0;
        // 遍历并删除过期数据（ConcurrentHashMap的iterator是fail-safe的，支持边遍历边删除）
        Iterator<Map.Entry<String, Long>> iterator = localLockMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            String key = entry.getKey();
            Long lockTime = entry.getValue();

            // 判断是否过期（这里取防重复提交注解的默认过期时间5秒，可根据实际调整）
            if (currentTime - lockTime > 5 * 1000) {
                iterator.remove(); // 安全删除（避免ConcurrentModificationException）
                cleanCount++;
            }
        }

        // 打印日志（便于监控）
        if (cleanCount > 0) {
            System.out.println("清理过期本地锁数据，数量：" + cleanCount + "，剩余数据量：" + localLockMap.size());
        }
    }

    /**
     * 销毁方法：关闭定时线程池（避免内存泄漏）
     * @PreDestroy：Bean销毁前执行
     */
    @PreDestroy
    public void destroy() {
        cleanExecutor.shutdown();
        try {
            // 等待线程池终止（最多等5秒）
            if (!cleanExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanExecutor.shutdownNow();
        }
        System.out.println("本地锁清理线程池已关闭");
    }

}
