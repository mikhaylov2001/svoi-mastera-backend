package ru.svoi.mastera.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.svoi.mastera.backend.entity.User;
import ru.svoi.mastera.backend.repository.UserRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/avatar")
@RequiredArgsConstructor
public class AvatarController {

    private final UserRepository userRepository;

    private static final String UPLOAD_DIR = "/tmp/avatars/";

    @PostMapping("/upload")
    public Map<String, String> upload(@RequestHeader("X-User-Id") UUID userId,
                                       @RequestBody Map<String, String> body) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String base64 = body.get("image"); // "data:image/png;base64,..."
        if (base64 == null || !base64.contains(",")) {
            throw new RuntimeException("Invalid image data");
        }

        String data = base64.split(",")[1];
        String ext = base64.contains("image/png") ? ".png" : ".jpg";
        String filename = userId + ext;

        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();

            File file = new File(UPLOAD_DIR + filename);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(Base64.getDecoder().decode(data));
            }

            String url = "/api/v1/avatar/" + filename;
            user.setAvatarUrl(url);
            userRepository.save(user);

            return Map.of("avatarUrl", url);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save avatar: " + e.getMessage());
        }
    }

    @GetMapping("/{filename}")
    public byte[] getAvatar(@PathVariable String filename) {
        try {
            File file = new File(UPLOAD_DIR + filename);
            if (!file.exists()) throw new RuntimeException("Avatar not found");
            return java.nio.file.Files.readAllBytes(file.toPath());
        } catch (Exception e) {
            throw new RuntimeException("Avatar not found");
        }
    }
}