package com.example.smartshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterDTO {
  private String name;
  private Double minPrice;
  private Double maxPrice;
  private Boolean inStock;
  private Integer page;
  private Integer size;
}
