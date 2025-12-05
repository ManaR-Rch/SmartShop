package com.example.smartshop.repository;

import com.example.smartshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByDeletedFalse(Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndDeletedFalse(String name, Pageable pageable);

    Page<Product> findByPriceBetweenAndDeletedFalse(Double minPrice, Double maxPrice, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.stock > 0")
    Page<Product> findActiveInStockProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.deleted = false " +
            "AND (:name IS NULL OR p.name LIKE CONCAT('%', :name, '%')) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
            "AND (:inStock IS NULL OR (:inStock = false OR p.stock > 0))")
    Page<Product> findByFilters(
            @Param("name") String name,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("inStock") Boolean inStock,
            Pageable pageable);

    Optional<Product> findByIdAndDeletedFalse(Long id);
}
