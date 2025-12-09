package com.example.smartshop.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standardized error response format for API
 * 
 * Follows requirements page 8 of SmartShop technical specification:
 * - timestamp: date/heure de l'erreur (ISO 8601)
 * - status: code HTTP numérique
 * - error: type d'erreur (ex: "Bad Request", "Not Found")
 * - message: message explicatif en français
 * - path: chemin de la requête qui a échoué
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponseDTO {

  @JsonProperty("timestamp")
  private LocalDateTime timestamp;

  @JsonProperty("status")
  private Integer status;

  @JsonProperty("error")
  private String error;

  @JsonProperty("message")
  private String message;

  @JsonProperty("path")
  private String path;

  /**
   * Factory method to create error response with all required fields
   */
  public static ErrorResponseDTO of(LocalDateTime timestamp, Integer status, String error, String message,
      String path) {
    return ErrorResponseDTO.builder()
        .timestamp(timestamp)
        .status(status)
        .error(error)
        .message(message)
        .path(path)
        .build();
  }
}
