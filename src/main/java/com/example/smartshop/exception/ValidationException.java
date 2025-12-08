package com.example.smartshop.exception;

/**
 * Exception levée quand une validation de données échoue (400 Bad Request)
 * 
 * Exemples:
 * - Paramètres manquants ou invalides
 * - Format de données incorrect
 * - Champs obligatoires vides
 * - Valeurs en dehors des limites autorisées
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
