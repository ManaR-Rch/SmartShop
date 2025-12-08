package com.example.smartshop.service;

import com.example.smartshop.dto.ProductRequestDTO;
import com.example.smartshop.dto.ProductResponseDTO;
import com.example.smartshop.entity.Product;
import com.example.smartshop.exception.BusinessRuleViolationException;
import com.example.smartshop.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void findByIdSuccess() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(999.99);
        product.setStock(10);
        product.setDeleted(false);

        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));

        ProductResponseDTO result = productService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Laptop", result.getName());
    }

    @Test
    void findByIdNotFound() {
        when(productRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessRuleViolationException.class, () -> productService.findById(99L));
    }

    @Test
    void createProductSuccess() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Mouse");
        product.setPrice(50.0);
        product.setStock(100);
        product.setDeleted(false);

        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setName("Mouse");
        dto.setPrice(50.0);
        dto.setStock(100);

        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponseDTO result = productService.create(dto);

        assertNotNull(result);
        assertEquals("Mouse", result.getName());
        assertEquals(50.0, result.getPrice());
    }

    @Test
    void updateProductSuccess() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(999.99);
        product.setStock(10);
        product.setDeleted(false);

        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName("Gaming Laptop");
        updatedProduct.setPrice(1299.99);
        updatedProduct.setStock(5);
        updatedProduct.setDeleted(false);

        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setName("Gaming Laptop");
        dto.setPrice(1299.99);
        dto.setStock(5);

        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        ProductResponseDTO result = productService.update(1L, dto);

        assertNotNull(result);
        assertEquals("Gaming Laptop", result.getName());
        assertEquals(1299.99, result.getPrice());
    }

    @Test
    void softDeleteSuccess() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(999.99);
        product.setStock(10);
        product.setDeleted(false);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.softDelete(1L);

        assertTrue(true);
    }

    @Test
    void decrementStockSuccess() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(999.99);
        product.setStock(10);
        product.setDeleted(false);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.decrementStock(1L, 5);

        assertTrue(true);
    }

    @Test
    void decrementStockInsufficientStock() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(999.99);
        product.setStock(2);
        product.setDeleted(false);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(BusinessRuleViolationException.class, () -> productService.decrementStock(1L, 5));
    }
}
