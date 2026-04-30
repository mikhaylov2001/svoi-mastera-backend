package ru.svoi.mastera.backend.dto;

import java.time.Instant;
import java.util.UUID;

public record ListingDto(
        UUID id,
        UUID workerId,
        String workerName,
        String workerLastName,
        String workerAvatar,
        String title,
        String description,
        Integer price,
        String priceUnit,
        String category,
        String[] photos,
        boolean active,
        Instant createdAt,
        int viewCount,
        long pendingDealsCount,
        /** Объявление снято с публикации после завершённой сделки — без восстановления и редактирования */
        boolean lockedAfterCompletedDeal
) {}