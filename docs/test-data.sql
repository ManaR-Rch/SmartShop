-- ============================================================================
-- SmartShop - Script de Données de Test pour PostgreSQL
-- ============================================================================
-- Ce script initialise la base de données avec des données de test complètes
-- pour faciliter les tests dans Postman
-- ============================================================================

-- Nettoyer les données existantes (optionnel)
-- DELETE FROM order_items;
-- DELETE FROM payments;
-- DELETE FROM orders;
-- DELETE FROM clients;
-- DELETE FROM products;
-- DELETE FROM users;

-- ============================================================================
-- 1. CRÉER DES UTILISATEURS (Users)
-- ============================================================================

-- Réinitialiser la séquence des IDs
ALTER SEQUENCE users_id_seq RESTART WITH 1;

-- Admin
INSERT INTO users (username, password, role) VALUES
('admin', 'YWRtaW4xMjM=', 'ADMIN'); -- password: admin123 (Base64 encoded)

-- Clients
INSERT INTO users (username, password, role) VALUES
('john.doe', 'Y2xpZW50MTIz', 'CLIENT'),      -- password: client123
('jane.smith', 'Y2xpZW50MTIz', 'CLIENT'),
('bob.wilson', 'Y2xpZW50MTIz', 'CLIENT'),
('alice.johnson', 'Y2xpZW50MTIz', 'CLIENT'),
('charlie.brown', 'Y2xpZW50MTIz', 'CLIENT');

-- ============================================================================
-- 2. CRÉER DES PRODUITS (Products)
-- ============================================================================

ALTER SEQUENCE products_id_seq RESTART WITH 1;

INSERT INTO products (name, price, stock, deleted) VALUES
('Laptop ASUS VivoBook 15', 8500.00, 15, false),
('Mouse Logitech MX Master 3', 350.00, 50, false),
('Clavier Mécanique RGB Corsair', 1200.00, 25, false),
('Écran 4K 27" Dell UltraSharp', 3500.00, 10, false),
('Casque Audio Sony WH-1000XM5', 1800.00, 20, false),
('Webcam HD Logitech C920', 450.00, 30, false),
('Disque SSD 1TB Samsung 970 EVO', 900.00, 40, false),
('Moniteur 144Hz 24" ASUS ROG', 2200.00, 18, false),
('Souris sans fil Razer DeathAdder', 650.00, 35, false),
('Tapis souris XL Corsair MM1000', 180.00, 60, false),
('Hub USB-C 7 ports Anker', 380.00, 25, false),
('Adaptateur HDMI vers DisplayPort', 120.00, 45, false),
('Câble USB-C 3.1 2m Belkin', 85.00, 80, false),
('Support pour Laptop Aluminium', 250.00, 50, false),
('Station d''accueil USB-C 11 ports', 1500.00, 12, false);

-- ============================================================================
-- 3. CRÉER DES CLIENTS (Clients) avec différents niveaux de fidélité
-- ============================================================================

-- Client BASIC (John Doe) - 0 commandes
INSERT INTO clients (user_id, name, email, tier, total_orders, total_spent, first_order_date, last_order_date) VALUES
(2, 'John Doe', 'john.doe@example.com', 'BASIC', 0, 0.0, NULL, NULL);

-- Client SILVER (Jane Smith) - 3 commandes, ~1500 DH
INSERT INTO clients (user_id, name, email, tier, total_orders, total_spent, first_order_date, last_order_date) VALUES
(3, 'Jane Smith', 'jane.smith@example.com', 'SILVER', 3, 1750.50, '2025-11-10 10:30:00', '2025-12-05 14:45:00');

-- Client GOLD (Bob Wilson) - 10 commandes, ~6000 DH
INSERT INTO clients (user_id, name, email, tier, total_orders, total_spent, first_order_date, last_order_date) VALUES
(4, 'Bob Wilson', 'bob.wilson@example.com', 'GOLD', 10, 6250.75, '2025-08-15 09:15:00', '2025-12-08 16:20:00');

