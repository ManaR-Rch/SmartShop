package com.example.smartshop.service;

import com.example.smartshop.config.PasswordUtil;
import com.example.smartshop.dto.CreateUserDTO;
import com.example.smartshop.dto.LogInDTO;
import com.example.smartshop.entity.User;
import com.example.smartshop.entity.UserRole;
import com.example.smartshop.exception.BusinessRuleViolationException;
import com.example.smartshop.mapper.UserMapper;
import com.example.smartshop.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private UserRepository userRepository;
    private UserMapper userMapper;
    private PasswordUtil passwordUtil;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordUtil passwordUtil) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordUtil = passwordUtil;
    }

    public User create(CreateUserDTO dto) {
        var existingUser = userRepository.findByUsername(dto.getUsername()).orElse(null);
        if (existingUser != null) {
            throw new BusinessRuleViolationException("User already exists");
        }

        User user = userMapper.toEntity(dto);
        user.setPassword(passwordUtil.passwordHash(dto.getPassword()));
        user.setRole(UserRole.CLIENT); // Default role

        userRepository.save(user);
        return user;
    }

    public User logIn(LogInDTO dto) {
        User user = userRepository.findByUsername(dto.getUsername()).orElse(null);
        if (user == null) {
            throw new BusinessRuleViolationException("Username or password is incorrect");
        }

        if (!passwordUtil.checkPassword(dto.getPassword(), user.getPassword())) {
            throw new BusinessRuleViolationException("Username or password is incorrect");
        }

        return user;
    }
}