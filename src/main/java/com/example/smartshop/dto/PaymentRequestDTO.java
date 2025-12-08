package com.example.smartshop.dto;

import com.example.smartshop.entity.PaymentMethod;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * DTO for creating or updating payments
 * 
 * Required fields depend on payment method:
 * - CASH: receiptNumber (optional), max amount 20,000 DH
 * - CHEQUE: checkNumber, checkBank, checkDueDate
 * - TRANSFER: transferReference, transferBank
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {

  @NotNull(message = "Order ID is required")
  private Long orderId;

  @NotNull(message = "Amount is required")
  @Positive(message = "Amount must be positive")
  @DecimalMax(value = "20000.00", message = "Cash payments cannot exceed 20,000 DH (Article 193 CGI)")
  private Double amount;

  @NotNull(message = "Payment method is required")
  private PaymentMethod method;

  // CASH fields
  @JsonProperty("receipt_number")
  private String receiptNumber;

  // CHEQUE fields
  @JsonProperty("check_number")
  private String checkNumber;

  @JsonProperty("check_bank")
  private String checkBank;

  @JsonProperty("check_due_date")
  private LocalDate checkDueDate;

  // TRANSFER fields
  @JsonProperty("transfer_reference")
  private String transferReference;

  @JsonProperty("transfer_bank")
  private String transferBank;
}
