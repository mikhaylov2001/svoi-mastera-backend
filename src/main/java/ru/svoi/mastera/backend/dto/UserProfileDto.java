package ru.svoi.mastera.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private UUID id;
    private String displayName;
    private String email;
    private String phone;
    private String city;
    private String role; // "CUSTOMER" или "WORKER"
    private Instant createdAt;
}