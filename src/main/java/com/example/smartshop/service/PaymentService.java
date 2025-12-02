package com.example.smartshop.service;

import com.example.smartshop.entity.Payment;
import com.example.smartshop.entity.Order;
import com.example.smartshop.entity.OrderStatus;
import com.example.smartshop.entity.PaymentStatus;
import com.example.smartshop.dto.PaymentRequestDTO;
import com.example.smartshop.dto.PaymentResponseDTO;
import com.example.smartshop.repository.PaymentRepository;
import com.example.smartshop.repository.OrderRepository;
import com.example.smartshop.exception.BusinessRuleViolationException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    public PaymentResponseDTO addPayment(PaymentRequestDTO dto) {
        // Validation de la commande
        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new BusinessRuleViolationException("Order not found"));

        // Validation du montant
        if (dto.getAmount() <= 0) {
            throw new BusinessRuleViolationException("Payment amount must be positive");
        }

        // Vérifier que le montant ne dépasse pas le montant restant
        if (dto.getAmount() > order.getRemainingAmount()) {
            throw new BusinessRuleViolationException("Payment amount exceeds remaining amount");
        }

        // Créer le paiement
        Payment payment = Payment.builder()
                .order(order)
                .amount(dto.getAmount())
                .type("CARD")
                .paymentDate(LocalDateTime.now())
                .status(PaymentStatus.ENCAISSE)
                .build();

        // Réduire le montant restant
        Double newRemainingAmount = order.getRemainingAmount() - dto.getAmount();
        order.setRemainingAmount(newRemainingAmount);

        // ===== CONFIRMATION AUTOMATIQUE =====
        // Si remainingAmount == 0, passer le status à CONFIRMED
        if (Math.abs(newRemainingAmount) < 0.01) {  // Utiliser une comparaison avec epsilon pour les doubles
            order.setStatus(OrderStatus.CONFIRMED);
            payment.setStatus(PaymentStatus.ENCAISSE);
        }

        // Sauvegarder
        payment = paymentRepository.save(payment);
        orderRepository.save(order);

        return toResponseDTO(payment, order.getRemainingAmount());
    }

    public PaymentResponseDTO findById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleViolationException("Payment not found"));

        Order order = payment.getOrder();
        return toResponseDTO(payment, order.getRemainingAmount());
    }

    public List<PaymentResponseDTO> findByOrderId(Long orderId) {
        List<Payment> payments = paymentRepository.findByOrderId(orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessRuleViolationException("Order not found"));

        return payments.stream()
                .map(p -> toResponseDTO(p, order.getRemainingAmount()))
                .collect(Collectors.toList());
    }

    private PaymentResponseDTO toResponseDTO(Payment payment, Double remainingAmount) {
        return PaymentResponseDTO.builder()
                .id(payment.getId())
                .orderId(payment.getOrder() != null ? payment.getOrder().getId() : null)
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .status(payment.getStatus() != null ? payment.getStatus().name() : "EN_ATTENTE")
                .remainingAmount(remainingAmount)
                .build();
    }
}
