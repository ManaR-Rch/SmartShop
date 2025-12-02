package com.example.smartshop.dto;

import com.example.smartshop.entity.CustomerTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponseDTO {
    private Long id;
    private String name;
    private String email;
    private CustomerTier tier;
    private Integer totalOrders;
    private Double totalSpent;
}
