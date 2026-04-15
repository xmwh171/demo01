package com.example.demofirst.interceptor;

import com.example.demofirst.Result;
import com.example.demofirst.constant.ErrorCodeEnum;
import com.example.demofirst.utils.TokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

/**
 * Token拦截器：校验登录态
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Resource
    private TokenUtils tokenUtils;

    /**
     * 请求处理前校验Token
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从请求头获取Token（前端需把Token放在header的token字段）
        String token = request.getHeader("token");

        // 2. 校验Token
        Long userId = tokenUtils.validateToken(token);
        if (userId == null) {
            // 3. Token无效：返回统一格式的错误信息
            response.setContentType("application/json;charset=utf-8");
            PrintWriter writer = response.getWriter();
            Result<String> errorResult = Result.error(ErrorCodeEnum.UNAUTHORIZED, "未登录或Token已过期，请重新登录");

            // ========== 以下是100%修复序列化问题的核心代码 ==========
            // 步骤1：新建ObjectMapper实例
            ObjectMapper objectMapper = new ObjectMapper();
            // 步骤2：强制注册Java 8时间模块（必须有这行！）
            objectMapper.registerModule(new JavaTimeModule());
            // 步骤3：关闭时间戳序列化，返回字符串格式
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            // 步骤4：序列化（此时不会再报LocalDateTime异常）
            String jsonStr = objectMapper.writeValueAsString(errorResult);

            // 写入响应并关闭流（避免重复写入）
            writer.write(jsonStr);
            writer.flush(); // 强制刷新
            writer.close(); // 关闭流，杜绝重复写响应
            // ========== 核心修复结束 ==========

            return false;  // 拦截请求，禁止访问
        }

        // 4. Token有效：将用户ID存入请求，后续接口可获取
        request.setAttribute("userId", userId);
        return true;  // 放行请求
    }

}