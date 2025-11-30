package com.example.smartshop.controller;

import com.example.smartshop.dto.OrderHistoryDTO;
import com.example.smartshop.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/client/orders")
public class OrderHistoryController {

    private final OrderService orderService;

    public OrderHistoryController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getOrderHistory(HttpSession session) {
        Long clientId = (Long) session.getAttribute("user");

        List<OrderHistoryDTO> history = orderService.getClientOrderHistory(clientId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Historique des commandes récupéré avec succès");
        response.put("orders", history);
        response.put("total", history.size());

        return ResponseEntity.ok(response);
    }
}
