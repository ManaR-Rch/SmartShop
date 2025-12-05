package com.example.smartshop.config;

import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class PasswordUtil {

  public String passwordHash(String rawPassword) {
    return Base64.getEncoder().encodeToString(rawPassword.getBytes(StandardCharsets.UTF_8));
  }

  public boolean checkPassword(String rawPassword, String hashedPassword) {
    String encoded = Base64.getEncoder().encodeToString(rawPassword.getBytes(StandardCharsets.UTF_8));
    return encoded.equals(hashedPassword);
  }
}
