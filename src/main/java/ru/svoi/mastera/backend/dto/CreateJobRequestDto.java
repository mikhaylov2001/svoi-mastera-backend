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
public class CreateJobRequestDto {
    private UUID categoryId;
    private String title;
    private String description;
    private String city;
    private String addressText;
    private Instant scheduledAt;
    private BigDecimal budgetFrom;
    private BigDecimal budgetTo;

}
