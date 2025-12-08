package com.example.smartshop.repository;

import com.example.smartshop.entity.Payment;
import com.example.smartshop.entity.PaymentMethod;
import com.example.smartshop.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

  /**
   * Find all payments for an order
   */
  @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId ORDER BY p.sequenceNumber ASC")
  List<Payment> findByOrderId(@Param("orderId") Long orderId);

  /**
   * Find all payments for an order with pagination
   */
  @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId ORDER BY p.sequenceNumber ASC")
  Page<Payment> findByOrderIdPageable(@Param("orderId") Long orderId, Pageable pageable);

  /**
   * Find all payments with specific status
   */
  Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

  /**
   * Find all payments with specific method
   */
  Page<Payment> findByMethod(PaymentMethod method, Pageable pageable);

  /**
   * Find all encaished payments for an order
   */
  @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId AND p.status = com.example.smartshop.entity.PaymentStatus.ENCAISSE ORDER BY p.sequenceNumber ASC")
  List<Payment> findEncaishedPaymentsByOrderId(@Param("orderId") Long orderId);

  /**
   * Find the highest sequence number for an order
   */
  @Query("SELECT COALESCE(MAX(p.sequenceNumber), 0) FROM Payment p WHERE p.order.id = :orderId")
  Integer findMaxSequenceNumberByOrderId(@Param("orderId") Long orderId);

  /**
   * Check if a payment exceeds the legal limit for cash payments
   * CASH payments cannot exceed 20,000 DH per Article 193 CGI
   */
  @Query("SELECT CASE WHEN SUM(p.amount) > 20000 THEN true ELSE false END " +
      "FROM Payment p WHERE p.order.id = :orderId AND p.method = com.example.smartshop.entity.PaymentMethod.CASH AND p.status = com.example.smartshop.entity.PaymentStatus.ENCAISSE")
  Boolean isCashLimitExceeded(@Param("orderId") Long orderId);
}
