package com.example.smartshop.controller;

import com.example.smartshop.dto.ProductRequestDTO;
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
public class ProductController {

  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

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

  @PostMapping
  public ResponseEntity<Map<String, Object>> createProduct(@Valid @RequestBody ProductRequestDTO dto) {
    ProductResponseDTO product = productService.create(dto);
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Product created successfully");
    response.put("product", product);
    return ResponseEntity.status(201).body(response);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Map<String, Object>> updateProduct(
      @PathVariable Long id,
      @Valid @RequestBody ProductRequestDTO dto) {
    ProductResponseDTO product = productService.update(id, dto);
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Product updated successfully");
    response.put("product", product);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
    productService.softDelete(id);
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Product deleted successfully (soft delete)");
    return ResponseEntity.ok(response);
  }
}
