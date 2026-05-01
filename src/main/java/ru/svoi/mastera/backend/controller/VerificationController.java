package ru.svoi.mastera.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.svoi.mastera.backend.dto.VerificationMeDto;
import ru.svoi.mastera.backend.dto.VerificationRejectDto;
import ru.svoi.mastera.backend.dto.VerificationSubmitDto;
import ru.svoi.mastera.backend.service.VerificationService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/verification")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VerificationController {

    private final VerificationService verificationService;

    @GetMapping("/me")
    public VerificationMeDto me(@RequestHeader("X-User-Id") UUID userId) {
        return verificationService.getMe(userId);
    }

    @PostMapping("/submit")
    public VerificationMeDto submit(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody VerificationSubmitDto dto
    ) {
        return verificationService.submit(userId, dto);
    }

    /** Модерация: одобрить верификацию пользователя (user id аккаунта). */
    @PostMapping("/admin/users/{targetUserId}/approve")
    public void approveAdmin(
            @RequestHeader("X-Admin-Key") String adminKey,
            @PathVariable UUID targetUserId
    ) {
        verificationService.approveByAdmin(targetUserId, adminKey);
    }

    @PostMapping("/admin/users/{targetUserId}/reject")
    public void rejectAdmin(
            @RequestHeader("X-Admin-Key") String adminKey,
            @PathVariable UUID targetUserId,
            @RequestBody(required = false) VerificationRejectDto body
    ) {
        verificationService.rejectByAdmin(targetUserId, adminKey, body != null ? body.getReason() : null);
    }
}
