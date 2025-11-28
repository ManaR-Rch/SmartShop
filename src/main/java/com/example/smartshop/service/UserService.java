package com.example.smartshop.service;

import com.example.smartshop.dto.CreateUserDTO;
import com.example.smartshop.dto.LogInDTO;
import com.example.smartshop.entity.User;
import com.example.smartshop.entity.UserRole;
import com.example.smartshop.mapper.UserMapper;
import com.example.smartshop.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private UserRepository userRepository;
  private UserMapper userMapper;

  public UserService(UserRepository userRepository, UserMapper userMapper) {
    this.userRepository = userRepository;
    this.userMapper = userMapper;
  }

  public User create(CreateUserDTO dto) {
    User user = userRepository.findByUsername(dto.getUsername()).orElse(null);
    if (user != null) {
      throw new RuntimeException("User already exists");
    }
    user = userMapper.toEntity(dto);
    user.setRole(UserRole.CLIENT);
    userRepository.save(user);
    return user;
  }

  public User logIn(LogInDTO dto) {
    User user = userRepository.findByUsername(dto.getUsername()).orElse(null);
    if (user == null) {
      throw new RuntimeException("Username or password is incorrect");
    }
    if (!user.getPassword().equals(dto.getPassword())) {
      throw new RuntimeException("Username or password is incorrect");
    }
    return user;
  }
}
