package com.example.smartshop.controller;

import com.example.smartshop.dto.OrderRequestDTO;
import com.example.smartshop.dto.OrderResponseDTO;
import com.example.smartshop.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrder(@Valid @RequestBody OrderRequestDTO dto) {
        OrderResponseDTO order = orderService.create(dto);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Order created successfully");
        response.put("order", order);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrder(@PathVariable Long id) {
        OrderResponseDTO order = orderService.findById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Order retrieved successfully");
        response.put("order", order);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<Map<String, Object>> getClientOrders(@PathVariable Long clientId) {
        List<OrderResponseDTO> orders = orderService.findAllByClientId(clientId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Orders retrieved successfully");
        response.put("orders", orders);
        response.put("total", orders.size());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirmOrder(@PathVariable Long id) {
        OrderResponseDTO order = orderService.confirmOrder(id);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Order confirmed successfully");
        response.put("order", order);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable Long id) {
        OrderResponseDTO order = orderService.cancelOrder(id);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Order canceled successfully");
        response.put("order", order);
        return ResponseEntity.ok(response);
    }
}
