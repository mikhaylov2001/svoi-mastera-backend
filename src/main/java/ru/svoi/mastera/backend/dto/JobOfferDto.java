package ru.svoi.mastera.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Data
@NoArgsConstructor
public class JobOfferDto {
    private UUID id;
    private UUID jobRequestId;
    private UUID workerId;
    private String message;
    private BigDecimal price;
    private Integer estimatedDays;
    private String status;
    private Instant createdAt;
    public JobOfferDto(UUID id,
                       UUID jobRequestId,
                       UUID workerId,
                       String message,
                       BigDecimal price,
                       Integer estimatedDays,
                       String status,
                       Instant createdAt) {
        this.id = id;
        this.jobRequestId = jobRequestId;
        this.workerId = workerId;
        this.message = message;
        this.price = price;
        this.estimatedDays = estimatedDays;
        this.status = status;
        this.createdAt = createdAt;
    }
}
