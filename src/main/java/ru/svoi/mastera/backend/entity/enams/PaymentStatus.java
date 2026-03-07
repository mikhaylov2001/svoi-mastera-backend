package ru.svoi.mastera.backend.entity.enams;

public enum PaymentStatus {
    CREATED,
    PENDING,
    REQUIRES_ACTION,
    SUCCEEDED,
    FAILED,
    CANCELLED,
    REFUNDED,
    PARTIALLY_REFUNDED
}
