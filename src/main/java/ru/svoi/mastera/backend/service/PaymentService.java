package ru.svoi.mastera.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svoi.mastera.backend.dto.PaymentInitDto;
import ru.svoi.mastera.backend.dto.YooKassaWebhookPayload;
import ru.svoi.mastera.backend.entity.Deal;
import ru.svoi.mastera.backend.entity.Payment;
import ru.svoi.mastera.backend.entity.enams.DealStatus;
import ru.svoi.mastera.backend.entity.enams.JobRequestStatus;
import ru.svoi.mastera.backend.entity.enams.PaymentProvider;
import ru.svoi.mastera.backend.entity.enams.PaymentStatus;
import ru.svoi.mastera.backend.entity.enams.PaymentType;
import ru.svoi.mastera.backend.repository.DealRepository;
import ru.svoi.mastera.backend.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final DealRepository dealRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;

    private static final BigDecimal FEE_PERCENT = new BigDecimal("0.05");

    @Transactional
    public PaymentInitDto initiatePayment(UUID customerUserId, UUID dealId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        if (!deal.getCustomer().getUser().getId().equals(customerUserId)) {
            throw new RuntimeException("You are not owner of this deal");
        }
        if (deal.getStatus() != DealStatus.AWAITING_PAYMENT && deal.getStatus() != DealStatus.IN_PROGRESS) {
            throw new RuntimeException("Deal is not ready for payment");
        }

        BigDecimal amount = deal.getAgreedPrice();
        BigDecimal fee = amount.multiply(FEE_PERCENT);
        BigDecimal payout = amount.subtract(fee);

        // сохраняем комиссию и выплату в Deal
        deal.setPlatformFee(fee);
        deal.setPayoutAmount(payout);
        dealRepository.save(deal);

        // создаём локальный Payment
        Payment payment = new Payment();
        payment.setDeal(deal);
        payment.setAmount(amount);
        payment.setCurrency("RUB");
        payment.setProvider(PaymentProvider.MOCK); // мок-провайдер
        payment.setStatus(PaymentStatus.CREATED);
        payment.setType(PaymentType.FULL);

        payment = paymentRepository.save(payment);

        // имитируем, что клиент «оплатил», без внешней платёжки
        mockSucceedPayment(payment);

        // confirmationUrl не нужен — фронт никуда не редиректит
        return new PaymentInitDto(payment.getId(), null);
    }

    @Transactional
    public void mockSucceedPayment(Payment payment) {
        payment.setStatus(PaymentStatus.SUCCEEDED);
        payment.setPaidAt(Instant.now());
        paymentRepository.save(payment);

        Deal deal = payment.getDeal();
        // Завершаем сделку после успешной оплаты
        if (deal.getStatus() == DealStatus.AWAITING_PAYMENT || deal.getStatus() == DealStatus.NEW) {
            deal.setStatus(DealStatus.COMPLETED);
            deal.setCompletedAt(Instant.now());
            if (deal.getJobRequest() != null) {
                deal.getJobRequest().setStatus(JobRequestStatus.COMPLETED);
            }
            dealRepository.save(deal);

            // 🔔 Уведомляем обе стороны
            try {
                String jobTitle = deal.getJobRequest() != null ? deal.getJobRequest().getTitle() : "Задача";
                String amount = deal.getAgreedPrice() != null ? deal.getAgreedPrice().toPlainString() : "0";
                notificationService.notifyPaymentDone(
                        deal.getCustomer().getUser().getId(),
                        deal.getWorker().getUser().getId(),
                        deal.getCustomer().getDisplayName(),
                        deal.getWorker().getDisplayName(),
                        jobTitle, amount
                );
            } catch (Exception ignored) {}
        }
    }

    @Transactional
    public void handleYooKassaWebhook(YooKassaWebhookPayload payload) {
        String event = payload.getEvent(); // например, "payment.succeeded"
        YooKassaWebhookPayload.ObjectWrapper object = payload.getObject();
        String providerPaymentId = object.getId();

        Payment payment = paymentRepository.findByProviderPaymentId(providerPaymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        YooKassaWebhookPayload.ObjectWrapper.Amount amountObj = object.getAmount();
        if (amountObj != null) {
            BigDecimal receivedAmount = new BigDecimal(amountObj.getValue());
            if (payment.getAmount().compareTo(receivedAmount) != 0) {
                throw new RuntimeException("Amount mismatch");
            }
        }

        if ("payment.succeeded".equals(event)) {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            payment.setPaidAt(Instant.now());

            Deal deal = payment.getDeal();
            if (deal.getStatus() == DealStatus.NEW) {
                deal.setStatus(DealStatus.IN_PROGRESS);
                dealRepository.save(deal);
            }
        } else if ("payment.canceled".equals(event)) {
            payment.setStatus(PaymentStatus.FAILED);
        }

        paymentRepository.save(payment);
    }
}
