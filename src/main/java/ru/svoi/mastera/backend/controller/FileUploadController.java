package ru.svoi.mastera.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@RestController
@RequestMapping("/api/v1/files")
public class FileUploadController {

    // Та же директория что и у аватаров — /tmp
    private static final String UPLOAD_DIR = "/tmp/chat-files/";

    // Базовый URL сервера — замени на свой домен
    private static final String BASE_URL = "https://svoi-mastera-backend-n9om.onrender.com";

    private static final long MAX_SIZE = 50L * 1024 * 1024; // 50 MB

    // Проверка типа файла — по префиксу для медиа, точно для документов
    private static boolean isAllowed(String contentType) {
        if (contentType == null) return false;
        String ct = contentType.toLowerCase().split(";")[0].trim(); // убираем ";codecs=..."
        if (ct.startsWith("image/")) return true;
        if (ct.startsWith("audio/")) return true;
        if (ct.startsWith("video/")) return true;
        return Set.of(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/zip", "application/x-zip-compressed",
                "application/x-rar-compressed", "application/x-7z-compressed",
                "text/plain"
        ).contains(ct);
    }

    /**
     * POST /api/v1/files/upload
     * Header: X-User-Id: {userId}
     * Body: multipart/form-data, поле "file"
     *
     * Возвращает: { url, filename, size, contentType }
     */
    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestPart("file") MultipartFile file) {

        // Валидация
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Файл пустой"));
        }
        if (file.getSize() > MAX_SIZE) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Файл слишком большой. Максимум 50 МБ"));
        }
        String contentType = file.getContentType();
        if (!isAllowed(contentType)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Недопустимый тип файла: " + contentType));
        }

        // Нормализуем contentType — убираем ;codecs=...
        String normalizedType = contentType != null
                ? contentType.toLowerCase().split(";")[0].trim()
                : "application/octet-stream";

        try {
            // Директория для пользователя: /tmp/chat-files/{userId}/
            String userDir = UPLOAD_DIR + userId + "/";
            File dir = new File(userDir);
            if (!dir.exists()) dir.mkdirs();

            // Уникальное имя файла
            String originalName = file.getOriginalFilename();
            String ext = (originalName != null && originalName.contains("."))
                    ? originalName.substring(originalName.lastIndexOf('.'))
                    : "";
            String uniqueName = UUID.randomUUID() + ext;

            // Сохраняем файл
            File dest = new File(userDir + uniqueName);
            file.transferTo(dest);

            // URL для скачивания
            String fileUrl = BASE_URL + "/api/v1/files/" + userId + "/" + uniqueName;

            Map<String, Object> response = new HashMap<>();
            response.put("url", fileUrl);
            response.put("filename", originalName);
            response.put("size", file.getSize());
            response.put("contentType", normalizedType);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Ошибка сохранения: " + e.getMessage()));
        }
    }

    /**
     * GET /api/v1/files/{userId}/{filename}
     * Отдаёт файл по URL
     */
    @GetMapping("/{userId}/{filename}")
    public ResponseEntity<byte[]> getFile(
            @PathVariable String userId,
            @PathVariable String filename) {

        // Защита от path traversal
        if (filename.contains("..") || filename.contains("/")) {
            return ResponseEntity.badRequest().build();
        }

        try {
            File file = new File(UPLOAD_DIR + userId + "/" + filename);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            byte[] content = Files.readAllBytes(file.toPath());
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                    .header("Cache-Control", "public, max-age=31536000") // кешируем на год
                    .body(content);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}