package ru.svoi.mastera.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Data
public class DealDto {
    private UUID id;
    private UUID jobRequestId;
    private UUID jobOfferId;
    private UUID customerId;
    private UUID workerId;
    private BigDecimal agreedPrice;
    private String status;
    private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;

    public DealDto() {
    }

    public DealDto(UUID id,
                   UUID jobRequestId,
                   UUID jobOfferId,
                   UUID customerId,
                   UUID workerId,
                   BigDecimal agreedPrice,
                   String status,
                   Instant createdAt,
                   Instant startedAt,
                   Instant completedAt) {
        this.id = id;
        this.jobRequestId = jobRequestId;
        this.jobOfferId = jobOfferId;
        this.customerId = customerId;
        this.workerId = workerId;
        this.agreedPrice = agreedPrice;
        this.status = status;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }
}
