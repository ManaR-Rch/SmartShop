package com.example.smartshop.service;

import com.example.smartshop.entity.Order;
import com.example.smartshop.entity.Client;
import com.example.smartshop.dto.OrderHistoryDTO;
import com.example.smartshop.repository.OrderRepository;
import com.example.smartshop.exception.BusinessRuleViolationException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ClientService clientService;

    public OrderService(OrderRepository orderRepository, ClientService clientService) {
        this.orderRepository = orderRepository;
        this.clientService = clientService;
    }

    public Order create(Order order) {
        if (order == null || order.getClient() == null) {
            throw new BusinessRuleViolationException("La commande et le client associé sont obligatoires");
        }

        Order savedOrder = orderRepository.save(order);

        Client client = order.getClient();
        clientService.updateClientStats(client, order.getTotal());

        return savedOrder;
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleViolationException("Commande non trouvée avec l'ID: " + id));
    }

    public List<OrderHistoryDTO> getClientOrderHistory(Long clientId) {
        List<Order> orders = orderRepository.findByClientIdOrderByCreatedAtDesc(clientId);

        return orders.stream()
                .map(order -> OrderHistoryDTO.builder()
                        .id(order.getId())
                        .date(order.getCreatedAt())
                        .totalAmount(order.getTotal())
                        .status(order.getStatus() != null ? order.getStatus().name() : "PENDING")
                        .build())
                .collect(Collectors.toList());
    }

    public Order update(Long id, Order orderDetails) {
        Order order = findById(id);

        if (orderDetails.getStatus() != null) {
            order.setStatus(orderDetails.getStatus());
        }
        if (orderDetails.getSubtotal() != null) {
            order.setSubtotal(orderDetails.getSubtotal());
        }
        if (orderDetails.getDiscount() != null) {
            order.setDiscount(orderDetails.getDiscount());
        }
        if (orderDetails.getVat() != null) {
            order.setVat(orderDetails.getVat());
        }
        if (orderDetails.getTotal() != null) {
            order.setTotal(orderDetails.getTotal());
        }

        return orderRepository.save(order);
    }

    public void delete(Long id) {
        Order order = findById(id);
        orderRepository.delete(order);
    }
}
