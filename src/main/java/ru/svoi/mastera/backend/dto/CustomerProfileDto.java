package ru.svoi.mastera.backend.dto;

import java.time.Instant;
import java.util.UUID;

public record CustomerProfileDto(
        UUID id,
        String displayName,
        String lastName,
        String city,
        String avatarUrl,
        Instant createdAt
) {}