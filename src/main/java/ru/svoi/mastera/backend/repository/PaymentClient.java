package ru.svoi.mastera.backend.repository;

import ru.svoi.mastera.backend.dto.YooKassaPaymentResponse;

import java.math.BigDecimal;

public interface PaymentClient {

    YooKassaPaymentResponse createPayment(BigDecimal amount,
                                          String currency,
                                          String description,
                                          String idempotenceKey);
}
