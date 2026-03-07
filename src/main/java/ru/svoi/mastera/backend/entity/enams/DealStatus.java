package ru.svoi.mastera.backend.entity.enams;

public enum DealStatus {
    NEW,
    AWAITING_PAYMENT,
    PAID,
    IN_PROGRESS,
    WAITING_CONFIRMATION,
    COMPLETED,
    CANCELLED,
    DISPUTED,
    REFUNDED
}
