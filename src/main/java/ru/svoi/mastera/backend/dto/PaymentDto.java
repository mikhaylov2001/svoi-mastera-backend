package ru.svoi.mastera.backend.dto;

import ru.svoi.mastera.backend.entity.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentDto(
        UUID id,
        UUID dealId,
        BigDecimal amount,
        String currency,
        String provider,
        String providerPaymentId,
        String status,
        String type,
        Instant paidAt,
        Instant createdAt
) {
    public static PaymentDto from(Payment p) {
        return new PaymentDto(
                p.getId(),
                p.getDeal().getId(),
                p.getAmount(),
                p.getCurrency(),
                p.getProvider().name(),
                p.getProviderPaymentId(),
                p.getStatus().name(),
                p.getType().name(),
                p.getPaidAt(),
                p.getCreatedAt()
        );
    }
}
