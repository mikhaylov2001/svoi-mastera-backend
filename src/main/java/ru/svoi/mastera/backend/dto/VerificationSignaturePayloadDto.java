package ru.svoi.mastera.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationSignaturePayloadDto {
    /** ФИО для учётной записи (как в профиле). */
    private String fullLegalName;
    /** Согласие с правилами платформы (обязательный чекбокс). */
    private Boolean agreementAccepted;
    /** Устарело: раньше требовалась загрузка изображения подписи. */
    private String signatureImageUrl;
}
