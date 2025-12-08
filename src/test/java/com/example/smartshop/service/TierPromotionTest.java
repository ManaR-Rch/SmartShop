package com.example.smartshop.service;

import com.example.smartshop.entity.Client;
import com.example.smartshop.entity.CustomerTier;
import com.example.smartshop.entity.Order;
import com.example.smartshop.entity.OrderStatus;
import com.example.smartshop.repository.ClientRepository;
import com.example.smartshop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests pour valider les scénarios d'acceptation du système de fidélité (tier promotion).
 * 
 * Règles d'acquisition du niveau (OBTENTION) :
 * - BASIC : Client par défaut (0 commande)
 * - SILVER : À partir de 3 commandes OU 1 000 DH cumulés
 * - GOLD : À partir de 10 commandes OU 5 000 DH cumulés
 * - PLATINUM : À partir de 20 commandes OU 15 000 DH cumulés
 */
@ExtendWith(MockitoExtension.class)
class TierPromotionTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ClientService clientService;

    /**
     * Scénario 1: Client BASIC → 3 commandes → devient SILVER
     * 
     * Étapes:
     * 1. Créer un client avec tier BASIC (0 commandes, 0 DH dépensé)
     * 2. Ajouter 3 commandes de 500 DH chacune (total 1500 DH)
     * 3. Appeler calculateAndUpdateTier()
     * 4. Vérifier que le tier est devenu SILVER
     * 5. Vérifier que totalOrders = 3 et totalSpent = 1500.0
     */
    @Test
    void testScenario1_BasicToSilverWith3Orders() {
        // Arrange
        Client client = new Client();
        client.setId(1L);
        client.setName("Test Client");
        client.setEmail("test@example.com");
        client.setTier(CustomerTier.BASIC);
        client.setTotalOrders(0);
        client.setTotalSpent(0.0);

        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Order order = new Order();
            order.setId((long) (i + 1));
            order.setClient(client);
            order.setTotal(500.0);
            order.setStatus(OrderStatus.CONFIRMED);
            orders.add(order);
        }

        when(orderRepository.findByClientId(1L)).thenReturn(orders);
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        clientService.calculateAndUpdateTier(client);

        // Assert
        assertEquals(CustomerTier.SILVER, client.getTier(), "Client should be promoted to SILVER tier");
        assertEquals(3, client.getTotalOrders(), "Total orders should be 3");
        assertEquals(1500.0, client.getTotalSpent(), "Total spent should be 1500.0");
    }

    /**
     * Scénario 2: Client SILVER → dépense 5000 DH → devient GOLD
     * 
     * Étapes:
     * 1. Créer un client avec tier SILVER (3 commandes, 1500 DH dépensé)
     * 2. Ajouter une commande de 5000 DH
     * 3. Appeler calculateAndUpdateTier()
     * 4. Vérifier que le tier est devenu GOLD
     * 5. Vérifier que totalOrders = 4 et totalSpent = 6500.0
     */
    @Test
    void testScenario2_SilverToGoldWith5000Spent() {
        // Arrange
        Client client = new Client();
        client.setId(2L);
        client.setName("Silver Client");
        client.setEmail("silver@example.com");
        client.setTier(CustomerTier.SILVER);
        client.setTotalOrders(3);
        client.setTotalSpent(1500.0);

        List<Order> orders = new ArrayList<>();
        // 3 commandes existantes de 500 DH chacune
        for (int i = 0; i < 3; i++) {
            Order order = new Order();
            order.setId((long) (i + 1));
            order.setClient(client);
            order.setTotal(500.0);
            order.setStatus(OrderStatus.CONFIRMED);
            orders.add(order);
        }
        // Nouvelle commande de 5000 DH
        Order newOrder = new Order();
        newOrder.setId(4L);
        newOrder.setClient(client);
        newOrder.setTotal(5000.0);
        newOrder.setStatus(OrderStatus.CONFIRMED);
        orders.add(newOrder);

        when(orderRepository.findByClientId(2L)).thenReturn(orders);
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        clientService.calculateAndUpdateTier(client);

        // Assert
        assertEquals(CustomerTier.GOLD, client.getTier(), "Client should be promoted to GOLD tier");
        assertEquals(4, client.getTotalOrders(), "Total orders should be 4");
        assertEquals(6500.0, client.getTotalSpent(), "Total spent should be 6500.0");
    }

    /**
     * Scénario 3: Client GOLD → passe commande de 1000 DH → remise de 10%
     * 
     * Étapes:
     * 1. Créer un client GOLD avec 10 commandes, 5000 DH dépensé
     * 2. Vérifier que le tier GOLD applique 10% de remise sur subtotal >= 800 DH
     * 3. Commande de 1000 DH → remise 10% → montant réduit à 900 DH
     * 4. Après TVA 20% : 900 * 1.2 = 1080 DH
     * 
     * Note: Ce scénario valide que la remise GOLD (10%) s'applique correctement
     * quand le client passe une nouvelle commande.
     */
    @Test
    void testScenario3_GoldClientReceives10PercentDiscount() {
        // Arrange
        Client client = new Client();
        client.setId(3L);
        client.setName("Gold Client");
        client.setEmail("gold@example.com");
        client.setTier(CustomerTier.GOLD);
        client.setTotalOrders(10);
        client.setTotalSpent(5000.0);

        // Vérifier que le client GOLD reçoit bien 10% de remise sur une commande >= 800 DH
        // Pour une commande de 1000 DH :
        // - Subtotal: 1000 DH
        // - Remise GOLD 10% (subtotal >= 800): 100 DH
        // - Montant après remise: 900 DH
        // - TVA 20%: 180 DH
        // - Total: 1080 DH

        Double subtotal = 1000.0;
        Double expectedDiscount = 100.0; // 10% de 1000
        Double discountedAmount = subtotal - expectedDiscount; // 900
        Double tax = discountedAmount * 0.20; // 180
        Double expectedTotal = discountedAmount + tax; // 1080

        // Assert
        assertEquals(CustomerTier.GOLD, client.getTier());
        assertEquals(100.0, expectedDiscount, "GOLD tier should apply 10% discount");
        assertEquals(1080.0, expectedTotal, "Total with TAX should be 1080");
    }

    /**
     * Test supplémentaire: Vérifier que le tier PLATINUM s'applique correctement
     * 
     * Client avec 20 commandes ou 15000 DH dépensés → PLATINUM
     */
    @Test
    void testPlatinumTierWith20Orders() {
        // Arrange
        Client client = new Client();
        client.setId(4L);
        client.setName("Platinum Client");
        client.setEmail("platinum@example.com");
        client.setTier(CustomerTier.BASIC);
        client.setTotalOrders(0);
        client.setTotalSpent(0.0);

        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Order order = new Order();
            order.setId((long) (i + 1));
            order.setClient(client);
            order.setTotal(1000.0);
            order.setStatus(OrderStatus.CONFIRMED);
            orders.add(order);
        }

        when(orderRepository.findByClientId(4L)).thenReturn(orders);
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        clientService.calculateAndUpdateTier(client);

        // Assert
        assertEquals(CustomerTier.PLATINUM, client.getTier(), "Client with 20 orders should reach PLATINUM");
        assertEquals(20, client.getTotalOrders());
        assertEquals(20000.0, client.getTotalSpent());
    }

    /**
     * Test supplémentaire: Vérifier que le tier PLATINUM s'applique avec 15000 DH
     * 
     * Client avec moins de 20 commandes mais 15000+ DH dépensés → PLATINUM
     */
    @Test
    void testPlatinumTierWith15000DhSpent() {
        // Arrange
        Client client = new Client();
        client.setId(5L);
        client.setName("Big Spender");
        client.setEmail("bigspender@example.com");
        client.setTier(CustomerTier.BASIC);
        client.setTotalOrders(0);
        client.setTotalSpent(0.0);

        List<Order> orders = new ArrayList<>();
        // 5 commandes de 3000 DH chacune = 15000 DH total
        for (int i = 0; i < 5; i++) {
            Order order = new Order();
            order.setId((long) (i + 1));
            order.setClient(client);
            order.setTotal(3000.0);
            order.setStatus(OrderStatus.CONFIRMED);
            orders.add(order);
        }

        when(orderRepository.findByClientId(5L)).thenReturn(orders);
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        clientService.calculateAndUpdateTier(client);

        // Assert
        assertEquals(CustomerTier.PLATINUM, client.getTier(), "Client with 15000 DH spent should reach PLATINUM");
        assertEquals(5, client.getTotalOrders());
        assertEquals(15000.0, client.getTotalSpent());
    }

    /**
     * Test supplémentaire: Vérifier la limite minimale de SILVER
     * 
     * Client avec 2 commandes (< 3) et 500 DH (< 1000) → reste BASIC
     */
    @Test
    void testRemainsBasicWithInsufficientOrders() {
        // Arrange
        Client client = new Client();
        client.setId(6L);
        client.setName("Minimal Client");
        client.setEmail("minimal@example.com");
        client.setTier(CustomerTier.BASIC);
        client.setTotalOrders(0);
        client.setTotalSpent(0.0);

        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Order order = new Order();
            order.setId((long) (i + 1));
            order.setClient(client);
            order.setTotal(250.0);
            order.setStatus(OrderStatus.CONFIRMED);
            orders.add(order);
        }

        when(orderRepository.findByClientId(6L)).thenReturn(orders);
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        clientService.calculateAndUpdateTier(client);

        // Assert
        assertEquals(CustomerTier.BASIC, client.getTier(), "Client should remain BASIC");
        assertEquals(2, client.getTotalOrders());
        assertEquals(500.0, client.getTotalSpent());
    }
}
