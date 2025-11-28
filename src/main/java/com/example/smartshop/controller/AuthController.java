package com.example.smartshop.controller;

import com.example.smartshop.dto.LogInDTO;
import com.example.smartshop.dto.CreateUserDTO;
import com.example.smartshop.entity.User;
import com.example.smartshop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> logIn(@Valid @RequestBody LogInDTO dto, HttpServletRequest request) {
        User user = userService.logIn(dto);
        HttpSession session = request.getSession(true);

        session.setAttribute("user", user.getId());
        return ResponseEntity.ok("Login successful");
    }

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserDTO dto, HttpServletRequest request) {
        User user = userService.create(dto);

        HttpSession session = request.getSession(true);
        session.setAttribute("user", user.getId());

        return ResponseEntity.ok("Account created successfully");
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok("Logged out successfully");
    }
}
