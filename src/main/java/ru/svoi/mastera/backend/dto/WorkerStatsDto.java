package ru.svoi.mastera.backend.dto;

import java.time.Instant;

public record WorkerStatsDto(
        Double averageRating,
        Long reviewsCount,
        Long completedWorksCount,
        Instant registeredAt,
        String displayName,
        String lastName,
        String avatarUrl,
        String city     // ✅ ИСПРАВЛЕНО: порядок
) {}