package com.example.smartshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryDTO {
    private Long id;
    private LocalDateTime date;
    private Double totalAmount;
    private String status;
}
