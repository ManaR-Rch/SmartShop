package com.example.smartshop.service;

import com.example.smartshop.dto.CreateUserDTO;
import com.example.smartshop.dto.LogInDTO;
import com.example.smartshop.entity.User;
import com.example.smartshop.entity.UserRole;
import com.example.smartshop.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User logIn(LogInDTO dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(dto.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return user;
    }

    public User create(CreateUserDTO dto) {
        User user = User.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .role(UserRole.CLIENT)
                .build();

        return userRepository.save(user);
    }
}
