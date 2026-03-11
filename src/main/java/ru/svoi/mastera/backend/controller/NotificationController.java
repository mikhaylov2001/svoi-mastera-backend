package ru.svoi.mastera.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestHeader("X-User-Id") String userId,
                                       @RequestBody Map<String, Object> payload) {
        // В реальности здесь сохраняем объект подписки (endpoint/key) в БД.
        return ResponseEntity.ok(Map.of("status", "subscribed", "userId", userId));
    }

}
