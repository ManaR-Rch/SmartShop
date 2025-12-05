package com.example.smartshop.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    HttpSession session = request.getSession(false);

    if (session == null || session.getAttribute("user") == null) {
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.getWriter().write("{\"error\": \"Unauthorized - No valid session\"}");
      return false;
    }

    return true;
  }
}
