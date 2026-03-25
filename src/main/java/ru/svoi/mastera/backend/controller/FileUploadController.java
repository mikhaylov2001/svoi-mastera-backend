package ru.svoi.mastera.backend.controller;
 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
 
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
 
@RestController
@RequestMapping("/api/v1/files")
@CrossOrigin(origins = "*")
public class FileUploadController {
 
    // В application.properties: file.upload-dir=./uploads
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;
 
    // В application.properties: file.base-url=https://svoi-mastera-backend.onrender.com
    @Value("${file.base-url:http://localhost:8080}")
    private String baseUrl;
 
    private static final long MAX_SIZE = 50 * 1024 * 1024; // 50 MB
 
    private static final Set<String> ALLOWED_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp",
        "video/mp4", "video/webm", "video/quicktime",
        "audio/webm", "audio/mp4", "audio/mpeg", "audio/ogg", "audio/wav",
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/zip", "application/x-rar-compressed",
        "text/plain"
    );
 
    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestHeader("X-User-Id") String userId,
            @RequestPart("file") MultipartFile file) {
 
        // Проверки
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Файл пустой"));
        }
        if (file.getSize() > MAX_SIZE) {
            return ResponseEntity.badRequest().body(Map.of("message", "Файл слишком большой (макс 50 МБ)"));
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Недопустимый тип файла: " + contentType));
        }
 
        try {
            // Создаём директорию если нет
            Path uploadPath = Paths.get(uploadDir, userId);
            Files.createDirectories(uploadPath);
 
            // Уникальное имя файла
            String originalName = file.getOriginalFilename();
            String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf('.'))
                : "";
            String uniqueName = UUID.randomUUID() + ext;
 
            // Сохраняем
            Path filePath = uploadPath.resolve(uniqueName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
 
            // Возвращаем URL
            String fileUrl = baseUrl + "/api/v1/files/" + userId + "/" + uniqueName;
 
            Map<String, Object> response = new HashMap<>();
            response.put("url", fileUrl);
            response.put("filename", originalName);
            response.put("size", file.getSize());
            response.put("contentType", contentType);
 
            return ResponseEntity.ok(response);
 
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "Ошибка сохранения файла: " + e.getMessage()));
        }
    }
 
    // Отдаём файл по URL
    @GetMapping("/{userId}/{filename}")
    public ResponseEntity<byte[]> getFile(
            @PathVariable String userId,
            @PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir, userId, filename);
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            byte[] content = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "application/octet-stream";
 
            return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                .body(content);
 
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}