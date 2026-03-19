package ru.svoi.mastera.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.svoi.mastera.backend.dto.ChangePasswordDto;
import ru.svoi.mastera.backend.dto.UpdateProfileDto;
import ru.svoi.mastera.backend.dto.UserProfileDto;
import ru.svoi.mastera.backend.service.UserProfileService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserProfileController {
    private final UserProfileService userProfileService;

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileDto> getProfile(@PathVariable UUID userId) {
        UserProfileDto profile = userProfileService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<UserProfileDto> updateProfile(
            @PathVariable UUID userId,
            @RequestBody UpdateProfileDto dto
    ) {
        UserProfileDto updated = userProfileService.updateProfile(userId, dto);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{userId}/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable UUID userId,
            @RequestBody ChangePasswordDto dto
    ) {
        try {
            userProfileService.changePassword(userId, dto);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}