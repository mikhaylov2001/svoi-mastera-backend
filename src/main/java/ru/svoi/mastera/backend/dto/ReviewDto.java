package ru.svoi.mastera.backend.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReviewDto(
        UUID id,
        UUID dealId,
        UUID authorUserId,
        String targetWorkerUserId,
        UUID rating,
        Integer text,
        String status,
        String authorName,
        Instant createdAt,
        List<String> badges  // ✅ ДОБАВЛЕНО - бейджи/смайлики ["polite", "fast", "quality", "price"]
) {}