-- Client PLATINUM (Alice Johnson) - 25 commandes, ~22000 DH
INSERT INTO clients (user_id, name, email, tier, total_orders, total_spent, first_order_date, last_order_date) VALUES
(5, 'Alice Johnson', 'alice.johnson@example.com', 'PLATINUM', 25, 22500.00, '2025-01-20 11:00:00', '2025-12-09 13:30:00');

-- Client BASIC avec au moins une commande (Charlie Brown) - 1 commande
INSERT INTO clients (user_id, name, email, tier, total_orders, total_spent, first_order_date, last_order_date) VALUES
(6, 'Charlie Brown', 'charlie.brown@example.com', 'BASIC', 1, 800.00, '2025-12-01 15:45:00', '2025-12-01 15:45:00');

-- ============================================================================
-- 4. CRÉER DES COMMANDES (Orders)
-- ============================================================================

ALTER SEQUENCE orders_id_seq RESTART WITH 1;

-- Commandes PENDING pour John Doe (client 2)
INSERT INTO orders (client_id, date, created_at, status, subtotal, discount_amount, tax, total, remaining_amount, promo_code) VALUES
(2, '2025-12-10', '2025-12-10 10:00:00', 'PENDING', 1500.00, 0.0, 300.00, 1800.00, 1800.00, NULL);

-- Commandes CONFIRMED pour Jane Smith (client 3)
INSERT INTO orders (client_id, date, created_at, status, subtotal, discount_amount, tax, total, remaining_amount, promo_code) VALUES
(3, '2025-11-10', '2025-11-10 10:30:00', 'CONFIRMED', 1000.00, 100.00, 180.00, 1080.00, 0.0, NULL),
(3, '2025-11-25', '2025-11-25 14:15:00', 'CONFIRMED', 550.00, 0.0, 110.00, 660.00, 0.0, NULL),
(3, '2025-12-05', '2025-12-05 14:45:00', 'CONFIRMED', 500.00, 0.0, 100.00, 600.00, 0.0, 'PROMO-ABC1');

-- Commandes CONFIRMED pour Bob Wilson (client 4)
INSERT INTO orders (client_id, date, created_at, status, subtotal, discount_amount, tax, total, remaining_amount, promo_code) VALUES
(4, '2025-10-05', '2025-10-05 09:00:00', 'CONFIRMED', 2000.00, 200.00, 360.00, 2160.00, 0.0, NULL),
(4, '2025-10-20', '2025-10-20 11:30:00', 'CONFIRMED', 1500.00, 0.0, 300.00, 1800.00, 0.0, NULL),
(4, '2025-11-02', '2025-11-02 15:45:00', 'CONFIRMED', 1200.00, 120.00, 216.00, 1296.00, 0.0, NULL),
(4, '2025-11-18', '2025-11-18 13:20:00', 'CONFIRMED', 800.00, 0.0, 160.00, 960.00, 0.0, NULL),
(4, '2025-12-01', '2025-12-01 10:15:00', 'CONFIRMED', 600.00, 0.0, 120.00, 720.00, 0.0, NULL),
(4, '2025-12-08', '2025-12-08 16:20:00', 'PENDING', 1000.00, 0.0, 200.00, 1200.00, 1200.00, NULL);

