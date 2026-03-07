package ru.svoi.mastera.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svoi.mastera.backend.dto.DealDto;
import ru.svoi.mastera.backend.entity.*;
import ru.svoi.mastera.backend.entity.enams.DealStatus;
import ru.svoi.mastera.backend.entity.enams.JobOfferStatus;
import ru.svoi.mastera.backend.entity.enams.JobRequestStatus;
import ru.svoi.mastera.backend.repository.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DealService {
    private final DealRepository dealRepository;
    private final JobRequestRepository jobRequestRepository;
    private final JobOfferRepository jobOfferRepository;
    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final WorkerProfileRepository workerProfileRepository;

    @Transactional
    public DealDto acceptOffer(UUID customerUserId, UUID jobRequestId, UUID offerId) {
        User customerUser = userRepository.findById(customerUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        JobRequest jobRequest = jobRequestRepository.findById(jobRequestId)
                .orElseThrow(() -> new RuntimeException("Job request not found"));

        JobOffer offer = jobOfferRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));

        if (!offer.getJobRequest().getId().equals(jobRequest.getId())) {
            throw new RuntimeException("Offer does not belong to this job request");
        }

        // Проверяем, что текущий пользователь — владелец заявки
        if (!jobRequest.getCustomer().getUser().getId().equals(customerUserId)) {
            throw new RuntimeException("You are not owner of this job request");
        }

        // обновляем статусы
        jobRequest.setStatus(JobRequestStatus.IN_PROGRESS); // или свой статус из enum
        offer.setStatus(JobOfferStatus.ACCEPTED);
        jobRequest.setSelectedOffer(offer);

        // создаём сделку
        Deal deal = new Deal();
        deal.setJobRequest(jobRequest);
        deal.setJobOffer(offer);
        deal.setCustomer(jobRequest.getCustomer());   // CustomerProfile
        deal.setWorker(offer.getWorker());           // WorkerProfile
        deal.setAgreedPrice(offer.getPrice());
        deal.setStatus(DealStatus.NEW);
        deal.setStartedAt(Instant.now());

        deal = dealRepository.save(deal);

        return toDto(deal);
    }

    @Transactional(readOnly = true)
    public List<DealDto> listMyDeals(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // для простоты считаем, что пользователь сейчас клиент
        CustomerProfile customer = customerProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Customer profile not found"));

        List<Deal> deals = dealRepository.findAllByCustomer(customer);
        return deals.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DealDto getById(UUID dealId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
        return toDto(deal);
    }

    private DealDto toDto(Deal deal) {
        return new DealDto(
                deal.getId(),
                deal.getJobRequest().getId(),
                deal.getJobOffer().getId(),
                deal.getCustomer().getUser().getId(), // id пользователя-клиента
                deal.getWorker().getUser().getId(),   // id пользователя-мастера
                deal.getAgreedPrice(),
                deal.getStatus() != null ? deal.getStatus().name() : null,
                deal.getCreatedAt(),
                deal.getStartedAt(),
                deal.getCompletedAt()
        );
    }


    public DealDto completeDeal(UUID userId, UUID dealId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        if (!deal.getCustomer().getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not owner of this deal");
        }
        if (deal.getStatus() != DealStatus.NEW && deal.getStatus() != DealStatus.IN_PROGRESS) {
            throw new RuntimeException("Deal cannot be completed in this status");
        }

        deal.setStatus(DealStatus.COMPLETED);
        deal.setCompletedAt(Instant.now());

        deal = dealRepository.save(deal);
        return toDto(deal);
    }
}
