package com.example.smartshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {
  private Long id;
  private Long clientId;
  private LocalDate date;
  private List<OrderItemResponseDTO> items;
  private String status;
  private Double subtotal;
  private Double discountAmount;
  private Double tax;
  private Double total;
  private String promoCode;
  private Double remainingAmount;
}
