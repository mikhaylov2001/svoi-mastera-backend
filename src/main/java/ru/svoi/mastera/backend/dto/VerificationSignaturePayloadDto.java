package ru.svoi.mastera.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationSignaturePayloadDto {
    private String fullLegalName;
    private Boolean agreementAccepted;
    /** URL файла подписи после загрузки через /api/v1/files/upload */
    private String signatureImageUrl;
}
