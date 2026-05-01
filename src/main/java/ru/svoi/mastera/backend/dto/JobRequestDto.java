package ru.svoi.mastera.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private String[] photos;
    private UUID customerId;
    private String customerName;
    private String customerLastName;
    private String customerAvatar;
    /** Число откликов мастеров (включая назначенного по объявлению) */
    private long offersCount;
    /** «Безопасная сделка» на карточке — только если заказчик принял заявление о гарантии. */
    private boolean customerGuaranteeTermsAccepted;
}