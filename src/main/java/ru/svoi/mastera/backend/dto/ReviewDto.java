package ru.svoi.mastera.backend.dto;

import java.time.Instant;
import java.util.UUID;

public record ReviewDto(
        UUID id,
        UUID dealId,
        UUID authorUserId,
        UUID targetWorkerUserId,
        int rating,
        String text,
        String status,
        Instant createdAt
) {}