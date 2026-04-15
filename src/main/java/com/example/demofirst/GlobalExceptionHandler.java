package com.example.demofirst;

import com.example.demofirst.constant.ErrorCodeEnum;
import com.example.demofirst.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 全局异常处理器：统一拦截所有异常，返回标准化响应
 */
@Slf4j // 日志注解
@RestControllerAdvice // 核心注解：全局捕获Controller异常 + 返回JSON
public class GlobalExceptionHandler {

    /**
     * 文件大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<String> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        return Result.error(ErrorCodeEnum.BUSINESS_ERROR,"文件上传失败：文件大小超过限制（最大100MB）");
    }

    /**
     * 文件操作异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return Result.error(ErrorCodeEnum.BUSINESS_ERROR,"文件操作失败：" + e.getMessage());
    }

    /**
     * 通用IO异常
     */
    @ExceptionHandler(IOException.class)
    public Result<String> handleIOException(IOException e) {
        return Result.error(ErrorCodeEnum.BUSINESS_ERROR,"文件操作失败：" + e.getMessage());
    }

    /**
     *  1.处理自定义业务异常（登录失败、参数错误等）
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Object> handleBusinessException(BusinessException e, HttpServletRequest request) {
        // 结构化日志（方便ELK收集）
        log.error("【业务异常】path: {}, code: {}, msg: {}",
                request.getRequestURI(),
                e.getErrorCode().getCode(),
                e.getDetailMsg());

        // 返回结果（开发环境添加调试信息）
        return Result.error(e.getErrorCode(), e.getDetailMsg())
                .setPath(request.getRequestURI())
                .addDebugInfo("errorEnum", e.getErrorCode().name())
                .addDebugInfo("requestMethod", request.getMethod());
    }

    /**
     *  2. 统一处理所有参数校验异常（兼容MethodArgumentNotValidException/BindException）
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Object> handleValidException(Exception e, HttpServletRequest request) {
        // 统一提取BindingResult
        BindingResult bindingResult = null;
        if (e instanceof MethodArgumentNotValidException) {
            bindingResult = ((MethodArgumentNotValidException) e).getBindingResult();
        } else if (e instanceof org.springframework.validation.BindException) {
            bindingResult = ((org.springframework.validation.BindException) e).getBindingResult();
        }

        // 解析字段错误
        Map<String, String> errorMap = new HashMap<>();
        if (bindingResult != null) {
            errorMap = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            FieldError::getDefaultMessage,
                            // 处理同名字段多个错误的情况
                            (msg1, msg2) -> msg1 + "；" + msg2
                    ));
        }

        String errorMsg = ErrorCodeEnum.PARAM_ERROR.getMsg() + "：" + errorMap;
        // 结构化日志
        log.error("【参数校验异常】path: {}, method: {}, errors: {}",
                request.getRequestURI(),
                request.getMethod(),
                errorMap);

        // 返回结果（开发环境展示错误字段）
        return Result.error(ErrorCodeEnum.PARAM_ERROR, errorMsg)
                .setPath(request.getRequestURI())
                .addDebugInfo("errorFields", errorMap)
                .addDebugInfo("requestParams", request.getParameterMap());
    }

    /**
     * 3. 处理系统异常（兜底）
     */
    @ExceptionHandler(Exception.class)
    public Result<Object> handleSystemException(Exception e, HttpServletRequest request) {
        // 打印完整异常栈（开发环境排查）
        log.error("【系统异常】path: {}, method: {}",
                request.getRequestURI(),
                request.getMethod(), e);

        // 生产环境返回友好提示，开发环境返回详细信息
        String msg = "prod".equals(System.getProperty("spring.profiles.active"))
                ? ErrorCodeEnum.SYSTEM_ERROR.getMsg()
                : e.getMessage();

        return Result.error(ErrorCodeEnum.SYSTEM_ERROR, msg)
                .setPath(request.getRequestURI())
                .addDebugInfo("exceptionType", e.getClass().getName())
                .addDebugInfo("stackTrace", getShortStackTrace(e));
    }

    /**
     * 辅助方法：获取简短异常栈（避免返回过长）
     */
    private String getShortStackTrace(Throwable e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(stackTrace.length, 5); i++) { // 只取前5行
            sb.append(stackTrace[i].toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * 辅助方法：获取当前请求路径，兼容非Web请求的路径获取
     */
    private String getRequestUrl() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "未知路径";
        }
        return attributes.getRequest().getRequestURI();
    }
}