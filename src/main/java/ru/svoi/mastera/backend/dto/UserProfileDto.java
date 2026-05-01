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
    private String lastName;    // ← третий аргумент (после displayName)
    private String email;
    private String phone;
    private String city;
    private String role;
    private Instant createdAt;
    /** URL или data:image;base64,... из User.avatarUrl */
    private String avatarUrl;

    private boolean verified;
    private String verificationStatus;
    private String verificationRejectionReason;
}