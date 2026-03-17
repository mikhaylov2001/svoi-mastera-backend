package ru.svoi.mastera.backend.dto;

import java.time.Instant;

public record WorkerStatsDto(
        Double averageRating,
        Long reviewsCount,           // ✅ ИСПРАВЛЕНО: порядок
        Long completedWorksCount,
        Instant registeredAt         // ✅ ИСПРАВЛЕНО: порядок
) {}