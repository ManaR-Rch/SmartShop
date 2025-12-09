package com.example.smartshop.service;

import com.example.smartshop.dto.OrderItemRequestDTO;
import com.example.smartshop.dto.OrderRequestDTO;
import com.example.smartshop.dto.OrderResponseDTO;
import com.example.smartshop.entity.*;
import com.example.smartshop.exception.BusinessRuleViolationException;
import com.example.smartshop.mapper.OrderMapper;
import com.example.smartshop.repository.ClientRepository;
import com.example.smartshop.repository.OrderRepository;
import com.example.smartshop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @Mock
    private ClientService clientService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        // Default behavior for OrderMapper.toResponseDTO
        lenient()
                .when(orderMapper.toResponseDTO(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    OrderResponseDTO dto = new OrderResponseDTO();
                    dto.setId(order.getId());
                    dto.setSubtotal(order.getSubtotal());
                    dto.setDiscountAmount(order.getDiscountAmount());
                    dto.setTax(order.getTax());
                    dto.setTotal(order.getTotal());
                    // Store OrderStatus as-is, not as String
                    if (order.getStatus() != null) {
                        // Try to use reflection to set status directly
                        dto.setStatus(order.getStatus().name());
                    } else {
                        dto.setStatus("PENDING");
                    }
                    dto.setPromoCode(order.getPromoCode());
                    return dto;
                });
    }

    @Test
    void createOrderBasicTierSuccess() {
        Client client = new Client();
        client.setId(1L);
        client.setName("John Doe");
        client.setTier(CustomerTier.BASIC);

        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(100.0);
        product.setStock(10);

        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO();
        itemDTO.setProductId(1L);
        itemDTO.setQuantity(2);
        itemDTO.setUnitPrice(100.0);

        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setClientId(1L);
        dto.setItems(List.of(itemDTO));

        Order order = new Order();
        order.setId(1L);
        order.setClient(client);
        order.setStatus(OrderStatus.PENDING);
        order.setSubtotal(200.0);
        order.setDiscountAmount(0.0);
        order.setTax(20.0);
        order.setTotal(240.0);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(productRepository.findAllById(List.of(1L))).thenReturn(List.of(product));
        when(orderMapper.toEntity(any(OrderRequestDTO.class), any(Client.class))).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponseDTO result = orderService.create(dto);

        assertNotNull(result);
        assertEquals(200.0, result.getSubtotal());
        assertEquals(0.0, result.getDiscountAmount());
    }

    @Test
    void createOrderSilverTierApplyDiscount() {
        Client client = new Client();
        client.setId(1L);
        client.setName("John Doe");
        client.setTier(CustomerTier.SILVER);

        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(250.0);
        product.setStock(10);

        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO();
        itemDTO.setProductId(1L);
        itemDTO.setQuantity(2);
        itemDTO.setUnitPrice(250.0);

        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setClientId(1L);
        dto.setItems(List.of(itemDTO));

        Order order = new Order();
        order.setId(1L);
        order.setClient(client);
        order.setStatus(OrderStatus.PENDING);
        order.setSubtotal(500.0);
        order.setDiscountAmount(25.0);
        order.setTax(20.0);
        order.setTotal(600.0);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(productRepository.findAllById(List.of(1L))).thenReturn(List.of(product));
        when(orderMapper.toEntity(any(OrderRequestDTO.class), any(Client.class))).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponseDTO result = orderService.create(dto);

        assertNotNull(result);
        assertEquals(500.0, result.getSubtotal());
        assertEquals(25.0, result.getDiscountAmount());
    }

    @Test
    void createOrderGoldTierApplyDiscount() {
        Client client = new Client();
        client.setId(1L);
        client.setName("John Doe");
        client.setTier(CustomerTier.GOLD);

        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(400.0);
        product.setStock(10);

        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO();
        itemDTO.setProductId(1L);
        itemDTO.setQuantity(2);
        itemDTO.setUnitPrice(400.0);

        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setClientId(1L);
        dto.setItems(List.of(itemDTO));

        Order order = new Order();
        order.setId(1L);
        order.setClient(client);
        order.setStatus(OrderStatus.PENDING);
        order.setSubtotal(800.0);
        order.setDiscountAmount(80.0);
        order.setTax(20.0);
        order.setTotal(864.0);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(productRepository.findAllById(List.of(1L))).thenReturn(List.of(product));
        when(orderMapper.toEntity(any(OrderRequestDTO.class), any(Client.class))).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponseDTO result = orderService.create(dto);

        assertNotNull(result);
        assertEquals(800.0, result.getSubtotal());
        assertEquals(80.0, result.getDiscountAmount());
    }

    @Test
    void createOrderPlatinumTierApplyDiscount() {
        Client client = new Client();
        client.setId(1L);
        client.setName("John Doe");
        client.setTier(CustomerTier.PLATINUM);

        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(600.0);
        product.setStock(10);

        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO();
        itemDTO.setProductId(1L);
        itemDTO.setQuantity(2);
        itemDTO.setUnitPrice(600.0);

        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setClientId(1L);
        dto.setItems(List.of(itemDTO));

        Order order = new Order();
        order.setId(1L);
        order.setClient(client);
        order.setStatus(OrderStatus.PENDING);
        order.setSubtotal(1200.0);
        order.setDiscountAmount(180.0);
        order.setTax(20.0);
        order.setTotal(1440.0);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(productRepository.findAllById(List.of(1L))).thenReturn(List.of(product));
        when(orderMapper.toEntity(any(OrderRequestDTO.class), any(Client.class))).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponseDTO result = orderService.create(dto);

        assertNotNull(result);
        assertEquals(1200.0, result.getSubtotal());
        assertEquals(180.0, result.getDiscountAmount());
    }

    @Test
    void createOrderWithValidPromoCode() {
        Client client = new Client();
        client.setId(1L);
        client.setName("John Doe");
        client.setTier(CustomerTier.BASIC);

        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(100.0);
        product.setStock(10);

        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO();
        itemDTO.setProductId(1L);
        itemDTO.setQuantity(2);
        itemDTO.setUnitPrice(100.0);

        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setClientId(1L);
        dto.setItems(List.of(itemDTO));
        dto.setPromoCode("PROMO-ABC1");

        Order order = new Order();
        order.setId(1L);
        order.setClient(client);
        order.setStatus(OrderStatus.PENDING);
        order.setSubtotal(200.0);
        order.setDiscountAmount(10.0);
        order.setPromoCode("PROMO-ABC1");

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(productRepository.findAllById(List.of(1L))).thenReturn(List.of(product));
        when(orderMapper.toEntity(any(OrderRequestDTO.class), any(Client.class))).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponseDTO result = orderService.create(dto);

        assertNotNull(result);
        assertEquals("PROMO-ABC1", result.getPromoCode());
    }

    @Test
    void createOrderWithInvalidPromoCode() {
        Client client = new Client();
        client.setId(1L);
        client.setTier(CustomerTier.BASIC);

        Product product = new Product();
        product.setId(1L);
        product.setPrice(100.0);
        product.setStock(10);

        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO();
        itemDTO.setProductId(1L);
        itemDTO.setQuantity(2);
        itemDTO.setUnitPrice(100.0);

        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setClientId(1L);
        dto.setItems(List.of(itemDTO));
        dto.setPromoCode("INVALID-CODE");

        Order order = new Order();
        order.setClient(client);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(productRepository.findAllById(List.of(1L))).thenReturn(List.of(product));
        when(orderMapper.toEntity(any(OrderRequestDTO.class), any(Client.class))).thenReturn(order);

        assertThrows(BusinessRuleViolationException.class, () -> orderService.create(dto));
    }

    @Test
    void createOrderInsufficientStock() {
        Client client = new Client();
        client.setId(1L);
        client.setTier(CustomerTier.BASIC);

        Product product = new Product();
        product.setId(1L);
        product.setPrice(100.0);
        product.setStock(1);

        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO();
        itemDTO.setProductId(1L);
        itemDTO.setQuantity(2);
        itemDTO.setUnitPrice(100.0);

        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setClientId(1L);
        dto.setItems(List.of(itemDTO));

        Order order = new Order();
        order.setClient(client);
        order.setStatus(OrderStatus.REJECTED);
        order.setSubtotal(0.0);
        order.setDiscountAmount(0.0);
        order.setTotal(0.0);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(productRepository.findAllById(List.of(1L))).thenReturn(List.of(product));
        when(orderMapper.toEntity(any(OrderRequestDTO.class), any(Client.class))).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponseDTO result = orderService.create(dto);

        assertNotNull(result);
        assertEquals("REJECTED", result.getStatus());
    }

    @Test
    void createOrderClientNotFound() {
        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO();
        itemDTO.setProductId(1L);
        itemDTO.setQuantity(2);

        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setClientId(99L);
        dto.setItems(List.of(itemDTO));

        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessRuleViolationException.class, () -> orderService.create(dto));
    }

    @Test
    void createOrderEmptyItems() {
        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setClientId(1L);
        dto.setItems(List.of());

        assertThrows(BusinessRuleViolationException.class, () -> orderService.create(dto));
    }

    @Test
    void createOrderNullItems() {
        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setClientId(1L);
        dto.setItems(null);

        assertThrows(BusinessRuleViolationException.class, () -> orderService.create(dto));
    }

    @Test
    void createOrderProductNotFound() {
        Client client = new Client();
        client.setId(1L);
        client.setTier(CustomerTier.BASIC);

        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO();
        itemDTO.setProductId(1L);
        itemDTO.setQuantity(2);

        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setClientId(1L);
        dto.setItems(List.of(itemDTO));

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(productRepository.findAllById(List.of(1L))).thenReturn(List.of());

        assertThrows(BusinessRuleViolationException.class, () -> orderService.create(dto));
    }

    @Test
    void createOrderMultipleItems() {
        Client client = new Client();
        client.setId(1L);
        client.setTier(CustomerTier.BASIC);

        Product product1 = new Product();
        product1.setId(1L);
        product1.setPrice(100.0);
        product1.setStock(10);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setPrice(50.0);
        product2.setStock(10);

        OrderItemRequestDTO item1 = new OrderItemRequestDTO();
        item1.setProductId(1L);
        item1.setQuantity(2);
        item1.setUnitPrice(100.0);

        OrderItemRequestDTO item2 = new OrderItemRequestDTO();
        item2.setProductId(2L);
        item2.setQuantity(1);
        item2.setUnitPrice(50.0);

        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setClientId(1L);
        dto.setItems(List.of(item1, item2));

        Order order = new Order();
        order.setId(1L);
        order.setClient(client);
        order.setStatus(OrderStatus.PENDING);
        order.setSubtotal(250.0);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(productRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(product1, product2));
        when(orderMapper.toEntity(any(OrderRequestDTO.class), any(Client.class))).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponseDTO result = orderService.create(dto);

        assertNotNull(result);
        assertEquals(250.0, result.getSubtotal());
    }
}
