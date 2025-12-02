package com.example.smartshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {
    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemRequestDTO> items;

    private String promoCode;
}
