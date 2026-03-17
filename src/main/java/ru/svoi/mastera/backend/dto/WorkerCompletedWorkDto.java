package ru.svoi.mastera.backend.dto;
 
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
 
public record WorkerCompletedWorkDto(
        UUID id,
        String title,
        String description,
        String categoryName,
        BigDecimal price,
        Instant completedAt,
        String customerName  // Без фамилии для приватности
) {}