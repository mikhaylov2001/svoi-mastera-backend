package ru.svoi.mastera.backend.dto;

import java.time.Instant;
import java.util.UUID;

public record ReviewCreateDto(
        int rating,
        String text
) {}


