package com.example.demofirst;

import com.example.demofirst.constant.ErrorCodeEnum;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.core.env.Environment;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局统一返回体
 * 所有接口都返回这个对象，格式统一
 */
@Data  // Lombok注解，自动生成get/set/toString
public class Result<T> {
    /**
     * 状态码：200成功，400参数错误，500服务器错误
     */
    private int code;

    /**
     * 提示信息
     */
    private String msg;

    /**
     * 返回的数据（泛型，支持任意类型）
     */
    private T data;

    // 扩展字段（新增）
    private String path;        // 请求路径
    private LocalDateTime timestamp; // 响应时间戳

    // 开发环境才返回的详细信息
    private Map<String, Object> debugInfo;

    // 注入Environment，用于动态获取环境配置
    @Resource
    private static Environment environment;

    public static void setEnvironment(Environment env) {
        Result.environment = env;
    }

    // 静态方法判断是否为开发环境
    private static boolean isDev() {
        if (environment == null) {
            return true; // 兜底，默认视为开发环境
        }
        return "dev".equals(environment.getProperty("spring.profiles.active", "dev"));
    }

    // ========== 成功响应 ==========
    public static <T> Result<T> success() {
        return buildResult(ErrorCodeEnum.SUCCESS, null, null);
    }

    public static <T> Result<T> success(T data) {
        return buildResult(ErrorCodeEnum.SUCCESS, data, null);
    }

    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = buildResult(ErrorCodeEnum.SUCCESS, data, null);
        result.setMsg(msg);
        return result;
    }

    // ========== 错误响应 ==========
    public static <T> Result<T> error(ErrorCodeEnum errorCode) {
        return buildResult(errorCode, null, null);
    }

    public static <T> Result<T> error(ErrorCodeEnum errorCode, String detailMsg) {
        Result<T> result = buildResult(errorCode, null, null);
        result.setMsg(detailMsg);
        return result;
    }

    // ========== 核心构建方法 ==========
    private static <T> Result<T> buildResult(ErrorCodeEnum errorCode, T data, Map<String, Object> debugInfo) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMsg(errorCode.getMsg());
        result.setData(data);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }


    // ========== 新增链式方法（方便设置扩展字段） ==========
    public Result<T> setPath(String path) {
        this.path = path;
        return this; // 链式调用
    }

    public Result<T> setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    // 开发环境添加调试信息（如异常栈、请求参数）
    public Result<T> addDebugInfo(String key, Object value) {
        // 只有开发环境才存储调试信息
        if (isDev()) {
            if (this.debugInfo == null) {
                this.debugInfo = new HashMap<>();
            }
            this.debugInfo.put(key, value);
        }
        // 生产环境直接返回，不做任何操作
        return this;
    }
}
