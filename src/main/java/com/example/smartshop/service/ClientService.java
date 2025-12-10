package com.example.smartshop.service;

import com.example.smartshop.entity.Client;
import com.example.smartshop.entity.User;
import com.example.smartshop.entity.UserRole;
import com.example.smartshop.entity.Order;
import com.example.smartshop.entity.CustomerTier;
import com.example.smartshop.dto.ClientDTO;
import com.example.smartshop.dto.ClientResponseDTO;
import com.example.smartshop.dto.CreateClientDTO;
import com.example.smartshop.repository.ClientRepository;
import com.example.smartshop.repository.OrderRepository;
import com.example.smartshop.repository.UserRepository;
import com.example.smartshop.mapper.ClientMapper;
import com.example.smartshop.exception.BusinessRuleViolationException;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

  private final ClientRepository clientRepository;
  private final OrderRepository orderRepository;
  private final UserRepository userRepository;
  private final ClientMapper clientMapper;

  public ClientService(ClientRepository clientRepository, OrderRepository orderRepository,
      UserRepository userRepository, ClientMapper clientMapper) {
    this.clientRepository = clientRepository;
    this.orderRepository = orderRepository;
    this.userRepository = userRepository;
    this.clientMapper = clientMapper;
  }

  public List<ClientDTO> findAll() {
    return clientRepository.findAll().stream()
        .map(clientMapper::toDTO)
        .collect(Collectors.toList());
  }

  public ClientDTO findById(Long id) {
    Client client = clientRepository.findById(id)
        .orElseThrow(() -> new BusinessRuleViolationException("Client not found"));
    return clientMapper.toDTO(client);
  }

  public ClientResponseDTO findByIdWithStats(Long id) {
    Client client = clientRepository.findById(id)
        .orElseThrow(() -> new BusinessRuleViolationException("Client not found"));

    ClientResponseDTO response = clientMapper.toResponseDTO(client);

    // ===== CALCUL À LA LECTURE =====
    List<Order> orders = orderRepository.findByClientId(id);

    if (orders.isEmpty()) {
      response.setTotalOrders(0);
      response.setTotalSpent(0.0);
      return response;
    }

    // totalOrders = nombre total de commandes
    response.setTotalOrders(orders.size());

    // totalSpent = somme de tous les totals
    Double totalSpent = orders.stream()
        .mapToDouble(Order::getTotal)
        .sum();
    response.setTotalSpent(roundToTwoDecimals(totalSpent));

    return response;
  }

  /**
   * Calcule et met à jour automatiquement le tier du client basé sur :
   * - BASIC : Client par défaut (0 commandes)
   * - SILVER : À partir de 3 commandes OU 1 000 DH cumulés
   * - GOLD : À partir de 10 commandes OU 5 000 DH cumulés
   * - PLATINUM : À partir de 20 commandes OU 15 000 DH cumulés
   *
   * Utilise les statistiques persistées du client (totalOrders, totalSpent)
   * au lieu de recalculer depuis la base de données.
   *
   * @param client le client à mettre à jour
   */
  public void calculateAndUpdateTier(Client client) {
    // Utiliser les statistiques persistées
    int totalOrders = client.getTotalOrders();
    Double totalSpent = client.getTotalSpent();

    if (totalSpent == null) {
      totalSpent = 0.0;
    }
    totalSpent = roundToTwoDecimals(totalSpent);

    // Déterminer le nouveau tier
    CustomerTier newTier = calculateTier(totalOrders, totalSpent);
    client.setTier(newTier);

    // Persister la mise à jour
    clientRepository.save(client);
  }

  /**
   * Détermine le tier client basé sur le nombre de commandes et le montant total
   * dépensé.
   *
   * @param totalOrders nombre de commandes complétées
   * @param totalSpent  montant total dépensé en DH
   * @return le tier client déterminé
   */
  private CustomerTier calculateTier(int totalOrders, Double totalSpent) {
    // PLATINUM : À partir de 20 commandes OU 15 000 DH cumulés
    if (totalOrders >= 20 || totalSpent >= 15000.0) {
      return CustomerTier.PLATINUM;
    }

    // GOLD : À partir de 10 commandes OU 5 000 DH cumulés
    if (totalOrders >= 10 || totalSpent >= 5000.0) {
      return CustomerTier.GOLD;
    }

    // SILVER : À partir de 3 commandes OU 1 000 DH cumulés
    if (totalOrders >= 3 || totalSpent >= 1000.0) {
      return CustomerTier.SILVER;
    }

    // Par défaut BASIC
    return CustomerTier.BASIC;
  }

  private Double roundToTwoDecimals(Double value) {
    if (value == null)
      return 0.0;
    return new BigDecimal(value)
        .setScale(2, RoundingMode.HALF_UP)
        .doubleValue();
  }

  public ClientDTO create(CreateClientDTO dto) {
    boolean emailExists = clientRepository.findByEmail(dto.getEmail()).isPresent();
    if (emailExists) {
      throw new BusinessRuleViolationException("A client with this email already exists");
    }

    // Créer un User automatiquement pour le client
    String username = dto.getEmail().split("@")[0]; // Utiliser la partie avant @ comme username
    User user = User.builder()
        .username(username)
        .password(Base64.getEncoder().encodeToString("client123".getBytes())) // Mot de passe par défaut encodé
        .role(UserRole.CLIENT)
        .build();
    User savedUser = userRepository.save(user);

    // Créer le client avec le User associé
    Client client = clientMapper.toEntity(dto);
    client.setUser(savedUser);
    Client savedClient = clientRepository.save(client);
    return clientMapper.toDTO(savedClient);
  }

  public ClientDTO update(Long id, CreateClientDTO dto) {
    Client client = clientRepository.findById(id)
        .orElseThrow(() -> new BusinessRuleViolationException("Client not found"));

    boolean emailExists = clientRepository.findByEmail(dto.getEmail())
        .filter(c -> !c.getId().equals(id))
        .isPresent();
    if (emailExists) {
      throw new BusinessRuleViolationException("A client with this email already exists");
    }

    client.setName(dto.getName());
    client.setEmail(dto.getEmail());
    Client updatedClient = clientRepository.save(client);
    return clientMapper.toDTO(updatedClient);
  }

  public void delete(Long id) {
    Client client = clientRepository.findById(id)
        .orElseThrow(() -> new BusinessRuleViolationException("Client not found"));
    clientRepository.delete(client);
  }

  public void associateUserToClient(Client client, User user) {
    clientMapper.updateEntityWithUser(client, user);
    clientRepository.save(client);
  }

  public Client getClientByEmail(String email) {
    return clientRepository.findByEmail(email)
        .orElseThrow(() -> new BusinessRuleViolationException("Client not found"));
  }

  public Client getClientById(Long id) {
    return clientRepository.findById(id)
        .orElseThrow(() -> new BusinessRuleViolationException("Client not found"));
  }


}
