package com.example.smartshop.controller;

import com.example.smartshop.dto.PaymentRequestDTO;
import com.example.smartshop.dto.PaymentResponseDTO;
import com.example.smartshop.entity.PaymentMethod;
import com.example.smartshop.entity.PaymentStatus;
import com.example.smartshop.service.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/payments")
public class PaymentController {

  private final PaymentService paymentService;

  public PaymentController(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  /**
   * Add a new payment to an order
   * 
   * Supports multi-method payments:
   * - CASH: immediate, max 20,000 DH per payment (Article 193 CGI)
   * - CHEQUE: can be deferred with due date
   * - TRANSFER: immediate or deferred
   * 
   * Fractional payments: orders can be paid in multiple installments
   * 
   * @param dto Payment request DTO
   * @return Created payment with sequence number and remaining amount
   */
  @PostMapping
  public ResponseEntity<Map<String, Object>> addPayment(@Valid @RequestBody PaymentRequestDTO dto) {
    PaymentResponseDTO payment = paymentService.addPayment(dto);
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Payment recorded successfully");
    response.put("payment", payment);
    response.put("sequence_number", payment.getSequenceNumber());
    response.put("remaining_amount", payment.getRemainingAmount());
    return ResponseEntity.status(201).body(response);
  }

  /**
   * Get payment details by payment ID
   * 
   * @param id Payment ID
   * @return Payment details
   */
  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getPayment(@PathVariable Long id) {
    PaymentResponseDTO payment = paymentService.getPaymentById(id);
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Payment retrieved successfully");
    response.put("payment", payment);
    return ResponseEntity.ok(response);
  }

  /**
   * Get all payments for an order
   * 
   * @param orderId Order ID
   * @return List of payments for the order
   */
  @GetMapping("/order/{orderId}")
  public ResponseEntity<Map<String, Object>> getOrderPayments(@PathVariable Long orderId) {
    List<PaymentResponseDTO> payments = paymentService.getPaymentsByOrderId(orderId);
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Payments retrieved successfully");
    response.put("payments", payments);
    response.put("total", payments.size());
    return ResponseEntity.ok(response);
  }

  /**
   * Get paginated payments for an order
   * 
   * @param orderId  Order ID
   * @param pageable Pagination info
   * @return Paginated payments
   */
  @GetMapping("/order/{orderId}/paginated")
  public ResponseEntity<Map<String, Object>> getOrderPaymentsPaginated(
      @PathVariable Long orderId,
      Pageable pageable) {
    Page<PaymentResponseDTO> payments = paymentService.getPaymentsByOrderIdPaginated(orderId, pageable);
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Payments retrieved successfully");
    response.put("payments", payments.getContent());
    response.put("total_pages", payments.getTotalPages());
    response.put("total_elements", payments.getTotalElements());
    return ResponseEntity.ok(response);
  }

  /**
   * Get payments by status
   * 
   * @param status   Payment status (EN_ATTENTE, ENCAISSÉ, REJETÉ)
   * @param pageable Pagination info
   * @return Paginated payments
   */
  @GetMapping("/status/{status}")
  public ResponseEntity<Map<String, Object>> getPaymentsByStatus(
      @PathVariable PaymentStatus status,
      Pageable pageable) {
    Page<PaymentResponseDTO> payments = paymentService.getPaymentsByStatus(status, pageable);
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Payments retrieved successfully");
    response.put("payments", payments.getContent());
    response.put("total_pages", payments.getTotalPages());
    response.put("total_elements", payments.getTotalElements());
    return ResponseEntity.ok(response);
  }

  /**
   * Get payments by method
   * 
   * @param method   Payment method (CASH, CHEQUE, TRANSFER)
   * @param pageable Pagination info
   * @return Paginated payments
   */
  @GetMapping("/method/{method}")
  public ResponseEntity<Map<String, Object>> getPaymentsByMethod(
      @PathVariable PaymentMethod method,
      Pageable pageable) {
    Page<PaymentResponseDTO> payments = paymentService.getPaymentsByMethod(method, pageable);
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Payments retrieved successfully");
    response.put("payments", payments.getContent());
    response.put("total_pages", payments.getTotalPages());
    response.put("total_elements", payments.getTotalElements());
    return ResponseEntity.ok(response);
  }

  /**
   * Update payment status
   * 
   * Valid transitions:
   * - EN_ATTENTE → ENCAISSÉ (payment collected)
   * - EN_ATTENTE → REJETÉ (payment rejected)
   * 
   * @param paymentId Payment ID
   * @param status    New status
   * @return Updated payment
   */
  @PutMapping("/{paymentId}/status")
  public ResponseEntity<Map<String, Object>> updatePaymentStatus(
      @PathVariable Long paymentId,
      @RequestParam PaymentStatus status) {
    PaymentResponseDTO payment = paymentService.updatePaymentStatus(paymentId, status);
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Payment status updated successfully");
    response.put("payment", payment);
    response.put("remaining_amount", payment.getRemainingAmount());
    return ResponseEntity.ok(response);
  }

  /**
   * Check if an order is fully paid
   * 
   * @param orderId Order ID
   * @return true if remainingAmount ≈ 0
   */
  @GetMapping("/order/{orderId}/is-fully-paid")
  public ResponseEntity<Map<String, Object>> isOrderFullyPaid(@PathVariable Long orderId) {
    Boolean fullyPaid = paymentService.isFullyPaid(orderId);
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Order payment status retrieved");
    response.put("fully_paid", fullyPaid);
    return ResponseEntity.ok(response);
  }
}
