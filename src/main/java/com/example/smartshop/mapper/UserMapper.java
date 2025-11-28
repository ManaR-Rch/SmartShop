package com.example.smartshop.mapper;

import com.example.smartshop.dto.CreateUserDTO;
import com.example.smartshop.entity.User;
import com.example.smartshop.entity.UserRole;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    
    public User toEntity(CreateUserDTO dto) {
        return User.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .role(UserRole.CLIENT)
                .build();
    }
}
