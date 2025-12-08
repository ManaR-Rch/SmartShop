package com.example.smartshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Multi-method payment entity for SmartShop orders
 * 
 * Supports fractional payments with different methods:
 * - CASH: immediate, max 20,000 DH per payment
 * - CHEQUE: can be deferred with due date
 * - TRANSFER: immediate or deferred
 * 
 * Each payment has a sequential number within the order for invoicing
 */
@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Column(nullable = false)
  private Double amount;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private PaymentMethod method;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private PaymentStatus status = PaymentStatus.EN_ATTENTE;

  @Column(nullable = false)
  @Builder.Default
  private LocalDateTime paymentDate = LocalDateTime.now();

  /**
   * Sequential number for invoice purposes (1, 2, 3...)
   * Unique within the order
   */
  @Column(nullable = false)
  private Integer sequenceNumber;

  // ===== CASH Payment Fields =====
  /**
   * Receipt number for cash payments
   */
  @Column(name = "receipt_number")
  private String receiptNumber;

  // ===== CHEQUE Payment Fields =====
  /**
   * Check number for cheque payments
   */
  @Column(name = "check_number")
  private String checkNumber;

  /**
   * Bank name for cheque payments
   */
  @Column(name = "check_bank")
  private String checkBank;

  /**
   * Due date for cheque payments (can be in the future)
   */
  @Column(name = "check_due_date")
  private LocalDate checkDueDate;

  // ===== TRANSFER Payment Fields =====
  /**
   * Transaction reference for transfer payments
   */
  @Column(name = "transfer_reference")
  private String transferReference;

  /**
   * Bank name for transfer payments
   */
  @Column(name = "transfer_bank")
  private String transferBank;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
