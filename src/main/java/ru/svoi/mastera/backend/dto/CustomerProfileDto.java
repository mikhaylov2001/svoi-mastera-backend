package ru.svoi.mastera.backend.dto;

import java.time.Instant;
import java.util.UUID;

public class CustomerProfileDto {
    private UUID id;
    private String displayName;
    private String city;
    private Instant createdAt;

    public CustomerProfileDto(UUID id, String displayName, String city, Instant createdAt) {
        this.id = id;
        this.displayName = displayName;
        this.city = city;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCity() {
        return city;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
