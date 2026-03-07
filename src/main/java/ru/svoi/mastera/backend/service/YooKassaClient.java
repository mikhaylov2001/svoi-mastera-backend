package ru.svoi.mastera.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.svoi.mastera.backend.config.YooKassaProperties;
import ru.svoi.mastera.backend.dto.YooKassaPaymentResponse;
import ru.svoi.mastera.backend.repository.PaymentClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
@Profile("prod")
@RequiredArgsConstructor
public class YooKassaClient implements PaymentClient {

    private final YooKassaProperties props;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public YooKassaPaymentResponse createPayment(BigDecimal amount,
                                                 String currency,
                                                 String description,
                                                 String idempotenceKey) {
        String url = "https://api.yookassa.ru/v3/payments";

        Map<String, Object> body = Map.of(
                "amount", Map.of(
                        "value", amount.setScale(2, RoundingMode.HALF_UP).toString(),
                        "currency", currency
                ),
                "capture", true,
                "confirmation", Map.of(
                        "type", "redirect",
                        "return_url", props.getReturnUrl()
                ),
                "description", description
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String basic = Base64.getEncoder()
                .encodeToString((props.getShopId() + ":" + props.getSecretKey())
                        .getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + basic);
        headers.set("Idempotence-Key", idempotenceKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<YooKassaPaymentResponse> response = restTemplate
                .postForEntity(url, entity, YooKassaPaymentResponse.class);

        return response.getBody();
    }
}

