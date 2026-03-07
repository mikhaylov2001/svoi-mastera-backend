package ru.svoi.mastera.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
public class JobRequestDto {
    private UUID id;
    private UUID categoryId;
    private String title;
    private String description;
    private String city;
    private String addressText;
    private Instant createdAt;
    private Instant scheduledAt;
    private BigDecimal budgetFrom;
    private BigDecimal budgetTo;
    private String status;

    public JobRequestDto(UUID id,
                         UUID categoryId,
                         String title,
                         String description,
                         String city,
                         String addressText,
                         Instant createdAt,
                         Instant scheduledAt,
                         BigDecimal budgetFrom,
                         BigDecimal budgetTo,
                         String status) {
        this.id = id;
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.city = city;
        this.addressText = addressText;
        this.createdAt = createdAt;
        this.scheduledAt = scheduledAt;
        this.budgetFrom = budgetFrom;
        this.budgetTo = budgetTo;
        this.status = status;
    }
}
