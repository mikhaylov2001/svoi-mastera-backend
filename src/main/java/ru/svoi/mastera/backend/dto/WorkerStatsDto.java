package ru.svoi.mastera.backend.dto;

import java.time.Instant;

public record WorkerStatsDto(
        Double averageRating,
        Long completedWorksCount,  // ✅ ДОБАВЛЕНО
        long registeredAt,      // ✅ ДОБАВЛЕНО - дата регистрации
        Instant reviewsCount
) {}

