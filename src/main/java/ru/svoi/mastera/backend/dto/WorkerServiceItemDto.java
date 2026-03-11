package ru.svoi.mastera.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WorkerServiceItemDto(
        UUID id,
        UUID workerUserId,
        String title,
        String description,
        BigDecimal priceFrom,
        BigDecimal priceTo,
        boolean active,
        Instant createdAt
) {}

