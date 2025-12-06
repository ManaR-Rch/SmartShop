package com.example.smartshop.dto;

import com.example.smartshop.entity.PaymentMethod;
import com.example.smartshop.entity.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for payment response
 * 
 * Contains all payment details and remaining order amount
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {

    private Long id;

    private Long orderId;

    private Double amount;

    private PaymentMethod method;

    private PaymentStatus status;

    private LocalDateTime paymentDate;

    @JsonProperty("sequence_number")
    private Integer sequenceNumber;

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

    @JsonProperty("remaining_amount")
    private Double remainingAmount;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
