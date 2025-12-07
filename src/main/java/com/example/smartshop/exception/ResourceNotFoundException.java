package com.example.smartshop.exception;

/**
 * Exception levée quand une ressource n'est pas trouvée (404 Not Found)
 * 
 * Exemples:
 * - Client non trouvé
 * - Produit non trouvé
 * - Commande non trouvée
 * - Paiement non trouvé
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
