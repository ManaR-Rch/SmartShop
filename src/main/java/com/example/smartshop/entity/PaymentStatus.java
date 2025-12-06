package com.example.smartshop.entity;

/**
 * Payment statuses in SmartShop
 * 
 * EN_ATTENTE: payment received but not yet collected
 * ENCAISSE: amount effectively received
 * REJETÉ: payment rejected (exceeds legal limits, insufficient funds, etc.)
 */
public enum PaymentStatus {
    EN_ATTENTE("En attente"),
    ENCAISSE("Encaissé"),
    REJETÉ("Rejeté");

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
