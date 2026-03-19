package ru.svoi.mastera.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
public class DealDto {
    private UUID id;
    private UUID jobRequestId;
    private UUID jobOfferId;
    private UUID customerId;
    private UUID workerId;
    private String customerName;
    private String workerName;
    private String title;
    private String description;
    private String category;
    private BigDecimal agreedPrice;
    private String status;
    private boolean customerConfirmed;
    private boolean workerConfirmed;
    private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;
    private Boolean hasReview;

    public DealDto(UUID id, UUID jobRequestId, UUID jobOfferId,
                   UUID customerId, UUID workerId,
                   String customerName, String workerName,
                   String title, String description, String category,
                   BigDecimal agreedPrice, String status,
                   boolean customerConfirmed, boolean workerConfirmed,
                   Instant createdAt, Instant startedAt, Instant completedAt,
                   Boolean hasReview) {
        this.id = id;
        this.jobRequestId = jobRequestId;
        this.jobOfferId = jobOfferId;
        this.customerId = customerId;
        this.workerId = workerId;
        this.customerName = customerName;
        this.workerName = workerName;
        this.title = title;
        this.description = description;
        this.category = category;
        this.agreedPrice = agreedPrice;
        this.status = status;
        this.customerConfirmed = customerConfirmed;
        this.workerConfirmed = workerConfirmed;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.hasReview = hasReview;
    }
}