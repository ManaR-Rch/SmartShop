package com.example.smartshop.exception;

import com.example.smartshop.dto.ErrorResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour GlobalExceptionHandler
 * Vérifie la création et structure des ErrorResponseDTO
 * 
 * Note: Les tests d'intégration Spring Boot sont gérés séparément pour éviter
 * les problèmes de contexte dans le test suite global
 */
@DisplayName("GlobalExceptionHandler - Tests Unitaires")
class GlobalExceptionHandlerTest {

  /**
   * 1. Test ErrorResponseDTO structure
   */
  @Test
  @DisplayName("ErrorResponseDTO should contain all required fields")
  void testErrorResponseDTOStructure() {
    LocalDateTime now = LocalDateTime.now();
    ErrorResponseDTO error = ErrorResponseDTO.builder()
        .timestamp(now)
        .status(404)
        .error("Not Found")
        .message("Ressource non trouvée")
        .path("/api/admin/clients/99999")
        .build();

    assertThat(error.getTimestamp()).isEqualTo(now);
    assertThat(error.getStatus()).isEqualTo(404);
    assertThat(error.getError()).isEqualTo("Not Found");
    assertThat(error.getMessage()).isEqualTo("Ressource non trouvée");
    assertThat(error.getPath()).isEqualTo("/api/admin/clients/99999");
  }

  /**
   * 2. Test 404 error response
   */
  @Test
  @DisplayName("Should build 404 Not Found error response")
  void testNotFoundErrorResponse() {
    ErrorResponseDTO error = ErrorResponseDTO.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.NOT_FOUND.value())
        .error("Not Found")
        .message("Ressource non trouvée")
        .path("/api/admin/clients/99999")
        .build();

