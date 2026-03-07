package ru.svoi.mastera.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.svoi.mastera.backend.dto.PaymentInitDto;
import ru.svoi.mastera.backend.dto.YooKassaWebhookPayload;
import ru.svoi.mastera.backend.service.PaymentService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // инициировать оплату сделки
    @PostMapping("/initiate")
    public PaymentInitDto initiate(@RequestHeader("X-User-Id") UUID userId,
                                   @RequestParam UUID dealId) {
        return paymentService.initiatePayment(userId, dealId);
    }


}
