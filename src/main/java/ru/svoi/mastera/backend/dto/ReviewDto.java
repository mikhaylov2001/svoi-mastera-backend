package ru.svoi.mastera.backend.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReviewDto(
        UUID id,
        UUID dealId,
        UUID authorUserId,
        String authorName,
        String authorLastName,
        String authorAvatarUrl,
        UUID targetWorkerUserId,
        Integer rating,
        String text,
        String status,
        Instant createdAt,
        List<String> badges
) {}