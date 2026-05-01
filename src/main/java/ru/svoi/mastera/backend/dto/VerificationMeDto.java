package ru.svoi.mastera.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationMeDto {
    private String role;
    private boolean verified;
    private String verificationStatus;
    private String verificationRejectionReason;
}
