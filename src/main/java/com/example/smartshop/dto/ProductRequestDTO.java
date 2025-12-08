package com.example.smartshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {
  @NotNull(message = "Product name is required")
  @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
  private String name;

  @NotNull(message = "Price is required")
  @Positive(message = "Price must be positive")
  private Double price;

  @NotNull(message = "Stock is required")
  @Positive(message = "Stock must be positive")
  private Integer stock;
}
