package com.example.smartshop.mapper;

import com.example.smartshop.dto.PaymentRequestDTO;
import com.example.smartshop.dto.PaymentResponseDTO;
import com.example.smartshop.entity.Order;
import com.example.smartshop.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

  public PaymentResponseDTO toResponseDTO(Payment payment, Double remainingAmount) {
    if (payment == null) {
      return null;
    }

    return PaymentResponseDTO.builder()
        .id(payment.getId())
        .orderId(payment.getOrder().getId())
        .amount(payment.getAmount())
        .method(payment.getMethod())
        .status(payment.getStatus())
        .paymentDate(payment.getPaymentDate())
        .sequenceNumber(payment.getSequenceNumber())
        .receiptNumber(payment.getReceiptNumber())
        .checkNumber(payment.getCheckNumber())
        .checkBank(payment.getCheckBank())
        .checkDueDate(payment.getCheckDueDate())
        .transferReference(payment.getTransferReference())
        .transferBank(payment.getTransferBank())
        .remainingAmount(remainingAmount)
        .createdAt(payment.getCreatedAt())
        .updatedAt(payment.getUpdatedAt())
        .build();
  }

  public Payment toEntity(PaymentRequestDTO dto, Order order) {
    if (dto == null) {
      return null;
    }

    return Payment.builder()
        .order(order)
        .amount(dto.getAmount())
        .method(dto.getMethod())
        .receiptNumber(dto.getReceiptNumber())
        .checkNumber(dto.getCheckNumber())
        .checkBank(dto.getCheckBank())
        .checkDueDate(dto.getCheckDueDate())
        .transferReference(dto.getTransferReference())
        .transferBank(dto.getTransferBank())
        .build();
  }
}
