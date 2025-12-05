package com.example.smartshop.controller;

import com.example.smartshop.dto.PaymentRequestDTO;
import com.example.smartshop.dto.PaymentResponseDTO;
import com.example.smartshop.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addPayment(@Valid @RequestBody PaymentRequestDTO dto) {
        PaymentResponseDTO payment = paymentService.addPayment(dto);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Payment recorded successfully");
        response.put("payment", payment);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPayment(@PathVariable Long id) {
        PaymentResponseDTO payment = paymentService.findById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Payment retrieved successfully");
        response.put("payment", payment);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderPayments(@PathVariable Long orderId) {
        List<PaymentResponseDTO> payments = paymentService.findByOrderId(orderId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Payments retrieved successfully");
        response.put("payments", payments);
        response.put("total", payments.size());
        return ResponseEntity.ok(response);
    }
}
