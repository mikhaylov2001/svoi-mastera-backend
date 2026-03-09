package ru.svoi.mastera.backend.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageDto(
        UUID id,
        UUID senderId,
        UUID receiverId,
        String senderName,
        String senderAvatarUrl,
        UUID jobRequestId,
        String text,
        boolean isRead,
        Instant createdAt
) {}