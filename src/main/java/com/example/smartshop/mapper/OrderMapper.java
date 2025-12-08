package com.example.smartshop.mapper;

import com.example.smartshop.entity.Order;
import com.example.smartshop.entity.OrderItem;
import com.example.smartshop.entity.Client;
import com.example.smartshop.dto.OrderRequestDTO;
import com.example.smartshop.dto.OrderResponseDTO;
import com.example.smartshop.dto.OrderItemResponseDTO;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

  public Order toEntity(OrderRequestDTO dto, Client client) {
    if (dto == null || client == null) {
      return null;
    }

    return Order.builder()
        .client(client)
        .date(LocalDate.now())
        .status(com.example.smartshop.entity.OrderStatus.PENDING)
        .promoCode(dto.getPromoCode())
        .subtotal(0.0)
        .discountAmount(0.0)
        .tax(20.0)
        .total(0.0)
        .remainingAmount(0.0)
        .items(new ArrayList<>())
        .build();
  }

  public OrderResponseDTO toResponseDTO(Order order) {
    if (order == null) {
      return null;
    }

    List<OrderItemResponseDTO> itemDTOs = order.getItems() != null
        ? order.getItems().stream()
            .map(this::toItemResponseDTO)
            .collect(Collectors.toList())
        : new ArrayList<>();

    return OrderResponseDTO.builder()
        .id(order.getId())
        .clientId(order.getClient() != null ? order.getClient().getId() : null)
        .date(order.getDate())
        .items(itemDTOs)
        .status(order.getStatus() != null ? order.getStatus().name() : "PENDING")
        .subtotal(order.getSubtotal())
        .discountAmount(order.getDiscountAmount())
        .tax(order.getTax())
        .total(order.getTotal())
        .promoCode(order.getPromoCode())
        .remainingAmount(order.getRemainingAmount())
        .build();
  }

  private OrderItemResponseDTO toItemResponseDTO(OrderItem item) {
    if (item == null) {
      return null;
    }

    return OrderItemResponseDTO.builder()
        .id(item.getId())
        .productId(item.getProduct() != null ? item.getProduct().getId() : null)
        .quantity(item.getQuantity())
        .unitPrice(item.getUnitPrice())
        .lineTotal(item.getLineTotal())
        .build();
  }
}
