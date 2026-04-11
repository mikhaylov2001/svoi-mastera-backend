package ru.svoi.mastera.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
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
    private boolean hasReview;
    private boolean hasWorkerReview;
    private String[] photos;
    private String workerAvatar;
    private String workerLastName;
    private String customerAvatar;
    private String customerLastName;

}