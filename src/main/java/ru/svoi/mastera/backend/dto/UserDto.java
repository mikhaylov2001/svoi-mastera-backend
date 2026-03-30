package ru.svoi.mastera.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private UUID id;
    private String email;
    private String displayName;
    private String lastName;
    private boolean hasWorkerProfile;
    private boolean hasCustomerProfile;
    private String avatarUrl;
}
