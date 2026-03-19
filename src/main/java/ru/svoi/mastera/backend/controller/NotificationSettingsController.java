package ru.svoi.mastera.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.svoi.mastera.backend.dto.NotificationSettingsDto;
import ru.svoi.mastera.backend.service.NotificationSettingsService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationSettingsController {
    private final NotificationSettingsService settingsService;

    @GetMapping("/{userId}/notification-settings")
    public ResponseEntity<NotificationSettingsDto> getSettings(@PathVariable UUID userId) {
        NotificationSettingsDto settings = settingsService.getSettings(userId);
        return ResponseEntity.ok(settings);
    }

    @PutMapping("/{userId}/notification-settings")
    public ResponseEntity<NotificationSettingsDto> updateSettings(
            @PathVariable UUID userId,
            @RequestBody NotificationSettingsDto dto
    ) {
        NotificationSettingsDto updated = settingsService.updateSettings(userId, dto);
        return ResponseEntity.ok(updated);
    }
}