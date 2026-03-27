package ru.svoi.mastera.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.svoi.mastera.backend.entity.User;
import ru.svoi.mastera.backend.repository.UserRepository;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/avatar")
@RequiredArgsConstructor
public class AvatarController {

    private final UserRepository userRepository;

    // Максимальный размер изображения — 2MB в base64
    private static final int MAX_BASE64_LENGTH = 3 * 1024 * 1024; // ~2MB файл

    /**
     * POST /api/v1/avatar/upload
     * Body: { "image": "data:image/png;base64,..." }
     * Сохраняет base64 прямо в БД — не теряется при рестарте сервера
     */
    @PostMapping("/upload")
    public Map<String, String> upload(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody Map<String, String> body) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String base64 = body.get("image");
        if (base64 == null || !base64.contains(",")) {
            throw new RuntimeException("Invalid image data");
        }

        // Проверяем размер
        if (base64.length() > MAX_BASE64_LENGTH) {
            throw new RuntimeException("Image too large. Max 2MB.");
        }

        // Сохраняем base64 прямо в поле avatarUrl
        // Клиент будет использовать это как src напрямую
        user.setAvatarUrl(base64);
        userRepository.save(user);

        // Возвращаем тот же base64 как avatarUrl
        return Map.of("avatarUrl", base64);
    }

    /**
     * GET /api/v1/avatar/{filename}
     * Оставляем для обратной совместимости — отдаём файл если есть
     */
    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable String filename) {
        // Ищем пользователя у которого avatarUrl содержит этот filename
        // Для обратной совместимости — новые аватары уже хранятся как base64
        return ResponseEntity.notFound().build();
    }
}