package com.example.smartshop.service;

import com.example.smartshop.entity.Order;
import com.example.smartshop.entity.OrderItem;
import com.example.smartshop.entity.Client;
import com.example.smartshop.entity.Product;
import com.example.smartshop.entity.CustomerTier;
import com.example.smartshop.dto.OrderRequestDTO;
import com.example.smartshop.dto.OrderResponseDTO;
import com.example.smartshop.dto.OrderItemRequestDTO;
import com.example.smartshop.dto.OrderHistoryDTO;
import com.example.smartshop.repository.OrderRepository;
import com.example.smartshop.repository.ClientRepository;
import com.example.smartshop.repository.ProductRepository;
import com.example.smartshop.mapper.OrderMapper;
import com.example.smartshop.exception.BusinessRuleViolationException;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository, ClientRepository clientRepository,
            ProductRepository productRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
        this.orderMapper = orderMapper;
    }

    public OrderResponseDTO create(OrderRequestDTO dto) {
        // Validation du client
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new BusinessRuleViolationException("Client not found"));

        // Validation des produits
        List<Long> productIds = dto.getItems().stream()
                .map(OrderItemRequestDTO::getProductId)
                .collect(Collectors.toList());

        List<Product> products = productRepository.findAllById(productIds);
        if (products.size() != productIds.size()) {
            throw new BusinessRuleViolationException("One or more products not found");
        }

        // Création de la commande
        Order order = orderMapper.toEntity(dto, client);
        order = orderRepository.save(order);

        // Création des items
        List<OrderItem> orderItems = new java.util.ArrayList<>();
        for (OrderItemRequestDTO itemDTO : dto.getItems()) {
            Product product = products.stream()
                    .filter(p -> p.getId().equals(itemDTO.getProductId()))
                    .findFirst()
                    .orElseThrow();

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemDTO.getQuantity())
                    .unitPrice(itemDTO.getUnitPrice())
                    .lineTotal(itemDTO.getQuantity() * itemDTO.getUnitPrice())
                    .build();
            orderItems.add(item);
        }

        order.setItems(orderItems);

        // Calcul du subtotal
        Double subTotal = orderItems.stream()
                .mapToDouble(OrderItem::getLineTotal)
                .sum();
        order.setSubtotal(subTotal);

        // ===== LOGIQUE DE REMISE =====
        Double totalDiscountPercentage = 0.0;

        // Remise basée sur le tier
        if (client.getTier() == CustomerTier.PLATINUM && subTotal >= 1200) {
            totalDiscountPercentage += 15.0;
        } else if (client.getTier() == CustomerTier.GOLD && subTotal >= 800) {
            totalDiscountPercentage += 10.0;
        } else if (client.getTier() == CustomerTier.SILVER && subTotal >= 500) {
            totalDiscountPercentage += 5.0;
        }

        // Code promo : +5% supplémentaire
        if (dto.getPromoCode() != null && !dto.getPromoCode().isEmpty()) {
            totalDiscountPercentage += 5.0;
        }

        // Calcul de la remise
        Double discountAmount = subTotal * (totalDiscountPercentage / 100);
        order.setDiscountAmount(discountAmount);

        // Calcul du montant soumis à taxe
        Double amountSubjectToTax = subTotal - discountAmount;

        // Calcul de la taxe (20%)
        Double taxValue = amountSubjectToTax * (order.getTax() / 100);

        // Calcul du total
        Double total = amountSubjectToTax + taxValue;
        order.setTotal(total);
        order.setRemainingAmount(total);

        // Sauvegarde
        order = orderRepository.save(order);
        return orderMapper.toResponseDTO(order);
    }

    public OrderResponseDTO findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleViolationException("Order not found"));
        return orderMapper.toResponseDTO(order);
    }

    public List<OrderResponseDTO> findAllByClientId(Long clientId) {
        List<Order> orders = orderRepository.findByClientIdOrderByCreatedAtDesc(clientId);
        return orders.stream()
                .map(orderMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<OrderHistoryDTO> getClientOrderHistory(Long clientId) {
        List<Order> orders = orderRepository.findByClientIdOrderByCreatedAtDesc(clientId);

        return orders.stream()
                .map(order -> OrderHistoryDTO.builder()
                        .id(order.getId())
                        .date(order.getDate())
                        .totalAmount(order.getTotal())
                        .status(order.getStatus() != null ? order.getStatus().name() : "PENDING")
                        .build())
                .collect(Collectors.toList());
    }

    public OrderResponseDTO update(Long id, Order orderDetails) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleViolationException("Order not found"));

        if (orderDetails.getStatus() != null) {
            order.setStatus(orderDetails.getStatus());
        }

        order = orderRepository.save(order);
        return orderMapper.toResponseDTO(order);
    }

    public void delete(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleViolationException("Order not found"));
        orderRepository.delete(order);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleViolationException("Order not found"));
    }
}
