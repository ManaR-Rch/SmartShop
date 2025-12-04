package com.example.smartshop.service;

import com.example.smartshop.entity.*;
import com.example.smartshop.dto.OrderRequestDTO;
import com.example.smartshop.dto.OrderItemRequestDTO;
import com.example.smartshop.dto.OrderResponseDTO;
import com.example.smartshop.repository.OrderRepository;
import com.example.smartshop.repository.ClientRepository;
import com.example.smartshop.repository.ProductRepository;
import com.example.smartshop.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private Client platinumClient;
    private Client goldClient;
    private Client silverClient;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        // Créer les clients avec différents tiers
        platinumClient = Client.builder()
                .id(1L)
                .name("Platinum Client")
                .email("platinum@test.com")
                .tier(CustomerTier.PLATINUM)
                .totalOrders(0)
                .totalSpent(0.0)
                .build();

        goldClient = Client.builder()
                .id(2L)
                .name("Gold Client")
                .email("gold@test.com")
                .tier(CustomerTier.GOLD)
                .totalOrders(0)
                .totalSpent(0.0)
                .build();

        silverClient = Client.builder()
                .id(3L)
                .name("Silver Client")
                .email("silver@test.com")
                .tier(CustomerTier.SILVER)
                .totalOrders(0)
                .totalSpent(0.0)
                .build();

        // Créer les produits
        product1 = Product.builder()
                .id(1L)
                .name("Product 1")
                .price(500.0)
                .stock(10)
                .build();

        product2 = Product.builder()
                .id(2L)
                .name("Product 2")
                .price(300.0)
                .stock(10)
                .build();
    }

    @Test
    void testPlatinumDiscountWith1500Subtotal() {
        // PLATINUM → 15% si subTotal >= 1200
        OrderRequestDTO dto = OrderRequestDTO.builder()
                .clientId(1L)
                .items(Arrays.asList(
                        OrderItemRequestDTO.builder()
                                .productId(1L)
                                .quantity(3)
                                .unitPrice(500.0)
                                .build()
                ))
                .build();

        Order mockOrder = Order.builder()
                .id(1L)
                .client(platinumClient)
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = Order.builder()
                .id(1L)
                .client(platinumClient)
                .status(OrderStatus.PENDING)
                .subtotal(1500.0)
                .discountAmount(225.0)
                .tax(20.0)
                .total(1530.0)
                .remainingAmount(1530.0)
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(platinumClient));
        when(productRepository.findAllById(any())).thenReturn(Arrays.asList(product1));
        when(orderRepository.save(any())).thenReturn(savedOrder);
        when(orderMapper.toResponseDTO(savedOrder)).thenReturn(OrderResponseDTO.builder()
                .id(1L)
                .subtotal(1500.0)
                .discountAmount(225.0)
                .total(1530.0)
                .build());

        OrderResponseDTO result = orderService.create(dto);

        assertNotNull(result);
        assertEquals(225.0, result.getDiscountAmount());
        assertEquals(1530.0, result.getTotal());
    }

    @Test
    void testGoldDiscountWith1000Subtotal() {
        // GOLD → 10% si subTotal >= 800
        OrderRequestDTO dto = OrderRequestDTO.builder()
                .clientId(2L)
                .items(Arrays.asList(
                        OrderItemRequestDTO.builder()
                                .productId(1L)
                                .quantity(2)
                                .unitPrice(500.0)
                                .build()
                ))
                .build();

        Order savedOrder = Order.builder()
                .id(2L)
                .client(goldClient)
                .status(OrderStatus.PENDING)
                .subtotal(1000.0)
                .discountAmount(100.0)
                .tax(20.0)
                .total(1080.0)
                .remainingAmount(1080.0)
                .build();

        when(clientRepository.findById(2L)).thenReturn(Optional.of(goldClient));
        when(productRepository.findAllById(any())).thenReturn(Arrays.asList(product1));
        when(orderRepository.save(any())).thenReturn(savedOrder);
        when(orderMapper.toResponseDTO(savedOrder)).thenReturn(OrderResponseDTO.builder()
                .id(2L)
                .subtotal(1000.0)
                .discountAmount(100.0)
                .total(1080.0)
                .build());

        OrderResponseDTO result = orderService.create(dto);

        assertNotNull(result);
        assertEquals(100.0, result.getDiscountAmount());
        assertEquals(1080.0, result.getTotal());
    }

    @Test
    void testSilverDiscountWith600Subtotal() {
        // SILVER → 5% si subTotal >= 500
        OrderRequestDTO dto = OrderRequestDTO.builder()
                .clientId(3L)
                .items(Arrays.asList(
                        OrderItemRequestDTO.builder()
                                .productId(1L)
                                .quantity(1)
                                .unitPrice(600.0)
                                .build()
                ))
                .build();

        Order savedOrder = Order.builder()
                .id(3L)
                .client(silverClient)
                .status(OrderStatus.PENDING)
                .subtotal(600.0)
                .discountAmount(30.0)
                .tax(20.0)
                .total(684.0)
                .remainingAmount(684.0)
                .build();

        when(clientRepository.findById(3L)).thenReturn(Optional.of(silverClient));
        when(productRepository.findAllById(any())).thenReturn(Arrays.asList(product1));
        when(orderRepository.save(any())).thenReturn(savedOrder);
        when(orderMapper.toResponseDTO(savedOrder)).thenReturn(OrderResponseDTO.builder()
                .id(3L)
                .subtotal(600.0)
                .discountAmount(30.0)
                .total(684.0)
                .build());

        OrderResponseDTO result = orderService.create(dto);

        assertNotNull(result);
        assertEquals(30.0, result.getDiscountAmount());
        assertEquals(684.0, result.getTotal());
    }

    @Test
    void testPromoCodeAddsDiscount() {
        // Code promo → +5% cumulable
        OrderRequestDTO dto = OrderRequestDTO.builder()
                .clientId(2L)
                .items(Arrays.asList(
                        OrderItemRequestDTO.builder()
                                .productId(1L)
                                .quantity(2)
                                .unitPrice(500.0)
                                .build()
                ))
                .promoCode("PROMO-ABC5")
                .build();

        Order savedOrder = Order.builder()
                .id(4L)
                .client(goldClient)
                .status(OrderStatus.PENDING)
                .subtotal(1000.0)
                .discountAmount(150.0)  // 10% GOLD + 5% PROMO = 15%
                .tax(20.0)
                .total(1020.0)  // (1000 - 150) * 1.2
                .remainingAmount(1020.0)
                .build();

        when(clientRepository.findById(2L)).thenReturn(Optional.of(goldClient));
        when(productRepository.findAllById(any())).thenReturn(Arrays.asList(product1));
        when(orderRepository.save(any())).thenReturn(savedOrder);
        when(orderMapper.toResponseDTO(savedOrder)).thenReturn(OrderResponseDTO.builder()
                .id(4L)
                .subtotal(1000.0)
                .discountAmount(150.0)
                .total(1020.0)
                .build());

        OrderResponseDTO result = orderService.create(dto);

        assertNotNull(result);
        assertEquals(150.0, result.getDiscountAmount());
        assertEquals(1020.0, result.getTotal());
    }

    @Test
    void testSilverNoDiscountBelowThreshold() {
        // SILVER avec subTotal < 500 → 0%
        OrderRequestDTO dto = OrderRequestDTO.builder()
                .clientId(3L)
                .items(Arrays.asList(
                        OrderItemRequestDTO.builder()
                                .productId(2L)
                                .quantity(1)
                                .unitPrice(300.0)
                                .build()
                ))
                .build();

        Order savedOrder = Order.builder()
                .id(5L)
                .client(silverClient)
                .status(OrderStatus.PENDING)
                .subtotal(300.0)
                .discountAmount(0.0)  // Pas de remise (300 < 500)
                .tax(20.0)
                .total(360.0)  // 300 * 1.2
                .remainingAmount(360.0)
                .build();

        when(clientRepository.findById(3L)).thenReturn(Optional.of(silverClient));
        when(productRepository.findAllById(any())).thenReturn(Arrays.asList(product2));
        when(orderRepository.save(any())).thenReturn(savedOrder);
        when(orderMapper.toResponseDTO(savedOrder)).thenReturn(OrderResponseDTO.builder()
                .id(5L)
                .subtotal(300.0)
                .discountAmount(0.0)
                .total(360.0)
                .build());

        OrderResponseDTO result = orderService.create(dto);

        assertNotNull(result);
        assertEquals(0.0, result.getDiscountAmount());
        assertEquals(360.0, result.getTotal());
    }
}
