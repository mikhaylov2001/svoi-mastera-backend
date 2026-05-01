package ru.svoi.mastera.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svoi.mastera.backend.dto.VerificationMeDto;
import ru.svoi.mastera.backend.dto.VerificationSignaturePayloadDto;
import ru.svoi.mastera.backend.dto.VerificationSubmitDto;
import ru.svoi.mastera.backend.entity.CustomerProfile;
import ru.svoi.mastera.backend.entity.User;
import ru.svoi.mastera.backend.entity.WorkerProfile;
import ru.svoi.mastera.backend.entity.enams.VerificationStatus;
import ru.svoi.mastera.backend.repository.CustomerProfileRepository;
import ru.svoi.mastera.backend.repository.UserRepository;
import ru.svoi.mastera.backend.repository.WorkerProfileRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private static final String CONSENT_VERSION = "2026-04-01";
    private static final int MIN_DOCUMENTS = 2;

    private final UserRepository userRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.verification.auto-approve:true}")
    private boolean verificationAutoApprove;

    @Value("${app.verification.admin-key:}")
    private String adminVerificationKey;

    @Transactional(readOnly = true)
    public VerificationMeDto getMe(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        WorkerProfile w = workerProfileRepository.findByUser(user).orElse(null);
        if (w != null) {
            return new VerificationMeDto(
                    "WORKER",
                    w.isVerified(),
                    w.getVerificationStatus().name(),
                    w.getVerificationRejectionReason()
            );
        }
        CustomerProfile c = customerProfileRepository.findByUser(user).orElse(null);
        if (c != null) {
            return new VerificationMeDto(
                    "CUSTOMER",
                    c.isVerified(),
                    c.getVerificationStatus().name(),
                    c.getVerificationRejectionReason()
            );
        }
        throw new RuntimeException("Профиль не найден");
    }

    @Transactional
    public VerificationMeDto submit(UUID userId, VerificationSubmitDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        WorkerProfile worker = workerProfileRepository.findByUser(user).orElse(null);
        if (worker != null) {
            return submitWorker(userId, worker, dto);
        }
        CustomerProfile customer = customerProfileRepository.findByUser(user).orElse(null);
        if (customer != null) {
            return submitCustomer(userId, customer, dto);
        }
        throw new RuntimeException("Профиль не найден");
    }

    private VerificationMeDto submitWorker(UUID userId, WorkerProfile w, VerificationSubmitDto dto) {
        if (w.getVerificationStatus() == VerificationStatus.APPROVED || w.isVerified()) {
            throw new RuntimeException("Верификация уже пройдена");
        }
        if (w.getVerificationStatus() == VerificationStatus.PENDING) {
            throw new RuntimeException("Заявка уже на проверке. Дождитесь решения.");
        }
        validatePayload(userId, dto);
        w.setVerificationDocumentsJson(writeJson(dto.getDocumentUrls()));
        w.setVerificationSignatureJson(buildSignatureJson(dto.getSignature()));
        w.setVerificationSubmittedAt(Instant.now());
        w.setVerificationRejectionReason(null);

        if (verificationAutoApprove) {
            approveWorker(w);
        } else {
            w.setVerificationStatus(VerificationStatus.PENDING);
            w.setVerified(false);
            workerProfileRepository.save(w);
        }
        return getMe(userId);
    }

    private VerificationMeDto submitCustomer(UUID userId, CustomerProfile c, VerificationSubmitDto dto) {
        if (c.getVerificationStatus() == VerificationStatus.APPROVED || c.isVerified()) {
            throw new RuntimeException("Верификация уже пройдена");
        }
        if (c.getVerificationStatus() == VerificationStatus.PENDING) {
            throw new RuntimeException("Заявка уже на проверке. Дождитесь решения.");
        }
        validatePayload(userId, dto);
        c.setVerificationDocumentsJson(writeJson(dto.getDocumentUrls()));
        c.setVerificationSignatureJson(buildSignatureJson(dto.getSignature()));
        c.setVerificationSubmittedAt(Instant.now());
        c.setVerificationRejectionReason(null);

        if (verificationAutoApprove) {
            approveCustomer(c);
        } else {
            c.setVerificationStatus(VerificationStatus.PENDING);
            c.setVerified(false);
            customerProfileRepository.save(c);
        }
        return getMe(userId);
    }

    private void validatePayload(UUID userId, VerificationSubmitDto dto) {
        List<String> urls = dto.getDocumentUrls();
        if (urls == null || urls.size() < MIN_DOCUMENTS) {
            throw new RuntimeException("Загрузите минимум два документа: удостоверение личности и подтверждающий документ.");
        }
        for (String url : urls) {
            if (!urlBelongsToUser(userId, url)) {
                throw new RuntimeException("Недопустимый URL документа. Загрузите файлы заново.");
            }
        }
        VerificationSignaturePayloadDto sig = dto.getSignature();
        if (sig == null) {
            throw new RuntimeException("Заполните блок электронной подписи.");
        }
        if (sig.getFullLegalName() == null || sig.getFullLegalName().trim().length() < 3) {
            throw new RuntimeException("Укажите полное ФИО как в документе.");
        }
        if (sig.getAgreementAccepted() == null || !sig.getAgreementAccepted()) {
            throw new RuntimeException("Необходимо принять условия проверки документов.");
        }
        if (sig.getSignatureImageUrl() == null || sig.getSignatureImageUrl().isBlank()) {
            throw new RuntimeException("Добавьте изображение вашей подписи (поле подписи на странице).");
        }
        if (!urlBelongsToUser(userId, sig.getSignatureImageUrl())) {
            throw new RuntimeException("Недопустимый файл подписи.");
        }
    }

    private boolean urlBelongsToUser(UUID userId, String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        String needle = "/api/v1/files/" + userId + "/";
        return url.contains(needle);
    }

    private String writeJson(List<String> urls) {
        try {
            return objectMapper.writeValueAsString(urls);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка сохранения документов");
        }
    }

    private String buildSignatureJson(VerificationSignaturePayloadDto sig) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("fullLegalName", sig.getFullLegalName().trim());
        map.put("agreementAccepted", Boolean.TRUE.equals(sig.getAgreementAccepted()));
        map.put("consentVersion", CONSENT_VERSION);
        map.put("signatureImageUrl", sig.getSignatureImageUrl());
        map.put("serverRecordedAt", Instant.now().toString());
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка сохранения подписи");
        }
    }

    private void approveWorker(WorkerProfile w) {
        w.setVerificationStatus(VerificationStatus.APPROVED);
        w.setVerified(true);
        w.setVerificationRejectionReason(null);
        workerProfileRepository.save(w);
    }

    private void approveCustomer(CustomerProfile c) {
        c.setVerificationStatus(VerificationStatus.APPROVED);
        c.setVerified(true);
        c.setVerificationRejectionReason(null);
        customerProfileRepository.save(c);
    }

    private void rejectWorker(WorkerProfile w, String reason) {
        w.setVerificationStatus(VerificationStatus.REJECTED);
        w.setVerified(false);
        w.setVerificationRejectionReason(reason != null && !reason.isBlank() ? reason.trim() : "Отклонено модератором");
        workerProfileRepository.save(w);
    }

    private void rejectCustomer(CustomerProfile c, String reason) {
        c.setVerificationStatus(VerificationStatus.REJECTED);
        c.setVerified(false);
        c.setVerificationRejectionReason(reason != null && !reason.isBlank() ? reason.trim() : "Отклонено модератором");
        customerProfileRepository.save(c);
    }

    public void assertAdminKey(String key) {
        if (adminVerificationKey == null || adminVerificationKey.isBlank()) {
            throw new RuntimeException("Модерация верификации не настроена (ADMIN_VERIFICATION_KEY).");
        }
        String a = adminVerificationKey.trim();
        String b = key != null ? key.trim() : "";
        if (!MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8))) {
            throw new RuntimeException("Недостаточно прав");
        }
    }

    @Transactional
    public void approveByAdmin(UUID targetUserId, String adminKey) {
        assertAdminKey(adminKey);
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        WorkerProfile w = workerProfileRepository.findByUser(user).orElse(null);
        if (w != null) {
            approveWorker(w);
            return;
        }
        CustomerProfile c = customerProfileRepository.findByUser(user).orElse(null);
        if (c != null) {
            approveCustomer(c);
            return;
        }
        throw new RuntimeException("Профиль не найден");
    }

    @Transactional
    public void rejectByAdmin(UUID targetUserId, String adminKey, String reason) {
        assertAdminKey(adminKey);
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        WorkerProfile w = workerProfileRepository.findByUser(user).orElse(null);
        if (w != null) {
            rejectWorker(w, reason);
            return;
        }
        CustomerProfile c = customerProfileRepository.findByUser(user).orElse(null);
        if (c != null) {
            rejectCustomer(c, reason);
            return;
        }
        throw new RuntimeException("Профиль не найден");
    }
}
