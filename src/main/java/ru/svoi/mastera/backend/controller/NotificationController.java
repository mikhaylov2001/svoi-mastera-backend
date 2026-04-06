package ru.svoi.mastera.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.svoi.mastera.backend.dto.NotificationDto;
import ru.svoi.mastera.backend.service.NotificationService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // GET /api/v1/notifications  — все уведомления
    @GetMapping
    public List<NotificationDto> getAll(@RequestHeader("X-User-Id") UUID userId) {
        return notificationService.getAll(userId);
    }

    // GET /api/v1/notifications/unread-count
    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(@RequestHeader("X-User-Id") UUID userId) {
        return Map.of("count", notificationService.countUnread(userId));
    }

    // POST /api/v1/notifications/{id}/read — прочитать одно
    @PostMapping("/{id}/read")
    public void markRead(@PathVariable UUID id) {
        notificationService.markRead(id);
    }

    // POST /api/v1/notifications/read-all — прочитать все
    @PostMapping("/read-all")
    public void markAllRead(@RequestHeader("X-User-Id") UUID userId) {
        notificationService.markAllRead(userId);
    }
}