-- Commandes CONFIRMED pour Alice Johnson (client 5)
INSERT INTO orders (client_id, date, created_at, status, subtotal, discount_amount, tax, total, remaining_amount, promo_code) VALUES
(5, '2025-01-20', '2025-01-20 11:00:00', 'CONFIRMED', 3000.00, 300.00, 540.00, 3240.00, 0.0, NULL),
(5, '2025-02-10', '2025-02-10 09:30:00', 'CONFIRMED', 2500.00, 250.00, 450.00, 2700.00, 0.0, NULL),
(5, '2025-03-15', '2025-03-15 14:45:00', 'CONFIRMED', 2000.00, 0.0, 400.00, 2400.00, 0.0, NULL),
(5, '2025-04-22', '2025-04-22 10:20:00', 'CONFIRMED', 1800.00, 180.00, 324.00, 1944.00, 0.0, NULL),
(5, '2025-05-30', '2025-05-30 15:10:00', 'CONFIRMED', 2200.00, 0.0, 440.00, 2640.00, 0.0, NULL),
(5, '2025-06-15', '2025-06-15 12:00:00', 'CONFIRMED', 1500.00, 0.0, 300.00, 1800.00, 0.0, NULL),
(5, '2025-07-08', '2025-07-08 11:45:00', 'CONFIRMED', 2100.00, 210.00, 378.00, 2268.00, 0.0, NULL),
(5, '2025-08-12', '2025-08-12 13:30:00', 'CONFIRMED', 1600.00, 0.0, 320.00, 1920.00, 0.0, NULL),
(5, '2025-09-25', '2025-09-25 10:15:00', 'CONFIRMED', 1900.00, 190.00, 342.00, 2052.00, 0.0, NULL),
(5, '2025-10-30', '2025-10-30 14:40:00', 'CONFIRMED', 2300.00, 0.0, 460.00, 2760.00, 0.0, NULL),
(5, '2025-11-14', '2025-11-14 09:20:00', 'CONFIRMED', 1700.00, 0.0, 340.00, 2040.00, 0.0, NULL),
(5, '2025-12-09', '2025-12-09 13:30:00', 'CONFIRMED', 2800.00, 280.00, 504.00, 3024.00, 0.0, NULL),
(5, '2025-01-25', '2025-01-25 10:30:00', 'CONFIRMED', 1400.00, 0.0, 280.00, 1680.00, 0.0, NULL),
(5, '2025-02-28', '2025-02-28 14:15:00', 'CONFIRMED', 1600.00, 160.00, 288.00, 1728.00, 0.0, NULL),
(5, '2025-03-20', '2025-03-20 11:45:00', 'CONFIRMED', 2100.00, 0.0, 420.00, 2520.00, 0.0, NULL),
(5, '2025-04-05', '2025-04-05 09:30:00', 'CONFIRMED', 1800.00, 0.0, 360.00, 2160.00, 0.0, NULL),
(5, '2025-05-12', '2025-05-12 13:20:00', 'CONFIRMED', 2200.00, 220.00, 396.00, 2376.00, 0.0, NULL),
(5, '2025-06-28', '2025-06-28 15:45:00', 'CONFIRMED', 1500.00, 0.0, 300.00, 1800.00, 0.0, NULL),
(5, '2025-07-22', '2025-07-22 10:15:00', 'CONFIRMED', 1900.00, 0.0, 380.00, 2280.00, 0.0, NULL),
(5, '2025-08-30', '2025-08-30 12:30:00', 'CONFIRMED', 2300.00, 230.00, 414.00, 2544.00, 0.0, NULL),
(5, '2025-09-15', '2025-09-15 14:00:00', 'CONFIRMED', 1600.00, 0.0, 320.00, 1920.00, 0.0, NULL),
(5, '2025-10-10', '2025-10-10 11:20:00', 'CONFIRMED', 2000.00, 0.0, 400.00, 2400.00, 0.0, NULL);

-- Commande CONFIRMED pour Charlie Brown (client 6)
INSERT INTO orders (client_id, date, created_at, status, subtotal, discount_amount, tax, total, remaining_amount, promo_code) VALUES
(6, '2025-12-01', '2025-12-01 15:45:00', 'CONFIRMED', 500.00, 0.0, 100.00, 600.00, 0.0, NULL);

-- ============================================================================
-- 5. CRÉER DES ARTICLES DE COMMANDE (Order Items)
-- ============================================================================

ALTER SEQUENCE order_items_id_seq RESTART WITH 1;

-- Order 1 (John Doe - PENDING): Laptop + Mouse
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(1, 1, 1, 8500.00, 8500.00),
(1, 2, 1, 350.00, 350.00);

-- Order 2 (Jane Smith): Clavier + Écran
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(2, 3, 1, 1200.00, 1200.00);