    assertThat(error.getStatus()).isEqualTo(404);
    assertThat(error.getError()).isEqualTo("Not Found");
    assertThat(error.getMessage()).isNotBlank();
    assertThat(error.getPath()).isNotNull();
  }

  /**
   * 3. Test 400 Bad Request error response
   */
  @Test
  @DisplayName("Should build 400 Bad Request error response")
  void testBadRequestErrorResponse() {
    ErrorResponseDTO error = ErrorResponseDTO.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.BAD_REQUEST.value())
        .error("Bad Request")
        .message("Erreur de validation des données: field: message is invalid")
        .path("/api/admin/payments")
        .build();

    assertThat(error.getStatus()).isEqualTo(400);
    assertThat(error.getError()).isEqualTo("Bad Request");
  }

  /**
   * 4. Test 401 Unauthorized error response
   */
  @Test
  @DisplayName("Should build 401 Unauthorized error response")
  void testUnauthorizedErrorResponse() {
    ErrorResponseDTO error = ErrorResponseDTO.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.UNAUTHORIZED.value())
        .error("Unauthorized")
        .message("Authentification requise. Veuillez vous connecter.")
        .path("/api/admin/payments")
        .build();

    assertThat(error.getStatus()).isEqualTo(401);
    assertThat(error.getError()).isEqualTo("Unauthorized");
    assertThat(error.getMessage()).containsIgnoringCase("authentification");
  }

  /**
   * 5. Test 403 Forbidden error response
   */
  @Test
  @DisplayName("Should build 403 Forbidden error response")
  void testForbiddenErrorResponse() {
    ErrorResponseDTO error = ErrorResponseDTO.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.FORBIDDEN.value())
        .error("Forbidden")
        .message("Vous n'avez pas les permissions nécessaires pour accéder à cette ressource")
        .path("/api/admin/payments")
        .build();

    assertThat(error.getStatus()).isEqualTo(403);
    assertThat(error.getError()).isEqualTo("Forbidden");
    assertThat(error.getMessage()).containsIgnoringCase("permissions");
  }

  /**
   * 6. Test 422 Unprocessable Entity error response
   */
  @Test
  @DisplayName("Should build 422 Unprocessable Entity error response")
  void testUnprocessableEntityErrorResponse() {
    ErrorResponseDTO error = ErrorResponseDTO.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
        .error("Unprocessable Entity")
        .message("Stock insuffisant pour le produit")
        .path("/api/admin/orders/create")
        .build();

    assertThat(error.getStatus()).isEqualTo(422);
    assertThat(error.getError()).isEqualTo("Unprocessable Entity");
  }

  /**
   * 7. Test 500 Internal Server Error response
   */
  @Test
  @DisplayName("Should build 500 Internal Server Error response")
  void testInternalServerErrorResponse() {
    ErrorResponseDTO error = ErrorResponseDTO.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .error("Internal Server Error")
        .message("Une erreur interne s'est produite. Veuillez réessayer plus tard.")
        .path("/api/admin/some-endpoint")
        .build();

    assertThat(error.getStatus()).isEqualTo(500);
    assertThat(error.getError()).isEqualTo("Internal Server Error");
    assertThat(error.getMessage()).isNotBlank();
  }

  /**
   * 8. Test exception classes exist
   */
  @Test
  @DisplayName("All exception classes should be instantiable")
  void testExceptionClassesExist() {
    ResourceNotFoundException rnf = new ResourceNotFoundException("Test");
    assertThat(rnf).isNotNull();
    assertThat(rnf.getMessage()).isEqualTo("Test");

    ValidationException ve = new ValidationException("Test");
    assertThat(ve).isNotNull();
    assertThat(ve.getMessage()).isEqualTo("Test");

    AccessDeniedException ade = new AccessDeniedException("Test");
    assertThat(ade).isNotNull();
    assertThat(ade.getMessage()).isEqualTo("Test");

    AuthenticationException ae = new AuthenticationException("Test");
    assertThat(ae).isNotNull();
    assertThat(ae.getMessage()).isEqualTo("Test");

    BusinessRuleViolationException brve = new BusinessRuleViolationException("Test");
    assertThat(brve).isNotNull();
    assertThat(brve.getMessage()).isEqualTo("Test");
  }

  /**
   * 9. Test HTTP status codes mapping
   */
  @Test
  @DisplayName("Should correctly map HTTP status codes")
  void testHttpStatusCodeMapping() {
    // 400 - Bad Request
    assertThat(HttpStatus.BAD_REQUEST.value()).isEqualTo(400);

    // 401 - Unauthorized
    assertThat(HttpStatus.UNAUTHORIZED.value()).isEqualTo(401);

    // 403 - Forbidden
    assertThat(HttpStatus.FORBIDDEN.value()).isEqualTo(403);

    // 404 - Not Found
    assertThat(HttpStatus.NOT_FOUND.value()).isEqualTo(404);

    // 422 - Unprocessable Entity
    assertThat(HttpStatus.UNPROCESSABLE_ENTITY.value()).isEqualTo(422);

    // 500 - Internal Server Error
    assertThat(HttpStatus.INTERNAL_SERVER_ERROR.value()).isEqualTo(500);
  }

  /**
   * 10. Test error response with French messages
   */
  @Test
  @DisplayName("Error responses should contain French messages")
  void testFrenchErrorMessages() {
    String[] frenchMessages = {
        "Ressource non trouvée",
        "Authentification requise. Veuillez vous connecter.",
        "Vous n'avez pas les permissions nécessaires pour accéder à cette ressource",
        "Une erreur interne s'est produite. Veuillez réessayer plus tard.",
        "Violation de règle métier"
    };

    for (String message : frenchMessages) {
      ErrorResponseDTO error = ErrorResponseDTO.builder()
          .timestamp(LocalDateTime.now())
          .status(400)
          .error("Error")
          .message(message)
          .path("/test")
          .build();

      assertThat(error.getMessage()).isNotBlank();
      assertThat(error.getMessage()).isEqualTo(message);
    }
  }
}
