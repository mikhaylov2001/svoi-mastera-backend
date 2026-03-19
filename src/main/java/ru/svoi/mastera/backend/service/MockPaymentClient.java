package ru.svoi.mastera.backend.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ru.svoi.mastera.backend.dto.YooKassaPaymentResponse;
import ru.svoi.mastera.backend.repository.PaymentClient;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class MockPaymentClient implements PaymentClient {

    @Override
    public YooKassaPaymentResponse createPayment(BigDecimal amount,
                                                 String currency,
                                                 String description,
                                                 String idempotenceKey) {
        YooKassaPaymentResponse resp = new YooKassaPaymentResponse();
        resp.setId("mock-" + UUID.randomUUID());
        resp.setStatus("pending");

        YooKassaPaymentResponse.Confirmation c = new YooKassaPaymentResponse.Confirmation();
        c.setType("redirect");
        c.setConfirmation_url("https://example.com/mock-payment?paymentId=" + resp.getId());

        resp.setConfirmation(c);
        return resp;
    }
}

