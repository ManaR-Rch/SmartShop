package com.example.smartshop.entity;

/**
 * Payment methods accepted in SmartShop
 * 
 * CASH: immediate payment, legal limit 20,000 DH per payment (Article 193 CGI)
 * CHEQUE: can be deferred (future due date)
 * TRANSFER: immediate or deferred payment
 */
public enum PaymentMethod {
  CASH("Espèces"),
  CHEQUE("Chèque"),
  TRANSFER("Virement");

  private final String label;

  PaymentMethod(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
