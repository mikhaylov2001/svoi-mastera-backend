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
    /** Дата рождения, ISO yyyy-MM-dd. */
    private String birthDate;
    /** Город / населённый пункт / регион проживания. */
    private String residence;
    /** Согласие с правилами платформы (обязательный чекбокс). */
    private Boolean agreementAccepted;
    /** Устарело: раньше требовалась загрузка изображения подписи. */
    private String signatureImageUrl;
}
