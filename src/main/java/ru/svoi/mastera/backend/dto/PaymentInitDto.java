package ru.svoi.mastera.backend.dto;

import java.util.UUID;

public record PaymentInitDto(
        UUID paymentId,
        String confirmationUrl
) {}