package ru.svoi.mastera.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpsertWorkerServiceItemDto {
    private String title;
    private String description;
    private BigDecimal priceFrom;
    private BigDecimal priceTo;
    private Boolean active;
}

