package com.example.smartshop.service;

import com.example.smartshop.entity.Client;
import com.example.smartshop.entity.User;
import com.example.smartshop.entity.Order;
import com.example.smartshop.dto.ClientDTO;
import com.example.smartshop.dto.ClientResponseDTO;
import com.example.smartshop.dto.CreateClientDTO;
import com.example.smartshop.repository.ClientRepository;
import com.example.smartshop.repository.OrderRepository;
import com.example.smartshop.mapper.ClientMapper;
import com.example.smartshop.exception.BusinessRuleViolationException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

  private final ClientRepository clientRepository;
  private final OrderRepository orderRepository;
  private final ClientMapper clientMapper;

  public ClientService(ClientRepository clientRepository, OrderRepository orderRepository, ClientMapper clientMapper) {
    this.clientRepository = clientRepository;
    this.orderRepository = orderRepository;
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
    response.setTotalSpent(totalSpent);

    return response;
  }

  public ClientDTO create(CreateClientDTO dto) {
    boolean emailExists = clientRepository.findByEmail(dto.getEmail()).isPresent();
    if (emailExists) {
      throw new BusinessRuleViolationException("A client with this email already exists");
    }

    Client client = clientMapper.toEntity(dto);
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