-- Order 3 (Jane Smith): Casque + Webcam
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(3, 5, 1, 1800.00, 1800.00);

-- Order 4 (Jane Smith): SSD
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(4, 7, 1, 900.00, 900.00);

-- Order 5 (Bob Wilson): Écran + Casque
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(5, 4, 1, 3500.00, 3500.00);

-- Order 6 (Bob Wilson): Moniteur gaming
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(6, 8, 1, 2200.00, 2200.00);

-- Order 7 (Bob Wilson): Souris gaming
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(7, 9, 2, 650.00, 1300.00);

-- Order 8 (Bob Wilson): Tapis souris
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(8, 10, 2, 180.00, 360.00);

-- Order 9 (Bob Wilson): Hub USB + Câble
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(9, 11, 1, 380.00, 380.00),
(9, 13, 3, 85.00, 255.00);

-- Order 10 (Bob Wilson - PENDING): Support Laptop
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(10, 14, 2, 250.00, 500.00);

-- Orders pour Alice Johnson (client 5)
-- Order 11 (Alice): Laptop + Mouse + Clavier
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(11, 1, 1, 8500.00, 8500.00),
(11, 2, 1, 350.00, 350.00),
(11, 3, 1, 1200.00, 1200.00);

-- Order 12 (Alice): Écran + Casque
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(12, 4, 1, 3500.00, 3500.00),
(12, 5, 1, 1800.00, 1800.00);

-- Order 13 (Alice): Webcam + Hub
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(13, 6, 2, 450.00, 900.00),
(13, 11, 1, 380.00, 380.00);

-- Order 14 (Alice): SSD + Moniteur
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(14, 7, 2, 900.00, 1800.00),
(14, 8, 1, 2200.00, 2200.00);

-- Order 15 (Alice): Souris + Tapis
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(15, 9, 1, 650.00, 650.00),
(15, 10, 3, 180.00, 540.00);

-- Order 16 (Alice): Adaptateurs + Câbles
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(16, 12, 2, 120.00, 240.00),
(16, 13, 5, 85.00, 425.00);

-- Order 17 (Alice)
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(17, 1, 1, 8500.00, 8500.00);

-- Order 18 (Alice)
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(18, 4, 1, 3500.00, 3500.00);

-- Order 19 (Alice)
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(19, 8, 1, 2200.00, 2200.00);

-- Order 20 (Alice)
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(20, 11, 2, 380.00, 760.00);

-- Order 21 (Alice)
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(21, 3, 1, 1200.00, 1200.00);

-- Order 22 (Alice)
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(22, 5, 1, 1800.00, 1800.00);

-- Order 23 (Alice)
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(23, 7, 1, 900.00, 900.00);

-- Order 24 (Alice)
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(24, 2, 1, 350.00, 350.00);

-- Order 25 (Alice)
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(25, 9, 1, 650.00, 650.00);

-- Order 26 (Charlie Brown)
INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(26, 6, 1, 450.00, 450.00);

-- ============================================================================
-- STATISTIQUES FINALES
-- ============================================================================
-- Total users: 6 (1 admin + 5 clients)
-- Total products: 15
-- Total clients: 5 (avec différents tiers: BASIC, SILVER, GOLD, PLATINUM)
-- Total orders: 25
-- Total order items: 50+
-- ============================================================================

-- Afficher les statistiques
SELECT 'Users' as entity, COUNT(*) as count FROM users
UNION ALL
SELECT 'Products', COUNT(*) FROM products
UNION ALL
SELECT 'Clients', COUNT(*) FROM clients
UNION ALL
SELECT 'Orders', COUNT(*) FROM orders
UNION ALL
SELECT 'Order Items', COUNT(*) FROM order_items;

-- Vérifier les clients par tier
SELECT tier, COUNT(*) as count, SUM(total_orders) as total_orders, ROUND(SUM(total_spent)::numeric, 2) as total_spent 
FROM clients 
GROUP BY tier 
ORDER BY tier;
