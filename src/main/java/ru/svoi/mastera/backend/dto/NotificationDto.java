package ru.svoi.mastera.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {
    private UUID id;
    private String type;
    private String title;
    private String body;
    private String link;
    @JsonProperty("isRead")
    private boolean isRead;
    private Instant createdAt;
}