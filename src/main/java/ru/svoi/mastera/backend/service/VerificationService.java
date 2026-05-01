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
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private static final ObjectMapper JSON = new ObjectMapper();

    /** Версия согласия: тест по правилам + чекбокс (без документов и ЭП). */
    private static final String CONSENT_VERSION = "2026-05-01-quiz-rules";

    /**
     * Правильные индексы вариантов по порядку вопросов (синхронно с {@code verificationQuiz.js} на фронте).
     */
    private static final int[] QUIZ_CORRECT_OPTION_INDEX = {1, 0, 2, 1, 1, 2};

    private final UserRepository userRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final CustomerProfileRepository customerProfileRepository;

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
        w.setVerificationDocumentsJson(writeQuizJson(dto.getQuizAnswers()));
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
        c.setVerificationDocumentsJson(writeQuizJson(dto.getQuizAnswers()));
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
        List<Integer> answers = dto.getQuizAnswers();
        if (answers == null || answers.size() != QUIZ_CORRECT_OPTION_INDEX.length) {
            throw new RuntimeException("Ответьте на все вопросы теста.");
        }
        for (int i = 0; i < QUIZ_CORRECT_OPTION_INDEX.length; i++) {
            Integer a = answers.get(i);
            if (a == null || a < 0 || a > 2 || !a.equals(QUIZ_CORRECT_OPTION_INDEX[i])) {
                throw new RuntimeException("В тесте есть неверные ответы. Ознакомьтесь с правилами общения на платформе и попробуйте снова.");
            }
        }
        VerificationSignaturePayloadDto sig = dto.getSignature();
        if (sig == null) {
            throw new RuntimeException("Заполните все поля формы: данные о себе и согласие с правилами.");
        }
        if (sig.getFullLegalName() == null || sig.getFullLegalName().trim().length() < 3) {
            throw new RuntimeException("Укажите полное ФИО.");
        }
        validateBirthDate(sig.getBirthDate());
        if (sig.getResidence() == null || sig.getResidence().trim().length() < 3) {
            throw new RuntimeException("Укажите населённый пункт или регион проживания (не короче 3 символов).");
        }
        if (sig.getAgreementAccepted() == null || !sig.getAgreementAccepted()) {
            throw new RuntimeException("Нужно подтвердить согласие с правилами платформы (отметьте галочку).");
        }
        if (sig.getSignatureImageUrl() != null && !sig.getSignatureImageUrl().isBlank()) {
            if (!urlBelongsToUser(userId, sig.getSignatureImageUrl())) {
                throw new RuntimeException("Недопустимый файл подписи.");
            }
        }
    }

    private void validateBirthDate(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new RuntimeException("Укажите дату рождения.");
        }
        LocalDate birth;
        try {
            birth = LocalDate.parse(raw.trim());
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Некорректная дата рождения.");
        }
        LocalDate today = LocalDate.now();
        if (!birth.isBefore(today)) {
            throw new RuntimeException("Дата рождения должна быть в прошлом.");
        }
        int age = Period.between(birth, today).getYears();
        if (age < 14) {
            throw new RuntimeException("Для использования платформы возраст должен быть не менее 14 лет.");
        }
        if (age > 120) {
            throw new RuntimeException("Проверьте корректность даты рождения.");
        }
    }

    private boolean urlBelongsToUser(UUID userId, String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        String needle = "/api/v1/files/" + userId + "/";
        return url.contains(needle);
    }

    private String writeQuizJson(List<Integer> answers) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("kind", "quiz-rules-v1");
        map.put("answers", answers != null ? new ArrayList<>(answers) : List.of());
        map.put("recordedAt", Instant.now().toString());
        try {
            return JSON.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка сохранения результата теста");
        }
    }

    private String buildSignatureJson(VerificationSignaturePayloadDto sig) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("fullLegalName", sig.getFullLegalName().trim());
        map.put("birthDate", sig.getBirthDate().trim());
        map.put("residence", sig.getResidence().trim());
        map.put("agreementAccepted", Boolean.TRUE.equals(sig.getAgreementAccepted()));
        map.put("consentVersion", CONSENT_VERSION);
        map.put("rulesAccepted", Boolean.TRUE);
        if (sig.getSignatureImageUrl() != null && !sig.getSignatureImageUrl().isBlank()) {
            map.put("signatureImageUrl", sig.getSignatureImageUrl());
        }
        map.put("serverRecordedAt", Instant.now().toString());
        try {
            return JSON.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка сохранения согласия");
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
