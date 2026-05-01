package ru.svoi.mastera.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationSubmitDto {
    private List<String> documentUrls;
    private VerificationSignaturePayloadDto signature;
}
