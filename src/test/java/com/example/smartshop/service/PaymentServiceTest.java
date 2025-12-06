package com.example.smartshop.service;

import com.example.smartshop.dto.PaymentRequestDTO;
import com.example.smartshop.dto.PaymentResponseDTO;
import com.example.smartshop.entity.*;
import com.example.smartshop.mapper.PaymentMapper;
import com.example.smartshop.repository.OrderRepository;
import com.example.smartshop.repository.PaymentRepository;
import com.example.smartshop.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Tests - Multi-Method Payment System")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentService paymentService;

    private Order order;
    private Payment payment;
    private PaymentRequestDTO requestDTO;
    private PaymentResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        // Create test order
        order = Order.builder()
                .id(1L)
                .total(1000.0)
                .remainingAmount(1000.0)
                .status(OrderStatus.PENDING)
                .build();

        // Create test payment
        payment = Payment.builder()
                .id(1L)
                .order(order)
                .amount(500.0)
                .method(PaymentMethod.CASH)
                .status(PaymentStatus.EN_ATTENTE)
                .sequenceNumber(1)
                .build();

        // Create test request DTO
        requestDTO = PaymentRequestDTO.builder()
                .orderId(1L)
                .amount(500.0)
                .method(PaymentMethod.CASH)
                .receiptNumber("RCP-001")
                .build();

        // Create test response DTO
        responseDTO = PaymentResponseDTO.builder()
                .id(1L)
                .orderId(1L)
                .amount(500.0)
                .method(PaymentMethod.CASH)
                .status(PaymentStatus.EN_ATTENTE)
                .sequenceNumber(1)
                .remainingAmount(500.0)
                .build();
    }

    @Test
    @DisplayName("Should add CASH payment successfully")
    void testAddCashPayment_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findMaxSequenceNumberByOrderId(1L)).thenReturn(0);
        when(paymentRepository.findEncaishedPaymentsByOrderId(1L)).thenReturn(Collections.emptyList());
        when(paymentMapper.toEntity(requestDTO, order)).thenReturn(payment);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toResponseDTO(any(Payment.class), anyDouble())).thenReturn(responseDTO);

        PaymentResponseDTO result = paymentService.addPayment(requestDTO);

        assertNotNull(result);
        assertEquals(500.0, result.getAmount());
        assertEquals(PaymentMethod.CASH, result.getMethod());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should reject CASH payment exceeding 20,000 DH limit")
    void testAddCashPayment_ExceedsLimit() {
        // Create order with large remaining amount to test CASH limit
        Order largeOrder = Order.builder()
                .id(1L)
                .total(30000.0)
                .remainingAmount(30000.0)
                .status(OrderStatus.PENDING)
                .build();

        PaymentRequestDTO overLimitDTO = PaymentRequestDTO.builder()
                .orderId(1L)
                .amount(25000.0)  // Exceeds 20,000 DH CASH limit
                .method(PaymentMethod.CASH)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(largeOrder));

        BusinessRuleViolationException exception = assertThrows(
            BusinessRuleViolationException.class,
            () -> paymentService.addPayment(overLimitDTO)
        );

        assertTrue(exception.getMessage().contains("exceeds legal limit"));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should add CHEQUE payment with required fields")
    void testAddChequePayment_Success() {
        PaymentRequestDTO chequeDTO = PaymentRequestDTO.builder()
                .orderId(1L)
                .amount(300.0)
                .method(PaymentMethod.CHEQUE)
                .checkNumber("CHK-12345")
                .checkBank("Banque Marocaine")
                .checkDueDate(LocalDate.now().plusDays(30))
                .build();

        Payment chequePayment = Payment.builder()
                .id(1L)
                .order(order)
                .amount(300.0)
                .method(PaymentMethod.CHEQUE)
                .status(PaymentStatus.EN_ATTENTE)
                .sequenceNumber(1)
                .checkNumber("CHK-12345")
                .checkBank("Banque Marocaine")
                .checkDueDate(LocalDate.now().plusDays(30))
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findMaxSequenceNumberByOrderId(1L)).thenReturn(0);
        when(paymentRepository.findEncaishedPaymentsByOrderId(1L)).thenReturn(Collections.emptyList());
        when(paymentMapper.toEntity(chequeDTO, order)).thenReturn(chequePayment);
        when(paymentRepository.save(chequePayment)).thenReturn(chequePayment);
        when(paymentMapper.toResponseDTO(any(), anyDouble())).thenReturn(responseDTO);

        PaymentResponseDTO result = paymentService.addPayment(chequeDTO);

        assertNotNull(result);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should reject CHEQUE payment without required fields")
    void testAddChequePayment_MissingFields() {
        PaymentRequestDTO invalidChequeDTO = PaymentRequestDTO.builder()
                .orderId(1L)
                .amount(300.0)
                .method(PaymentMethod.CHEQUE)
                // Missing checkNumber, checkBank, checkDueDate
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findEncaishedPaymentsByOrderId(1L)).thenReturn(Collections.emptyList());

        BusinessRuleViolationException exception = assertThrows(
            BusinessRuleViolationException.class,
            () -> paymentService.addPayment(invalidChequeDTO)
        );

        assertTrue(exception.getMessage().contains("required"));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should add TRANSFER payment with required fields")
    void testAddTransferPayment_Success() {
        PaymentRequestDTO transferDTO = PaymentRequestDTO.builder()
                .orderId(1L)
                .amount(200.0)
                .method(PaymentMethod.TRANSFER)
                .transferReference("VIREMENT-ABC123")
                .transferBank("Banque Centrale")
                .build();

        Payment transferPayment = Payment.builder()
                .id(1L)
                .order(order)
                .amount(200.0)
                .method(PaymentMethod.TRANSFER)
                .status(PaymentStatus.EN_ATTENTE)
                .sequenceNumber(1)
                .transferReference("VIREMENT-ABC123")
                .transferBank("Banque Centrale")
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findMaxSequenceNumberByOrderId(1L)).thenReturn(0);
        when(paymentRepository.findEncaishedPaymentsByOrderId(1L)).thenReturn(Collections.emptyList());
        when(paymentMapper.toEntity(transferDTO, order)).thenReturn(transferPayment);
        when(paymentRepository.save(transferPayment)).thenReturn(transferPayment);
        when(paymentMapper.toResponseDTO(any(), anyDouble())).thenReturn(responseDTO);

        PaymentResponseDTO result = paymentService.addPayment(transferDTO);

        assertNotNull(result);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should reject payment exceeding remaining amount")
    void testAddPayment_ExceedsRemainingAmount() {
        requestDTO.setAmount(1500.0);  // Order total is 1000.0

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findEncaishedPaymentsByOrderId(1L)).thenReturn(Collections.emptyList());

        BusinessRuleViolationException exception = assertThrows(
            BusinessRuleViolationException.class,
            () -> paymentService.addPayment(requestDTO)
        );

        assertTrue(exception.getMessage().contains("exceeds remaining amount"));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should update payment status from EN_ATTENTE to ENCAISSE")
    void testUpdatePaymentStatus_ToEncaisse() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.findEncaishedPaymentsByOrderId(1L)).thenReturn(Collections.emptyList());
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toResponseDTO(any(), anyDouble())).thenReturn(responseDTO);

        PaymentResponseDTO result = paymentService.updatePaymentStatus(1L, PaymentStatus.ENCAISSE);

        assertNotNull(result);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should calculate remaining amount correctly with one encaished payment")
    void testCalculateRemainingAmount_WithOnePayment() {
        Payment encaishedPayment = Payment.builder()
                .id(1L)
                .amount(300.0)
                .status(PaymentStatus.ENCAISSE)
                .build();

        when(paymentRepository.findEncaishedPaymentsByOrderId(1L))
                .thenReturn(Collections.singletonList(encaishedPayment));

        Double remaining = paymentService.calculateRemainingAmount(order);

        assertEquals(700.0, remaining);  // 1000 - 300
    }

    @Test
    @DisplayName("Should calculate remaining amount correctly with multiple payments")
    void testCalculateRemainingAmount_WithMultiplePayments() {
        Payment payment1 = Payment.builder().amount(300.0).status(PaymentStatus.ENCAISSE).build();
        Payment payment2 = Payment.builder().amount(200.0).status(PaymentStatus.ENCAISSE).build();
        Payment payment3 = Payment.builder().amount(150.0).status(PaymentStatus.ENCAISSE).build();

        when(paymentRepository.findEncaishedPaymentsByOrderId(1L))
                .thenReturn(java.util.Arrays.asList(payment1, payment2, payment3));

        Double remaining = paymentService.calculateRemainingAmount(order);

        assertEquals(350.0, remaining);  // 1000 - (300 + 200 + 150)
    }

    @Test
    @DisplayName("Should check if order is fully paid")
    void testIsFullyPaid_WhenTrue() {
        Payment fullPayment = Payment.builder()
                .id(1L)
                .amount(1000.0)
                .status(PaymentStatus.ENCAISSE)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findEncaishedPaymentsByOrderId(1L))
                .thenReturn(Collections.singletonList(fullPayment));

        Boolean fullyPaid = paymentService.isFullyPaid(1L);

        assertTrue(fullyPaid);
    }

    @Test
    @DisplayName("Should check if order is not fully paid")
    void testIsFullyPaid_WhenFalse() {
        Payment partialPayment = Payment.builder()
                .id(1L)
                .amount(500.0)
                .status(PaymentStatus.ENCAISSE)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findEncaishedPaymentsByOrderId(1L))
                .thenReturn(Collections.singletonList(partialPayment));

        Boolean fullyPaid = paymentService.isFullyPaid(1L);

        assertFalse(fullyPaid);
    }

    @Test
    @DisplayName("Should reject payment to non-PENDING order")
    void testAddPayment_ToConfirmedOrder() {
        order.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        BusinessRuleViolationException exception = assertThrows(
            BusinessRuleViolationException.class,
            () -> paymentService.addPayment(requestDTO)
        );

        assertTrue(exception.getMessage().contains("CONFIRMED"));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should reject order not found")
    void testAddPayment_OrderNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessRuleViolationException exception = assertThrows(
            BusinessRuleViolationException.class,
            () -> paymentService.addPayment(requestDTO)
        );

        assertTrue(exception.getMessage().contains("Order not found"));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should support fractional payments with multiple installments")
    void testFractionalPayments_MultipleInstallments() {
        // Test that multiple payments can be added sequentially
        Payment p1 = Payment.builder().amount(300.0).status(PaymentStatus.ENCAISSE).build();
        Payment p2 = Payment.builder().amount(400.0).status(PaymentStatus.ENCAISSE).build();
        Payment p3 = Payment.builder().amount(300.0).status(PaymentStatus.ENCAISSE).build();

        when(paymentRepository.findEncaishedPaymentsByOrderId(1L))
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.singletonList(p1))
                .thenReturn(java.util.Arrays.asList(p1, p2))
                .thenReturn(java.util.Arrays.asList(p1, p2, p3));

        // After 0 payments: 1000 remaining
        Double remaining0 = paymentService.calculateRemainingAmount(order);
        assertEquals(1000.0, remaining0);

        // After 1st payment (300): 700 remaining
        Double remaining1 = paymentService.calculateRemainingAmount(order);
        assertEquals(700.0, remaining1);

        // After 2nd payment (400): 300 remaining
        Double remaining2 = paymentService.calculateRemainingAmount(order);
        assertEquals(300.0, remaining2);

        // After 3rd payment (300): 0 remaining (fully paid)
        Double remaining3 = paymentService.calculateRemainingAmount(order);
        assertEquals(0.0, remaining3);

        Boolean fullyPaid = remaining3 < 0.01;
        assertTrue(fullyPaid);
    }
}
