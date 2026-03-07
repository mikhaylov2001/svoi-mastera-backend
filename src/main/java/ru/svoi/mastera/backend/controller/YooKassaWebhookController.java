package ru.svoi.mastera.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.svoi.mastera.backend.dto.YooKassaWebhookPayload;
import ru.svoi.mastera.backend.service.PaymentService;

@RestController
@RequestMapping("/api/v1/yookassa")
@RequiredArgsConstructor
public class YooKassaWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/webhook")
    public void handleWebhook(@RequestBody YooKassaWebhookPayload payload) {
        paymentService.handleYooKassaWebhook(payload);
    }
}
