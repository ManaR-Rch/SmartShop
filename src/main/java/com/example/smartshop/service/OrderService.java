package com.example.smartshop.service;

import com.example.smartshop.entity.Order;
import com.example.smartshop.entity.OrderItem;
import com.example.smartshop.entity.Client;
import com.example.smartshop.entity.Product;
import com.example.smartshop.entity.CustomerTier;
import com.example.smartshop.entity.OrderStatus;
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
import java.util.regex.Pattern;

@Service
public class OrderService {

  private final OrderRepository orderRepository;
  private final ClientRepository clientRepository;
  private final ProductRepository productRepository;
  private final ProductService productService;
  private final OrderMapper orderMapper;
  private static final Pattern PROMO_CODE_PATTERN = Pattern.compile("^PROMO-[A-Z0-9]{4}$");

  public OrderService(OrderRepository orderRepository, ClientRepository clientRepository,
      ProductRepository productRepository, ProductService productService, OrderMapper orderMapper) {
    this.orderRepository = orderRepository;
    this.clientRepository = clientRepository;
    this.productRepository = productRepository;
    this.productService = productService;
    this.orderMapper = orderMapper;
  }

  public OrderResponseDTO create(OrderRequestDTO dto) {
    // Validation du client
    if (dto.getClientId() == null) {
      throw new BusinessRuleViolationException("Client ID is required");
    }
    Client client = clientRepository.findById(dto.getClientId())
        .orElseThrow(() -> new BusinessRuleViolationException("Client not found"));

    // Validation des articles
    if (dto.getItems() == null || dto.getItems().isEmpty()) {
      throw new BusinessRuleViolationException("Order must contain at least one item");
    }

    // Validation des produits et du stock
    List<Long> productIds = dto.getItems().stream()
        .map(OrderItemRequestDTO::getProductId)
        .collect(Collectors.toList());

    List<Product> products = productRepository.findAllById(productIds);
    if (products.size() != productIds.size()) {
      throw new BusinessRuleViolationException("One or more products not found");
    }

    // Vérifier le stock pour tous les articles
    boolean insufficientStock = false;
    for (OrderItemRequestDTO itemDTO : dto.getItems()) {
      Product product = products.stream()
          .filter(p -> p.getId().equals(itemDTO.getProductId()))
          .findFirst()
          .orElseThrow();

      if (product.getStock() < itemDTO.getQuantity()) {
        insufficientStock = true;
        break;
      }
    }

    // Création de la commande
    Order order = orderMapper.toEntity(dto, client);

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

    // Si stock insuffisant, marquer comme REJECTED
    if (insufficientStock) {
      order.setStatus(OrderStatus.REJECTED);
      order.setSubtotal(0.0);
      order.setDiscountAmount(0.0);
      order.setTotal(0.0);
      order.setRemainingAmount(0.0);
      order = orderRepository.save(order);
      return orderMapper.toResponseDTO(order);
    }

    // Calcul du subtotal
    Double subTotal = orderItems.stream()
        .mapToDouble(OrderItem::getLineTotal)
        .sum();
    order.setSubtotal(roundToTwoDecimals(subTotal));

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

    // Code promo : +5% supplémentaire (validation format)
    if (dto.getPromoCode() != null && !dto.getPromoCode().isEmpty()) {
      if (!isValidPromoCode(dto.getPromoCode())) {
        throw new BusinessRuleViolationException("Invalid promo code format. Expected PROMO-XXXX");
      }
      totalDiscountPercentage += 5.0;
    }

    // Calcul de la remise
    Double discountAmount = roundToTwoDecimals(subTotal * (totalDiscountPercentage / 100));
    order.setDiscountAmount(discountAmount);

    // Calcul du montant soumis à taxe
    Double amountSubjectToTax = roundToTwoDecimals(subTotal - discountAmount);

    // Calcul de la taxe (20%)
    Double taxValue = roundToTwoDecimals(amountSubjectToTax * (order.getTax() / 100));

    // Calcul du total
    Double total = roundToTwoDecimals(amountSubjectToTax + taxValue);
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

  public OrderResponseDTO updateOrderStatus(Long id, OrderStatus newStatus) {
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new BusinessRuleViolationException("Order not found"));

    // Valider les transitions de statut
    validateStatusTransition(order.getStatus(), newStatus);

    order.setStatus(newStatus);
    order = orderRepository.save(order);
    return orderMapper.toResponseDTO(order);
  }

  public OrderResponseDTO confirmOrder(Long id) {
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new BusinessRuleViolationException("Order not found"));

    // Vérifier que la commande est entièrement payée
    if (order.getRemainingAmount() > 0.01) {
      throw new BusinessRuleViolationException(
          "Order is not fully paid. Remaining amount: " + order.getRemainingAmount());
    }

    // Vérifier le statut initial
    if (order.getStatus() != OrderStatus.PENDING) {
      throw new BusinessRuleViolationException("Only PENDING orders can be confirmed");
    }

    // Mettre à jour le statut
    order.setStatus(OrderStatus.CONFIRMED);

    // Décrémenter les stocks
    for (OrderItem item : order.getItems()) {
      productService.decrementStock(item.getProduct().getId(), item.getQuantity());
    }

    // Sauvegarder l'ordre
    order = orderRepository.save(order);

    return orderMapper.toResponseDTO(order);
  }

  public OrderResponseDTO cancelOrder(Long id) {
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new BusinessRuleViolationException("Order not found"));

    // Seules les commandes PENDING peuvent être annulées
    if (order.getStatus() != OrderStatus.PENDING) {
      throw new BusinessRuleViolationException("Only PENDING orders can be canceled");
    }

    order.setStatus(OrderStatus.CANCELED);
    order = orderRepository.save(order);
    return orderMapper.toResponseDTO(order);
  }

  private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
    if (currentStatus == null) {
      throw new BusinessRuleViolationException("Current status is null");
    }

    // Matrice de transitions valides
    boolean validTransition = false;

    if (currentStatus == OrderStatus.PENDING) {
      validTransition = (newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELED
          || newStatus == OrderStatus.REJECTED);
    } else if (currentStatus == OrderStatus.CONFIRMED || currentStatus == OrderStatus.CANCELED
        || currentStatus == OrderStatus.REJECTED) {
      validTransition = false; // États finaux
    }

    if (!validTransition) {
      throw new BusinessRuleViolationException("Invalid status transition from " + currentStatus + " to " + newStatus);
    }
  }

  private boolean isValidPromoCode(String promoCode) {
    return PROMO_CODE_PATTERN.matcher(promoCode).matches();
  }

  private Double roundToTwoDecimals(Double value) {
    if (value == null)
      return 0.0;
    return Math.round(value * 100.0) / 100.0;
  }

  public Order getOrderById(Long id) {
    return orderRepository.findById(id)
        .orElseThrow(() -> new BusinessRuleViolationException("Order not found"));
  }
}
