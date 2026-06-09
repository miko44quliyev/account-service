package com.example.accountservice.common.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    private static final String REAL_API_KEY = "MGX_204406";

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String requestApiKey = request.getHeader("X-API-KEY");

        if (requestApiKey == null || !requestApiKey.equals(REAL_API_KEY)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Error: API-Key is incorrect or has not been sent!");
            return false;
        }

        return true;
    }
}