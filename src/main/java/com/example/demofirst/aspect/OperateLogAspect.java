package com.example.demofirst.aspect;

import com.alibaba.fastjson2.JSON;
import com.example.demofirst.annotation.RecordLog;
import com.example.demofirst.entity.OperateLog;
import com.example.demofirst.service.OperateLogService;
import com.example.demofirst.utils.SensitiveDataUtils;
import com.example.demofirst.utils.TokenUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志切面：自动记录接口操作日志
 */
@Aspect // 标记为切面类
@Component // 交给Spring管理
public class OperateLogAspect {

    @Resource
    private TokenUtils tokenUtils;

    @Resource
    private OperateLogService operateLogService;
    // 如果要存入数据库，注入OperateLogService（后续可补充）

//    /**
//     * 定义切点：拦截所有/user开头的接口
//     */
//   @Pointcut("execution(* com.example.demofirst.controller.UserController.*(..))")
    /**
     * 定义切点：只拦截带有@RecordLog注解的方法
     */
    @Pointcut("@annotation(com.example.demofirst.annotation.RecordLog)")
    public void operateLogPointcut() {}

    /**
     * 环绕通知：记录日志（兼容异常、过滤敏感信息、存入数据库）
     */
    @Around("operateLogPointcut()")
    public Object recordOperateLog(ProceedingJoinPoint joinPoint) throws Throwable {
        // 初始化日志对象
        OperateLog log = new OperateLog();
        Object result = null;
        long costTime = 0;
        String resultStr = "";

        // 把 logDesc 提升到 try 外面，让 finally 能访问
        String logDesc = "";

        try {
            // 1. 获取请求上下文（兼容非Web请求）
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = null;
            if (attributes != null) {
                request = attributes.getRequest();
            }

            // 2. 获取注解描述
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            RecordLog recordLog = method.getAnnotation(RecordLog.class);
            logDesc = recordLog.value();

            // 3. 收集基础信息
            Long userId = null;
            String url = "";
            String methodType = "";
            if (request != null) {
                // 从Token获取操作人ID
                String token = request.getHeader("token");
                if (token != null) {
                    userId = tokenUtils.validateToken(token);
                }
                url = request.getRequestURI(); // 接口地址
                methodType = request.getMethod(); // 请求方式（GET/POST）
            }

            // 4. 收集请求参数（过滤敏感字段）
            Object[] args = joinPoint.getArgs();
            String params = SensitiveDataUtils.filterSensitiveData(args);

            // 5. 记录开始时间
            long startTime = System.currentTimeMillis();

            // 封装b部分日志信息
            log.setUserId(userId);
            log.setUrl(url);
            log.setMethod(methodType);
            log.setParams(params);
            // 6. 执行原接口方法
            result = joinPoint.proceed();

            // 7. 计算耗时、收集返回结果
            resultStr = SensitiveDataUtils.filterSensitiveData(result);
            costTime = System.currentTimeMillis() - startTime;

            // 8. 封装日志
            log.setResult(resultStr);
            log.setCostTime(costTime);

        } catch (Exception e) {
            // 接口抛出异常时，记录异常信息
            resultStr = "接口执行异常：" + e.getMessage();
            log.setResult(resultStr);
            // 异常不阻断原接口逻辑，继续抛出
            throw e;
        } finally {
            // 9. 日志存入数据库（finally确保无论是否异常都记录）
            try {
                log.setCreateTime(LocalDateTime.now());
                operateLogService.save(log);
                // 控制台打印格式化日志
                System.out.println("===== 操作日志（优化版） =====");
                System.out.println("描述：" + log.getUrl() + " - " + (logDesc.isEmpty() ? "无" : logDesc));
                System.out.println(JSON.toJSONString(log));
                System.out.println("=============================");
            } catch (Exception e) {
                // 日志入库失败不影响主业务
                System.err.println("日志入库失败：" + e.getMessage());
            }
        }

        // 10. 返回原接口结果
        return result;

    }
}
