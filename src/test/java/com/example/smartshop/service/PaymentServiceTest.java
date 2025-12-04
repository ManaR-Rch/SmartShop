package com.example.smartshop.service;

import com.example.smartshop.entity.*;
import com.example.smartshop.dto.PaymentRequestDTO;
import com.example.smartshop.dto.PaymentResponseDTO;
import com.example.smartshop.repository.PaymentRepository;
import com.example.smartshop.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Client client;
    private Order order;
    private Payment payment;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(1L)
                .name("Test Client")
                .email("test@test.com")
                .tier(CustomerTier.GOLD)
                .build();

        order = Order.builder()
                .id(1L)
                .client(client)
                .subtotal(1000.0)
                .discountAmount(100.0)  // 10% de remise GOLD
                .tax(20.0)
                .total(1080.0)  // (1000 - 100) * 1.2
                .remainingAmount(1080.0)
                .status(OrderStatus.PENDING)
                .build();

        payment = Payment.builder()
                .id(1L)
                .order(order)
                .amount(500.0)
                .type("CARD")
                .status(PaymentStatus.ENCAISSE)
                .build();
    }

    @Test
    void testAddPaymentReducesRemainingAmount() {
        // Test: Paiement partiel réduit remainingAmount
        PaymentRequestDTO dto = PaymentRequestDTO.builder()
                .orderId(1L)
                .amount(500.0)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any())).thenReturn(payment);

        PaymentResponseDTO result = paymentService.addPayment(dto);

        assertNotNull(result);
        assertEquals(500.0, result.getAmount());

        // Vérifier que remainingAmount a été réduit
        verify(orderRepository).save(any());
    }

    @Test
    void testPaymentExceedsRemainingAmount() {
        // Test: Paiement > remainingAmount lance exception
        PaymentRequestDTO dto = PaymentRequestDTO.builder()
                .orderId(1L)
                .amount(2000.0)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class, () -> {
            paymentService.addPayment(dto);
        });
    }

    @Test
    void testAutomaticOrderConfirmationWhenRemainingAmountZero() {
        // Test: Quand remainingAmount = 0, order.status passe à CONFIRMED
        order.setRemainingAmount(500.0);  // Seulement 500 restant

        PaymentRequestDTO dto = PaymentRequestDTO.builder()
                .orderId(1L)
                .amount(500.0)  // Paiement complet
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any())).thenReturn(payment);

        PaymentResponseDTO result = paymentService.addPayment(dto);

        assertNotNull(result);
        // Vérifier que l'ordre a été sauvegardé avec le nouveau status
        verify(orderRepository).save(any());
    }

    @Test
    void testPartialPaymentKeepsOrderPending() {
        // Test: Paiement partiel maintient status PENDING
        PaymentRequestDTO dto = PaymentRequestDTO.builder()
                .orderId(1L)
                .amount(540.0)  // Seulement la moitié
                .build();

        Payment partialPayment = Payment.builder()
                .id(1L)
                .order(order)
                .amount(540.0)
                .type("CARD")
                .status(PaymentStatus.ENCAISSE)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any())).thenReturn(partialPayment);

        PaymentResponseDTO result = paymentService.addPayment(dto);

        assertNotNull(result);
        assertEquals(540.0, result.getAmount());

        // RemainingAmount devrait être 1080 - 540 = 540
        verify(orderRepository).save(argThat(savedOrder ->
                Math.abs(savedOrder.getRemainingAmount() - 540.0) < 0.01
        ));
    }

    @Test
    void testFindPaymentById() {
        // Test: Récupération d'un paiement par ID
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        PaymentResponseDTO result = paymentService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(500.0, result.getAmount());
    }

    @Test
    void testPaymentNotFound() {
        // Test: Paiement non trouvé lance exception
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            paymentService.findById(999L);
        });
    }
}
