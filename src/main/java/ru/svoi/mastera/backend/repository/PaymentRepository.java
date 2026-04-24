package ru.svoi.mastera.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.svoi.mastera.backend.entity.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByProviderPaymentId(String providerPaymentId);
}
