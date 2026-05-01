package ru.svoi.mastera.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Пять обязательных отметок по разделам заявления о личной гарантии и ответственности пользователя.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuaranteeAcceptDto {
    private Boolean acceptDealProcedure;
    private Boolean acceptPaymentSettlement;
    private Boolean acceptDisputeResolution;
    private Boolean acceptOperatorDisclaimer;
    private Boolean acceptPersonalDeclarations;
}
