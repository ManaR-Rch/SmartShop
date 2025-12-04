package com.example.smartshop.service;

import com.example.smartshop.entity.*;
import com.example.smartshop.dto.ClientResponseDTO;
import com.example.smartshop.repository.ClientRepository;
import com.example.smartshop.repository.OrderRepository;
import com.example.smartshop.mapper.ClientMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientService clientService;

    private Client client;
    private Order order1;
    private Order order2;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(1L)
                .name("Test Client")
                .email("test@test.com")
                .tier(CustomerTier.GOLD)
                .totalOrders(0)
                .totalSpent(0.0)
                .build();

        order1 = Order.builder()
                .id(1L)
                .client(client)
                .subtotal(1000.0)
                .discountAmount(100.0)
                .tax(20.0)
                .total(1080.0)
                .status(OrderStatus.CONFIRMED)
                .build();

        order2 = Order.builder()
                .id(2L)
                .client(client)
                .subtotal(500.0)
                .discountAmount(25.0)
                .tax(20.0)
                .total(570.0)
                .status(OrderStatus.CONFIRMED)
                .build();
    }

    @Test
    void testFindByIdWithStatsCalculatesTotalOrders() {
        // Test: findByIdWithStats recalcule totalOrders
        List<Order> orders = Arrays.asList(order1, order2);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(orderRepository.findByClientId(1L)).thenReturn(orders);
        when(clientMapper.toResponseDTO(client)).thenReturn(
                ClientResponseDTO.builder()
                        .id(1L)
                        .name("Test Client")
                        .email("test@test.com")
                        .tier(CustomerTier.GOLD)
                        .totalOrders(0)
                        .totalSpent(0.0)
                        .build()
        );

        ClientResponseDTO result = clientService.findByIdWithStats(1L);

        assertNotNull(result);
        // totalOrders doit être 2 (nombre de commandes)
        assertEquals(2, result.getTotalOrders());
    }

    @Test
    void testFindByIdWithStatsCalculatesTotalSpent() {
        // Test: findByIdWithStats recalcule totalSpent
        List<Order> orders = Arrays.asList(order1, order2);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(orderRepository.findByClientId(1L)).thenReturn(orders);
        when(clientMapper.toResponseDTO(client)).thenReturn(
                ClientResponseDTO.builder()
                        .id(1L)
                        .name("Test Client")
                        .email("test@test.com")
                        .tier(CustomerTier.GOLD)
                        .totalOrders(0)
                        .totalSpent(0.0)
                        .build()
        );

        ClientResponseDTO result = clientService.findByIdWithStats(1L);

        assertNotNull(result);
        // totalSpent doit être 1080 + 570 = 1650
        assertEquals(1650.0, result.getTotalSpent(), 0.01);
    }

    @Test
    void testFindByIdWithStatsNoOrdersReturnsZeros() {
        // Test: Client sans commandes → totalOrders = 0, totalSpent = 0
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(orderRepository.findByClientId(1L)).thenReturn(Arrays.asList());
        when(clientMapper.toResponseDTO(client)).thenReturn(
                ClientResponseDTO.builder()
                        .id(1L)
                        .name("Test Client")
                        .email("test@test.com")
                        .tier(CustomerTier.GOLD)
                        .totalOrders(0)
                        .totalSpent(0.0)
                        .build()
        );

        ClientResponseDTO result = clientService.findByIdWithStats(1L);

        assertNotNull(result);
        assertEquals(0, result.getTotalOrders());
        assertEquals(0.0, result.getTotalSpent());
    }

    @Test
    void testTierNeverChangesAutomatically() {
        // Test: Le tier ne change jamais automatiquement
        // Client reste GOLD même après accumulation
        List<Order> orders = Arrays.asList(order1, order2);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(orderRepository.findByClientId(1L)).thenReturn(orders);
        when(clientMapper.toResponseDTO(client)).thenReturn(
                ClientResponseDTO.builder()
                        .id(1L)
                        .name("Test Client")
                        .email("test@test.com")
                        .tier(CustomerTier.GOLD)  // Tier reste GOLD
                        .totalOrders(0)
                        .totalSpent(0.0)
                        .build()
        );

        ClientResponseDTO result = clientService.findByIdWithStats(1L);

        assertNotNull(result);
        // Vérifier que le tier reste GOLD (pas d'upgrade automatique)
        assertEquals(CustomerTier.GOLD, result.getTier());

        // Vérifier que clientRepository.save() n'a pas été appelé
        // (pas de modification du tier en base)
        verify(clientRepository, never()).save(any());
    }

    @Test
    void testClientNotFound() {
        // Test: Client non trouvé lance exception
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            clientService.findByIdWithStats(999L);
        });
    }

    @Test
    void testStatsNotStoredInClientEntity() {
        // Test: Les champs totalOrders/totalSpent dans l'entité Client restent inchangés
        List<Order> orders = Arrays.asList(order1, order2);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(orderRepository.findByClientId(1L)).thenReturn(orders);
        when(clientMapper.toResponseDTO(client)).thenReturn(
                ClientResponseDTO.builder()
                        .id(1L)
                        .name("Test Client")
                        .email("test@test.com")
                        .tier(CustomerTier.GOLD)
                        .totalOrders(0)  // Valeur par défaut du DTO
                        .totalSpent(0.0)  // Valeur par défaut du DTO
                        .build()
        );

        ClientResponseDTO result = clientService.findByIdWithStats(1L);

        assertNotNull(result);
        // Les stats sont recalculées dans la réponse
        assertEquals(2, result.getTotalOrders());
        assertEquals(1650.0, result.getTotalSpent(), 0.01);

        // Mais l'entité Client ne doit pas être modifiée/sauvegardée
        verify(clientRepository, never()).save(any());
    }
}
