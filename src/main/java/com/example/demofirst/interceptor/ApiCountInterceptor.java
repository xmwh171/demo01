package com.example.demofirst.interceptor;

import com.example.demofirst.utils.CountUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


/**
 * 接口访问量计数拦截器（高并发安全）
 */
@Component
public class ApiCountInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取接口URI（如/user/login）
        String apiUri = request.getRequestURI();
        // 高并发计数（AtomicLong保证线程安全）
        CountUtils.incrApiCount(apiUri);
        return true;
    }
}
