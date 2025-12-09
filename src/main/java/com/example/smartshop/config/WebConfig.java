package com.example.smartshop.config;

import com.example.smartshop.interceptor.AdminInterceptor;
import com.example.smartshop.interceptor.LoginInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class WebConfig implements WebMvcConfigurer {
  private LoginInterceptor loginInterceptor;
  private AdminInterceptor adminInterceptor;

  public WebConfig(LoginInterceptor loginInterceptor, AdminInterceptor adminInterceptor) {
    this.loginInterceptor = loginInterceptor;
    this.adminInterceptor = adminInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(loginInterceptor)
        .addPathPatterns("/api/client/**", "/api/admin/orders/**", "/api/profile/**");

    registry.addInterceptor(adminInterceptor)
        .addPathPatterns("/api/admin/**");
  }
}
