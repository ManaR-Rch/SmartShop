package com.example.smartshop.exception;

import com.example.smartshop.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Gestionnaire centralisé d'exceptions pour toute l'application
 * 
 * Standardise les réponses d'erreur avec:
 * - Code HTTP approprié (400, 401, 403, 404, 422, 500)
 * - Timestamp ISO 8601
 * - Message d'erreur en français
 * - Chemin de la requête
 * - Logging sans exposition d'informations sensibles
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * Gère ResourceNotFoundException (404 Not Found)
   * Ressource demandée introuvable
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(
      ResourceNotFoundException ex,
      HttpServletRequest request) {

    ErrorResponseDTO error = ErrorResponseDTO.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.NOT_FOUND.value())
        .error("Not Found")
        .message(ex.getMessage())
        .path(request.getRequestURI())
        .build();

    log.error("Ressource non trouvée: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  /**
   * Gère BusinessRuleViolationException (422 Unprocessable Entity)
   * Violation des règles métier (stock insuffisant, état invalide, etc.)
   */
  @ExceptionHandler(BusinessRuleViolationException.class)
  public ResponseEntity<ErrorResponseDTO> handleBusinessRuleViolation(
      BusinessRuleViolationException ex,
      HttpServletRequest request) {

    ErrorResponseDTO error = ErrorResponseDTO.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
        .error("Unprocessable Entity")
        .message(ex.getMessage())
        .path(request.getRequestURI())
        .build();

    log.error("Violation de règle métier: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
  }

  /**
   * Gère ValidationException (400 Bad Request)
   * Données invalides ou format incorrect
   */
  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorResponseDTO> handleValidation(
      ValidationException ex,
      HttpServletRequest request) {

    ErrorResponseDTO error = ErrorResponseDTO.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.BAD_REQUEST.value())
        .error("Bad Request")
        .message(ex.getMessage())
        .path(request.getRequestURI())
        .build();

    log.error("Erreur de validation: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Gère AccessDeniedException (403 Forbidden)
   * Accès refusé par manque de permissions
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponseDTO> handleAccessDenied(
      AccessDeniedException ex,
      HttpServletRequest request) {

    ErrorResponseDTO error = ErrorResponseDTO.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.FORBIDDEN.value())
        .error("Forbidden")
        .message("Vous n'avez pas les permissions nécessaires pour accéder à cette ressource")
        .path(request.getRequestURI())
        .build();

    log.error("Accès refusé: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
  }

  /**
   * Gère AuthenticationException (401 Unauthorized)
   * Utilisateur non authentifié ou session expirée
   */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponseDTO> handleAuthentication(
      AuthenticationException ex,
      HttpServletRequest request) {

    ErrorResponseDTO error = ErrorResponseDTO.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.UNAUTHORIZED.value())
        .error("Unauthorized")
        .message("Authentification requise. Veuillez vous connecter.")
        .path(request.getRequestURI())
        .build();

    log.error("Erreur d'authentification: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  /**
   * Gère MethodArgumentNotValidException (400 Bad Request)
   * Erreurs de validation des paramètres de requête (Spring Validation)
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpServletRequest request) {

    // Récupère tous les messages d'erreur de validation
    String validationErrors = ex.getBindingResult()
        .getAllErrors()
        .stream()
        .map(error -> {
          if (error instanceof FieldError) {
            FieldError fieldError = (FieldError) error;
            return fieldError.getField() + ": " + fieldError.getDefaultMessage();
          }
          return error.getDefaultMessage();
        })
        .collect(Collectors.joining(", "));

    String message = "Erreur de validation des données: " + validationErrors;

    ErrorResponseDTO error = ErrorResponseDTO.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.BAD_REQUEST.value())
        .error("Bad Request")
        .message(message)
        .path(request.getRequestURI())
        .build();

    log.error("Erreurs de validation: {}", validationErrors);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Gère IllegalArgumentException (400 Bad Request)
   * Arguments invalides passés aux méthodes
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(
      IllegalArgumentException ex,
      HttpServletRequest request) {

    ErrorResponseDTO error = ErrorResponseDTO.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.BAD_REQUEST.value())
        .error("Bad Request")
        .message(ex.getMessage())
        .path(request.getRequestURI())
        .build();

    log.error("Argument invalide: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Gère Exception (500 Internal Server Error)
   * Erreurs non gérées - erreur serveur générique
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDTO> handleGeneralException(
      Exception ex,
      HttpServletRequest request) {

    ErrorResponseDTO error = ErrorResponseDTO.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .error("Internal Server Error")
        .message("Une erreur interne s'est produite. Veuillez réessayer plus tard.")
        .path(request.getRequestURI())
        .build();

    log.error("Erreur serveur interne", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
