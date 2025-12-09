package com.example.smartshop.service;

import com.example.smartshop.dto.PaymentRequestDTO;
import com.example.smartshop.dto.PaymentResponseDTO;
import com.example.smartshop.entity.*;
import com.example.smartshop.repository.OrderRepository;
import com.example.smartshop.repository.PaymentRepository;
import com.example.smartshop.mapper.PaymentMapper;
import com.example.smartshop.exception.BusinessRuleViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Multi-method payment service for SmartShop
 * 
 * Supports:
 * - CASH payments: max 20,000 DH per payment (Article 193 CGI)
 * - CHEQUE payments: can be deferred with due date
 * - TRANSFER payments: immediate or deferred
 * 
 * Fractional payments: orders can be paid in multiple installments
 * Sequential numbering: each payment has a sequence number for invoicing
 */
@Service
@Transactional
public class PaymentService {

  private static final Double CASH_LEGAL_LIMIT = 20000.0; // Article 193 CGI
  private static final Double EPSILON = 0.01; // For double comparison

  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;
  private final PaymentMapper paymentMapper;

  public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository,
      PaymentMapper paymentMapper) {
    this.paymentRepository = paymentRepository;
    this.orderRepository = orderRepository;
    this.paymentMapper = paymentMapper;
  }

  /**
   * Add a new payment to an order
   * 
   * Validations:
   * - Order must exist and not be finalized (CONFIRMED/REJECTED/CANCELED)
   * - Payment method specific validations
   * - CASH limit: max 20,000 DH per payment (Article 193 CGI)
   * - Cannot exceed remaining amount
   * - Required fields must be provided based on payment method
   * 
   * @param dto Payment request DTO
   * @return PaymentResponseDTO with updated remaining amount
   */
  public PaymentResponseDTO addPayment(PaymentRequestDTO dto) {
    // Validate order exists
    Order order = orderRepository.findById(dto.getOrderId())
        .orElseThrow(() -> new BusinessRuleViolationException("Order not found"));

    // Validate order is in PENDING status (not finalized)
    if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.REJECTED) {
      throw new BusinessRuleViolationException("Cannot add payment to order with status: " + order.getStatus());
    }

    // Calculate remaining amount before this payment
    Double remainingAmount = calculateRemainingAmount(order);

    // Validate payment amount doesn't exceed remaining
    if (dto.getAmount() > remainingAmount + EPSILON) {
      throw new BusinessRuleViolationException(
          "Payment amount (" + dto.getAmount() + ") exceeds remaining amount (" + remainingAmount + ")");
    }

    // Validate method-specific requirements
    validatePaymentMethodRequirements(dto);

    // Validate CASH payments don't exceed legal limit
    if (dto.getPaymentMethod() == PaymentMethod.CASH) {
      if (dto.getAmount() > CASH_LEGAL_LIMIT) {
        throw new BusinessRuleViolationException(
            "Cash payment (" + dto.getAmount() + ") exceeds legal limit of " + CASH_LEGAL_LIMIT
                + " DH (Article 193 CGI)");
      }
    }

    // Create payment entity
    Payment payment = paymentMapper.toEntity(dto, order);
    
    // For CASH payments, mark as ENCAISSE immediately (immediate payment)
    // For CHEQUE and TRANSFER, mark as EN_ATTENTE (deferred/pending)
    if (dto.getPaymentMethod() == PaymentMethod.CASH) {
      payment.setStatus(PaymentStatus.ENCAISSE);
    } else {
      payment.setStatus(PaymentStatus.EN_ATTENTE);
    }
    
    payment.setSequenceNumber(generateSequenceNumber(order.getId()));

    // Save payment
    payment = paymentRepository.save(payment);

    // Calculate new remaining amount after this payment
    Double newRemainingAmount = calculateRemainingAmount(order);
    
    // Update order's remaining amount
    order.setRemainingAmount(newRemainingAmount);
    orderRepository.save(order);

    // If fully paid, can be confirmed by ADMIN
    // (Confirmation is done through OrderService.confirmOrder())

    return paymentMapper.toResponseDTO(payment, newRemainingAmount);
  }

  /**
   * Update payment status (EN_ATTENTE → ENCAISSE / REJETÉ)
   * 
   * When a payment is marked as ENCAISSE, the order's remainingAmount is
   * recalculated
   * If remainingAmount becomes 0, the order can be confirmed
   * 
   * @param paymentId Payment ID
   * @param newStatus New status
   * @return Updated PaymentResponseDTO
   */
  public PaymentResponseDTO updatePaymentStatus(Long paymentId, PaymentStatus newStatus) {
    Payment payment = paymentRepository.findById(paymentId)
        .orElseThrow(() -> new BusinessRuleViolationException("Payment not found"));

    // Validate status transition
    validateStatusTransition(payment.getStatus(), newStatus);

    payment.setStatus(newStatus);
    payment = paymentRepository.save(payment);

    Order order = payment.getOrder();
    Double remainingAmount = calculateRemainingAmount(order);

    return paymentMapper.toResponseDTO(payment, remainingAmount);
  }

  /**
   * Get payment details by payment ID
   * 
   * @param paymentId Payment ID
   * @return PaymentResponseDTO
   */
  public PaymentResponseDTO getPaymentById(Long paymentId) {
    Payment payment = paymentRepository.findById(paymentId)
        .orElseThrow(() -> new BusinessRuleViolationException("Payment not found"));

    Double remainingAmount = calculateRemainingAmount(payment.getOrder());
    return paymentMapper.toResponseDTO(payment, remainingAmount);
  }

  /**
   * Get all payments for an order
   * 
   * @param orderId Order ID
   * @return List of PaymentResponseDTO
   */
  public List<PaymentResponseDTO> getPaymentsByOrderId(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new BusinessRuleViolationException("Order not found"));

    List<Payment> payments = paymentRepository.findByOrderId(orderId);
    Double remainingAmount = calculateRemainingAmount(order);

    return payments.stream()
        .map(p -> paymentMapper.toResponseDTO(p, remainingAmount))
        .collect(Collectors.toList());
  }

  /**
   * Get all payments for an order with pagination
   * 
   * @param orderId  Order ID
   * @param pageable Pagination info
   * @return Page of PaymentResponseDTO
   */
  public Page<PaymentResponseDTO> getPaymentsByOrderIdPaginated(Long orderId, Pageable pageable) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new BusinessRuleViolationException("Order not found"));

    Page<Payment> payments = paymentRepository.findByOrderIdPageable(orderId, pageable);
    Double remainingAmount = calculateRemainingAmount(order);

    return payments.map(p -> paymentMapper.toResponseDTO(p, remainingAmount));
  }

  /**
   * Get payments by status
   * 
   * @param status   Payment status
   * @param pageable Pagination info
   * @return Page of PaymentResponseDTO
   */
  public Page<PaymentResponseDTO> getPaymentsByStatus(PaymentStatus status, Pageable pageable) {
    Page<Payment> payments = paymentRepository.findByStatus(status, pageable);
    return payments.map(p -> paymentMapper.toResponseDTO(p, calculateRemainingAmount(p.getOrder())));
  }

  /**
   * Get payments by method
   * 
   * @param method   Payment method
   * @param pageable Pagination info
   * @return Page of PaymentResponseDTO
   */
  public Page<PaymentResponseDTO> getPaymentsByMethod(PaymentMethod method, Pageable pageable) {
    Page<Payment> payments = paymentRepository.findByMethod(method, pageable);
    return payments.map(p -> paymentMapper.toResponseDTO(p, calculateRemainingAmount(p.getOrder())));
  }

  /**
   * Calculate the remaining amount for an order
   * 
   * Formula: remainingAmount = order.total - sum(ENCAISSÉ payments)
   * 
   * @param order The order
   * @return Remaining amount to be paid
   */
  public Double calculateRemainingAmount(Order order) {
    List<Payment> encaishedPayments = paymentRepository.findEncaishedPaymentsByOrderId(order.getId());
    Double totalPaid = encaishedPayments.stream()
        .mapToDouble(Payment::getAmount)
        .sum();
    return roundToTwoDecimals(order.getTotal() - totalPaid);
  }

  /**
   * Check if an order is fully paid
   * 
   * @param orderId Order ID
   * @return true if remainingAmount ≈ 0
   */
  public Boolean isFullyPaid(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new BusinessRuleViolationException("Order not found"));
    Double remaining = calculateRemainingAmount(order);
    return remaining < EPSILON;
  }

  /**
   * Generate the next sequence number for a payment within an order
   * 
   * @param orderId Order ID
   * @return Next sequence number (1-based)
   */
  private Integer generateSequenceNumber(Long orderId) {
    Integer maxSeq = paymentRepository.findMaxSequenceNumberByOrderId(orderId);
    return maxSeq + 1;
  }

  /**
   * Validate payment method specific requirements
   * 
   * - CASH: no required fields (receiptNumber optional)
   * - CHEQUE: requires checkNumber, checkBank, checkDueDate
   * - TRANSFER: requires transferReference, transferBank
   * 
   * @param dto Payment request DTO
   */
  private void validatePaymentMethodRequirements(PaymentRequestDTO dto) {
    switch (dto.getPaymentMethod()) {
      case CASH:
        // CASH: receiptNumber is optional
        break;

      case CHEQUE:
        if (dto.getCheckNumber() == null || dto.getCheckNumber().isEmpty()) {
          throw new BusinessRuleViolationException("Check number is required for CHEQUE payment");
        }
        if (dto.getCheckBank() == null || dto.getCheckBank().isEmpty()) {
          throw new BusinessRuleViolationException("Check bank is required for CHEQUE payment");
        }
        if (dto.getCheckDueDate() == null) {
          throw new BusinessRuleViolationException("Check due date is required for CHEQUE payment");
        }
        break;

      case TRANSFER:
        if (dto.getTransferReference() == null || dto.getTransferReference().isEmpty()) {
          throw new BusinessRuleViolationException("Transfer reference is required for TRANSFER payment");
        }
        if (dto.getTransferBank() == null || dto.getTransferBank().isEmpty()) {
          throw new BusinessRuleViolationException("Transfer bank is required for TRANSFER payment");
        }
        break;

      default:
        throw new BusinessRuleViolationException("Unknown payment method: " + dto.getPaymentMethod());
    }
  }

  /**
   * Validate payment status transitions
   * 
   * Valid transitions:
   * - EN_ATTENTE → ENCAISSÉ (payment collected)
   * - EN_ATTENTE → REJETÉ (payment rejected)
   * 
   * @param currentStatus Current status
   * @param newStatus     New status
   */
  private void validateStatusTransition(PaymentStatus currentStatus, PaymentStatus newStatus) {
    if (currentStatus == newStatus) {
      throw new BusinessRuleViolationException("Payment is already in " + newStatus + " status");
    }

    if (currentStatus == PaymentStatus.EN_ATTENTE) {
      if (newStatus == PaymentStatus.ENCAISSE || newStatus == PaymentStatus.REJETÉ) {
        return; // Valid transition
      }
    } else if (currentStatus == PaymentStatus.ENCAISSE || currentStatus == PaymentStatus.REJETÉ) {
      throw new BusinessRuleViolationException("Cannot transition from " + currentStatus + " to " + newStatus);
    }

    throw new BusinessRuleViolationException("Invalid status transition from " + currentStatus + " to " + newStatus);
  }

  /**
   * Round value to 2 decimal places
   * 
   * @param value Value to round
   * @return Rounded value
   */
  private Double roundToTwoDecimals(Double value) {
    if (value == null)
      return 0.0;
    return Math.round(value * 100.0) / 100.0;
  }
}
