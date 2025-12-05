package com.example.smartshop.service;

import com.example.smartshop.entity.Product;
import com.example.smartshop.dto.ProductRequestDTO;
import com.example.smartshop.dto.ProductResponseDTO;
import com.example.smartshop.dto.ProductFilterDTO;
import com.example.smartshop.repository.ProductRepository;
import com.example.smartshop.exception.BusinessRuleViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponseDTO create(ProductRequestDTO dto) {
        Product product = Product.builder()
                .name(dto.getName())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .deleted(false)
                .build();

        Product savedProduct = productRepository.save(product);
        return toResponseDTO(savedProduct);
    }

    public ProductResponseDTO findById(Long id) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessRuleViolationException("Product not found"));
        return toResponseDTO(product);
    }

    public Page<ProductResponseDTO> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByDeletedFalse(pageable)
                .map(this::toResponseDTO);
    }

    public Page<ProductResponseDTO> findByFilters(ProductFilterDTO filters) {
        int page = filters.getPage() != null ? filters.getPage() : 0;
        int size = filters.getSize() != null ? filters.getSize() : 10;
        Pageable pageable = PageRequest.of(page, size);

        return productRepository.findByFilters(
                filters.getName(),
                filters.getMinPrice(),
                filters.getMaxPrice(),
                filters.getInStock(),
                pageable
        ).map(this::toResponseDTO);
    }

    public ProductResponseDTO update(Long id, ProductRequestDTO dto) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessRuleViolationException("Product not found"));

        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());

        Product updatedProduct = productRepository.save(product);
        return toResponseDTO(updatedProduct);
    }

    public void softDelete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleViolationException("Product not found"));

        product.setDeleted(true);
        productRepository.save(product);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleViolationException("Product not found"));
    }

    public void decrementStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessRuleViolationException("Product not found"));

        if (product.getStock() < quantity) {
            throw new BusinessRuleViolationException("Insufficient stock for product: " + product.getName());
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }

    private ProductResponseDTO toResponseDTO(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .build();
    }
}
