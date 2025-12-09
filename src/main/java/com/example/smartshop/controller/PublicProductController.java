package com.example.smartshop.controller;

import com.example.smartshop.dto.ProductResponseDTO;
import com.example.smartshop.dto.ProductFilterDTO;
import com.example.smartshop.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class PublicProductController {

  private final ProductService productService;

  public PublicProductController(ProductService productService) {
    this.productService = productService;
  }

  // ===== PUBLIC ENDPOINTS=====

  @GetMapping
  public ResponseEntity<Map<String, Object>> getAllProducts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Page<ProductResponseDTO> products = productService.findAll(page, size);
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Products retrieved successfully");
    response.put("products", products.getContent());
    response.put("totalPages", products.getTotalPages());
    response.put("totalElements", products.getTotalElements());
    response.put("currentPage", page);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/search")
  public ResponseEntity<Map<String, Object>> searchProducts(@Valid ProductFilterDTO filters) {
    Page<ProductResponseDTO> products = productService.findByFilters(filters);
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Products found");
    response.put("products", products.getContent());
    response.put("totalPages", products.getTotalPages());
    response.put("totalElements", products.getTotalElements());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getProductById(@PathVariable Long id) {
    ProductResponseDTO product = productService.findById(id);
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Product retrieved successfully");
    response.put("product", product);
    return ResponseEntity.ok(response);
  }
}
