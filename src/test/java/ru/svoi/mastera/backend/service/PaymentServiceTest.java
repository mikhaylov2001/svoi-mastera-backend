package ru.svoi.mastera.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.svoi.mastera.backend.dto.PaymentInitDto;
import ru.svoi.mastera.backend.dto.YooKassaWebhookPayload;
import ru.svoi.mastera.backend.entity.Deal;
import ru.svoi.mastera.backend.entity.Payment;
import ru.svoi.mastera.backend.entity.User;
import ru.svoi.mastera.backend.entity.CustomerProfile;
import ru.svoi.mastera.backend.entity.WorkerProfile;
import ru.svoi.mastera.backend.entity.enams.DealStatus;
import ru.svoi.mastera.backend.entity.enams.PaymentProvider;
import ru.svoi.mastera.backend.entity.enams.PaymentStatus;
import ru.svoi.mastera.backend.entity.enams.PaymentType;
import ru.svoi.mastera.backend.repository.DealRepository;
import ru.svoi.mastera.backend.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private DealRepository dealRepository;
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void initiatePaymentCreatesPaymentAndMovesDealInProgress() {
        UUID customerId = UUID.randomUUID();
        UUID dealId = UUID.randomUUID();

        User user = new User(); user.setId(customerId);
        CustomerProfile customer = new CustomerProfile(); customer.setUser(user);

        User workerUser = new User(); workerUser.setId(UUID.randomUUID());
        WorkerProfile worker = new WorkerProfile(); worker.setUser(workerUser);

        Deal deal = new Deal();
        deal.setId(dealId);
        deal.setCustomer(customer);
        deal.setWorker(worker);
        deal.setStatus(DealStatus.NEW);
        deal.setAgreedPrice(new BigDecimal("1000"));

        when(dealRepository.findById(dealId)).thenReturn(Optional.of(deal));
        when(dealRepository.save(any(Deal.class))).thenAnswer(i -> i.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        PaymentInitDto dto = paymentService.initiatePayment(customerId, dealId);

        assertThat(dto.paymentId()).isNotNull();
        assertThat(deal.getStatus()).isEqualTo(DealStatus.IN_PROGRESS);

        verify(paymentRepository, atLeast(2)).save(any(Payment.class));
    }

    @Test
    void mockSucceedPaymentUpdatesStatus() {
        Deal deal = new Deal(); deal.setStatus(DealStatus.NEW);
        Payment payment = new Payment(); payment.setDeal(deal);

        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
        when(dealRepository.save(any(Deal.class))).thenAnswer(i -> i.getArgument(0));

        paymentService.mockSucceedPayment(payment);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(deal.getStatus()).isEqualTo(DealStatus.IN_PROGRESS);
    }

    @Test
    void handleYooKassaWebhookThrowsForMismatchAmount() {
        UUID partnerId = UUID.randomUUID();
        Deal deal = new Deal(); deal.setStatus(DealStatus.NEW);

        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setAmount(new BigDecimal("1000"));
        payment.setDeal(deal);

        when(paymentRepository.findByProviderPaymentId("pid123")).thenReturn(Optional.of(payment));

        YooKassaWebhookPayload payload = new YooKassaWebhookPayload();
        payload.setEvent("payment.succeeded");
        YooKassaWebhookPayload.ObjectWrapper object = new YooKassaWebhookPayload.ObjectWrapper();
        object.setId("pid123");
        YooKassaWebhookPayload.ObjectWrapper.Amount amt = new YooKassaWebhookPayload.ObjectWrapper.Amount();
        amt.setValue("500");
        object.setAmount(amt);
        payload.setObject(object);

        assertThrows(RuntimeException.class, () -> paymentService.handleYooKassaWebhook(payload));
    }
}
