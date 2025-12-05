package com.example.smartshop.interceptor;

import com.example.smartshop.entity.UserRole;
import com.example.smartshop.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {
  private UserRepository userRepository;

  public AdminInterceptor(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    HttpSession session = request.getSession(false);

    if (session == null || session.getAttribute("user") == null) {
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.getWriter().write("{\"error\": \"Unauthorized - No valid session\"}");
      return false;
    }

    Long userId = (Long) session.getAttribute("user");
    var user = userRepository.findById(userId).orElse(null);

    if (user == null || user.getRole() != UserRole.ADMIN) {
      response.setStatus(HttpStatus.FORBIDDEN.value());
      response.getWriter().write("{\"error\": \"Forbidden - Admin access required\"}");
      return false;
    }

    return true;
  }
}
