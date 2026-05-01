package ru.svoi.mastera.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svoi.mastera.backend.dto.GuaranteeAcceptDto;
import ru.svoi.mastera.backend.dto.GuaranteeMeDto;
import ru.svoi.mastera.backend.entity.CustomerProfile;
import ru.svoi.mastera.backend.entity.User;
import ru.svoi.mastera.backend.entity.WorkerProfile;
import ru.svoi.mastera.backend.repository.CustomerProfileRepository;
import ru.svoi.mastera.backend.repository.UserRepository;
import ru.svoi.mastera.backend.repository.WorkerProfileRepository;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GuaranteeTermsService {

    private static final ObjectMapper JSON = new ObjectMapper();

    /** Версия документа; при изменении текста условий — повысить и выставить новую страницу на фронте. */
    public static final String CONSENT_VERSION = "2026-05-02-guarantee-v1";

    private final UserRepository userRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final CustomerProfileRepository customerProfileRepository;

    @Transactional(readOnly = true)
    public GuaranteeMeDto getMe(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        WorkerProfile w = workerProfileRepository.findByUser(user).orElse(null);
        if (w != null) {
            return toDto(w.isVerified(), w.getGuaranteeTermsAcceptedAt());
        }
        CustomerProfile c = customerProfileRepository.findByUser(user).orElse(null);
        if (c != null) {
            return toDto(c.isVerified(), c.getGuaranteeTermsAcceptedAt());
        }
        throw new RuntimeException("Профиль не найден");
    }

    private GuaranteeMeDto toDto(boolean verified, Instant acceptedAt) {
        return new GuaranteeMeDto(
                verified,
                acceptedAt != null,
                acceptedAt,
                acceptedAt != null ? CONSENT_VERSION : null
        );
    }

    @Transactional
    public GuaranteeMeDto accept(UUID userId, GuaranteeAcceptDto dto) {
        validateClauses(dto);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        WorkerProfile worker = workerProfileRepository.findByUser(user).orElse(null);
        if (worker != null) {
            return acceptWorker(worker);
        }
        CustomerProfile customer = customerProfileRepository.findByUser(user).orElse(null);
        if (customer != null) {
            return acceptCustomer(customer);
        }
        throw new RuntimeException("Профиль не найден");
    }

    private GuaranteeMeDto acceptWorker(WorkerProfile w) {
        if (!w.isVerified()) {
            throw new RuntimeException("Согласие с условиями программы гарантии доступно только после успешной верификации профиля.");
        }
        if (w.getGuaranteeTermsAcceptedAt() != null) {
            return toDto(true, w.getGuaranteeTermsAcceptedAt());
        }
        w.setGuaranteeTermsAcceptedAt(Instant.now());
        w.setGuaranteeTermsConsentJson(buildConsentSnapshot());
        workerProfileRepository.save(w);
        return toDto(true, w.getGuaranteeTermsAcceptedAt());
    }

    private GuaranteeMeDto acceptCustomer(CustomerProfile c) {
        if (!c.isVerified()) {
            throw new RuntimeException("Согласие с условиями программы гарантии доступно только после успешной верификации профиля.");
        }
        if (c.getGuaranteeTermsAcceptedAt() != null) {
            return toDto(true, c.getGuaranteeTermsAcceptedAt());
        }
        c.setGuaranteeTermsAcceptedAt(Instant.now());
        c.setGuaranteeTermsConsentJson(buildConsentSnapshot());
        customerProfileRepository.save(c);
        return toDto(true, c.getGuaranteeTermsAcceptedAt());
    }

    private void validateClauses(GuaranteeAcceptDto dto) {
        if (dto == null) {
            throw new RuntimeException("Передайте отметки согласия по всем пунктам.");
        }
        requireTrue(dto.getAcceptDealProcedure(), "Не отмечено согласие с порядком заключения и исполнения сделки.");
        requireTrue(dto.getAcceptPaymentSettlement(), "Не отмечено согласие с правилами расчётов.");
        requireTrue(dto.getAcceptDisputeResolution(), "Не отмечено согласие с порядком разрешения споров.");
        requireTrue(dto.getAcceptOperatorDisclaimer(), "Не отмечено согласие с разделом о роли оператора платформы.");
        requireTrue(dto.getAcceptPersonalDeclarations(), "Не подтверждены заявления о достоверности данных.");
    }

    private static void requireTrue(Boolean v, String msg) {
        if (!Boolean.TRUE.equals(v)) {
            throw new RuntimeException(msg);
        }
    }

    private String buildConsentSnapshot() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("consentVersion", CONSENT_VERSION);
        map.put("acceptedAt", Instant.now().toString());
        map.put("clauses", new String[]{
                "deal_procedure",
                "payment_settlement",
                "dispute_resolution",
                "operator_disclaimer",
                "personal_declarations"
        });
        try {
            return JSON.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка сохранения согласия");
        }
    }
}
