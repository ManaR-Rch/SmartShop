package com.example.smartshop.exception;

/**
 * Exception levée quand l'authentification échoue (401 Unauthorized)
 * 
 * Exemples:
 * - Utilisateur non authentifié
 * - Session expirée
 * - Identifiants invalides
 * - Token expiré ou invalide
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
