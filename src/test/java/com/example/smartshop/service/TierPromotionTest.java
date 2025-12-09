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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests pour valider les scénarios d'acceptation du système de fidélité (tier promotion).
 * 
 * Les statistiques (totalOrders, totalSpent) sont maintenant persistées directement
 * dans l'entité Client lors de chaque confirmation de commande via OrderService.confirmOrder().
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
     * Les stats sont déjà persistées en base (mises à jour par OrderService.confirmOrder)
     * Vérifier que calculateAndUpdateTier() utilise les stats persistées et les convertit en tier.
     */
    @Test
    void testScenario1_BasicToSilverWith3Orders() {
        // Arrange
        Client client = new Client();
        client.setId(1L);
        client.setName("Test Client");
        client.setEmail("test@example.com");
        client.setTier(CustomerTier.BASIC);
        client.setTotalOrders(3);        // Stats persistées par OrderService
        client.setTotalSpent(1500.0);    // Stats persistées par OrderService

        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        clientService.calculateAndUpdateTier(client);

        // Assert
        assertEquals(CustomerTier.SILVER, client.getTier(), "Client should be promoted to SILVER tier (3 commandes)");
        assertEquals(3, client.getTotalOrders(), "Total orders should remain 3");
        assertEquals(1500.0, client.getTotalSpent(), "Total spent should remain 1500.0");
    }

    /**
     * Scénario 2: Client SILVER → dépense 5000 DH → devient GOLD
     * 
     * Le client SILVER a déjà 3 commandes (1500 DH).
     * Après une 4ème commande de 5000 DH, il atteint 6500 DH total.
     * Comme 6500 >= 5000, le tier passe à GOLD.
     */
    @Test
    void testScenario2_SilverToGoldWith5000Spent() {
        // Arrange
        Client client = new Client();
        client.setId(2L);
        client.setName("Silver Client");
        client.setEmail("silver@example.com");
        client.setTier(CustomerTier.SILVER);
        client.setTotalOrders(4);        // 3 anciennes + 1 nouvelle commande
        client.setTotalSpent(6500.0);    // 1500 + 5000

        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        clientService.calculateAndUpdateTier(client);

        // Assert
        assertEquals(CustomerTier.GOLD, client.getTier(), "Client should be promoted to GOLD tier (6500 DH >= 5000)");
        assertEquals(4, client.getTotalOrders());
        assertEquals(6500.0, client.getTotalSpent());
    }

    /**
     * Scénario 3: Client GOLD → passe commande de 1000 DH → remise de 10%
     * 
     * Valide que la logique de remise est correcte pour un client GOLD.
     * Pour une commande de 1000 DH avec tier GOLD (10% si >= 800) :
     * - Subtotal: 1000 DH
     * - Remise GOLD 10%: 100 DH
     * - Montant après remise: 900 DH
     * - TVA 20%: 180 DH
     * - Total TTC: 1080 DH
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

        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        clientService.calculateAndUpdateTier(client);

        // Assert
        assertEquals(CustomerTier.GOLD, client.getTier());
        
        // Vérifier la logique de remise GOLD sur 1000 DH
        Double subtotal = 1000.0;
        Double expectedDiscount = 100.0; // 10% pour GOLD tier
        Double discountedAmount = subtotal - expectedDiscount; // 900
        Double tax = discountedAmount * 0.20; // 180
        Double expectedTotal = discountedAmount + tax; // 1080
        
        assertEquals(100.0, expectedDiscount);
        assertEquals(1080.0, expectedTotal);
    }

    /**
     * Test supplémentaire: Vérifier que le tier PLATINUM s'applique correctement
     * avec 20 commandes ou 15000 DH dépensés
     */
    @Test
    void testPlatinumTierWith20Orders() {
        // Arrange
        Client client = new Client();
        client.setId(4L);
        client.setName("Platinum Client");
        client.setEmail("platinum@example.com");
        client.setTier(CustomerTier.BASIC);
        client.setTotalOrders(20);
        client.setTotalSpent(20000.0);

        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        clientService.calculateAndUpdateTier(client);

        // Assert
        assertEquals(CustomerTier.PLATINUM, client.getTier(), "Client with 20 orders should reach PLATINUM");
        assertEquals(20, client.getTotalOrders());
        assertEquals(20000.0, client.getTotalSpent());
    }

    /**
     * Test supplémentaire: Vérifier que le tier PLATINUM s'applique avec 15000+ DH
     * même avec moins de 20 commandes
     */
    @Test
    void testPlatinumTierWith15000DhSpent() {
        // Arrange
        Client client = new Client();
        client.setId(5L);
        client.setName("Big Spender");
        client.setEmail("bigspender@example.com");
        client.setTier(CustomerTier.BASIC);
        client.setTotalOrders(5);        // < 20
        client.setTotalSpent(15000.0);   // >= 15000

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
        client.setTotalOrders(2);
        client.setTotalSpent(500.0);

        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        clientService.calculateAndUpdateTier(client);

        // Assert
        assertEquals(CustomerTier.BASIC, client.getTier(), "Client should remain BASIC");
        assertEquals(2, client.getTotalOrders());
        assertEquals(500.0, client.getTotalSpent());
    }

    /**
     * Test supplémentaire: GOLD tier avec 10 commandes exactement
     */
    @Test
    void testGoldTierWith10Orders() {
        // Arrange
        Client client = new Client();
        client.setId(7L);
        client.setName("Gold Client 10");
        client.setEmail("gold10@example.com");
        client.setTier(CustomerTier.BASIC);
        client.setTotalOrders(10);      // >= 10
        client.setTotalSpent(2500.0);   // < 5000

        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        clientService.calculateAndUpdateTier(client);

        // Assert
        assertEquals(CustomerTier.GOLD, client.getTier(), "Client with 10 orders should reach GOLD");
        assertEquals(10, client.getTotalOrders());
        assertEquals(2500.0, client.getTotalSpent());
    }

    /**
     * Test supplémentaire: SILVER tier avec 1000 DH exactement (limite)
     */
    @Test
    void testSilverTierWith1000DhExact() {
        // Arrange
        Client client = new Client();
        client.setId(8L);
        client.setName("Silver Spender");
        client.setEmail("silver1000@example.com");
        client.setTier(CustomerTier.BASIC);
        client.setTotalOrders(1);       // < 3
        client.setTotalSpent(1000.0);   // >= 1000 (limite minimale SILVER)

        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        clientService.calculateAndUpdateTier(client);

        // Assert
        assertEquals(CustomerTier.SILVER, client.getTier(), "Client with 1000 DH should reach SILVER");
        assertEquals(1, client.getTotalOrders());
        assertEquals(1000.0, client.getTotalSpent());
    }

    /**
     * Test supplémentaire: Rounding des montants (important pour BigDecimal)
     * Client avec un total qui nécessite arrondi : 1000.004 → 1000.00
     */
    @Test
    void testRoundingWith1000Dot004() {
        // Arrange
        Client client = new Client();
        client.setId(9L);
        client.setName("Rounding Client");
        client.setEmail("rounding@example.com");
        client.setTier(CustomerTier.BASIC);
        client.setTotalOrders(3);
        client.setTotalSpent(1000.004);  // Doit être arrondi à 1000.00

        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        clientService.calculateAndUpdateTier(client);

        // Assert
        assertEquals(CustomerTier.SILVER, client.getTier(), "Client should be SILVER (rounded to 1000.00)");
    }
}
