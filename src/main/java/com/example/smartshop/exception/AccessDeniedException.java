package com.example.smartshop.exception;

/**
 * Exception levée quand l'accès à une ressource est refusé (403 Forbidden)
 * 
 * Exemples:
 * - Client essayant d'accéder à une commande d'un autre client
 * - Client essayant d'accéder aux endpoints ADMIN
 * - Utilisateur sans les rôles nécessaires pour une opération
 * - Tentative de modifier une ressource sans permission
 */
public class AccessDeniedException extends RuntimeException {

  public AccessDeniedException(String message) {
    super(message);
  }

  public AccessDeniedException(String message, Throwable cause) {
    super(message, cause);
  }
}
