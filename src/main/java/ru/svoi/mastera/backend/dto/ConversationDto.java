package ru.svoi.mastera.backend.dto;

import java.time.Instant;
import java.util.UUID;

public record ConversationDto(
        UUID partnerId,
        String partnerName,
        String partnerAvatarUrl,
        String lastMessage,
        Instant lastMessageAt,
        long unreadCount
) {}