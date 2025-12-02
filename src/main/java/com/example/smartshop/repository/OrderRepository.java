package com.example.smartshop.repository;

import com.example.smartshop.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<Order> findByClientId(Long clientId);
}
