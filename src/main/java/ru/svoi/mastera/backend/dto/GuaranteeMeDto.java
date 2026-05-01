package ru.svoi.mastera.backend.dto;

import java.time.Instant;

public record GuaranteeMeDto(
        boolean profileVerified,
        boolean guaranteeAccepted,
        Instant guaranteeAcceptedAt,
        String consentVersion
) {}
