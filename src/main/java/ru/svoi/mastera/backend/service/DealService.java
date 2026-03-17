package ru.svoi.mastera.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svoi.mastera.backend.dto.DealDto;
import ru.svoi.mastera.backend.dto.WorkerCompletedWorkDto;
import ru.svoi.mastera.backend.entity.*;
import ru.svoi.mastera.backend.entity.enams.DealStatus;
import ru.svoi.mastera.backend.entity.enams.JobOfferStatus;
import ru.svoi.mastera.backend.entity.enams.JobRequestStatus;
import ru.svoi.mastera.backend.repository.*;

import java.time.Instant;
import java.util.ArrayList;
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

        if (!jobRequest.getCustomer().getUser().getId().equals(customerUserId)) {
            throw new RuntimeException("You are not owner of this job request");
        }

        jobRequest.setStatus(JobRequestStatus.IN_PROGRESS);
        offer.setStatus(JobOfferStatus.ACCEPTED);
        jobRequest.setSelectedOffer(offer);

        Deal deal = new Deal();
        deal.setJobRequest(jobRequest);
        deal.setJobOffer(offer);
        deal.setCustomer(jobRequest.getCustomer());
        deal.setWorker(offer.getWorker());
        deal.setAgreedPrice(offer.getPrice());
        deal.setStatus(DealStatus.IN_PROGRESS);
        deal.setStartedAt(Instant.now());
        deal.setCustomerConfirmed(false);
        deal.setWorkerConfirmed(false);

        deal = dealRepository.save(deal);
        return toDto(deal);
    }

    @Transactional(readOnly = true)
    public List<DealDto> listMyDeals(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Deal> deals = new ArrayList<>();

        // As customer
        customerProfileRepository.findByUser(user).ifPresent(customer -> {
            deals.addAll(dealRepository.findAllByCustomer(customer));
        });

        // As worker
        workerProfileRepository.findByUser(user).ifPresent(worker -> {
            List<Deal> workerDeals = dealRepository.findAllByWorker(worker);
            for (Deal d : workerDeals) {
                if (deals.stream().noneMatch(existing -> existing.getId().equals(d.getId()))) {
                    deals.add(d);
                }
            }
        });

        // Sort by createdAt desc
        deals.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        return deals.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DealDto getById(UUID dealId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
        return toDto(deal);
    }

    @Transactional
    public DealDto confirmDeal(UUID userId, UUID dealId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        if (deal.getStatus() != DealStatus.IN_PROGRESS) {
            throw new RuntimeException("Deal is not in progress");
        }

        UUID customerUserId = deal.getCustomer().getUser().getId();
        UUID workerUserId = deal.getWorker().getUser().getId();

        if (userId.equals(customerUserId)) {
            deal.setCustomerConfirmed(true);
        } else if (userId.equals(workerUserId)) {
            deal.setWorkerConfirmed(true);
        } else {
            throw new RuntimeException("You are not part of this deal");
        }

        // Both confirmed -> complete
        if (deal.isCustomerConfirmed() && deal.isWorkerConfirmed()) {
            deal.setStatus(DealStatus.COMPLETED);
            deal.setCompletedAt(Instant.now());
            // Synchronize related job request status so frontend и заказчик видят завершение
            if (deal.getJobRequest() != null) {
                deal.getJobRequest().setStatus(JobRequestStatus.COMPLETED);
            }
        }

        deal = dealRepository.save(deal);
        return toDto(deal);
    }

    // Keep old method name for backward compat
    @Transactional
    public DealDto completeDeal(UUID userId, UUID dealId) {
        return confirmDeal(userId, dealId);
    }

    private DealDto toDto(Deal deal) {
        String customerName = deal.getCustomer().getDisplayName();
        String workerName = deal.getWorker().getDisplayName();
        String title = deal.getJobRequest().getTitle();
        String description = deal.getJobRequest().getDescription();
        String category = deal.getJobRequest().getCategory() != null
                ? deal.getJobRequest().getCategory().getName() : null;

        return new DealDto(
                deal.getId(),
                deal.getJobRequest().getId(),
                deal.getJobOffer().getId(),
                deal.getCustomer().getUser().getId(),
                deal.getWorker().getUser().getId(),
                customerName,
                workerName,
                title,
                description,
                category,
                deal.getAgreedPrice(),
                deal.getStatus() != null ? deal.getStatus().name() : null,
                deal.isCustomerConfirmed(),
                deal.isWorkerConfirmed(),
                deal.getCreatedAt(),
                deal.getStartedAt(),
                deal.getCompletedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<WorkerCompletedWorkDto> getWorkerCompletedWorks(UUID workerUserId) {
        WorkerProfile worker = workerProfileRepository.findByUserId(workerUserId)
                .orElseThrow(() -> new RuntimeException("Worker profile not found"));

        List<Deal> completedDeals = dealRepository.findAllByWorker(worker)
                .stream()
                .filter(deal -> deal.getStatus() == DealStatus.COMPLETED)
                .sorted((a, b) -> b.getCompletedAt().compareTo(a.getCompletedAt())) // Новые сначала
                .collect(java.util.stream.Collectors.toList());

        return completedDeals.stream()
                .map(this::toCompletedWorkDto)
                .collect(java.util.stream.Collectors.toList());
    }

    private WorkerCompletedWorkDto toCompletedWorkDto(Deal deal) {
        String title = deal.getJobRequest().getTitle();
        String description = deal.getJobRequest().getDescription();
        String categoryName = deal.getJobRequest().getCategory() != null
                ? deal.getJobRequest().getCategory().getName() : null;

        // Только имя клиента без фамилии для приватности
        String customerFullName = deal.getCustomer().getDisplayName();
        String customerFirstName = customerFullName != null && customerFullName.contains(" ")
                ? customerFullName.split(" ")[0]
                : customerFullName;

        return new WorkerCompletedWorkDto(
                deal.getId(),
                title,
                description,
                categoryName,
                deal.getAgreedPrice(),
                deal.getCompletedAt(),
                customerFirstName
        );
    }

}
