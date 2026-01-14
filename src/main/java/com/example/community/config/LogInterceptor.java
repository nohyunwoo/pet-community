package com.example.community.config; // 프로젝트 패키지에 맞게 수정하세요

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LogInterceptor implements HandlerInterceptor {

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        int status = response.getStatus();

        if (status >= 400) {
            log.error(">>> [API ERROR] {} {} | Status: {}", method, requestURI, status);
        } else {
            log.info(">>> [API SUCCESS] {} {} | Status: {}", method, requestURI, status);
        }
    }
}