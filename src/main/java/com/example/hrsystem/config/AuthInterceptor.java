package com.example.hrsystem.config;

import com.example.hrsystem.controller.LoginController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getSession(false) != null
                && request.getSession(false).getAttribute(LoginController.SESSION_USER) != null) {
            return true;
        }

        response.sendRedirect(request.getContextPath() + "/login");
        return false;
    }
}
