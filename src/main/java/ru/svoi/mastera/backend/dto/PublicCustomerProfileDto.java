package ru.svoi.mastera.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicCustomerProfileDto {
    private UUID id;
    private String displayName;
    private String lastName;
    private String city;
    private String avatarUrl;
    private Instant registeredAt;
    private int totalRequests;
    private int completedRequests;
    private int openRequests;
}