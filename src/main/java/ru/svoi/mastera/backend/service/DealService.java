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

import java.math.BigDecimal;
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
    private final ReviewRepository reviewRepository;
    private final NotificationService notificationService;
    private final ListingRepository listingRepository;
    private final CategoryRepository categoryRepository;

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

        // 🔔 Уведомление мастеру: заказчик принял отклик
        try {
            UUID workerUserId = offer.getWorker().getUser().getId();
            String customerName = jobRequest.getCustomer().getDisplayName();
            String customerLastName = jobRequest.getCustomer().getLastName();
            String jobTitle = jobRequest.getTitle();
            String price = offer.getPrice() != null ? offer.getPrice().toPlainString() : "договорная";
            notificationService.notifyOfferAccepted(workerUserId, customerName, customerLastName, jobTitle, price, deal.getId());
        } catch (Exception ignored) {}

        return toDto(deal);
    }

    @Transactional
    public DealDto acceptListing(UUID customerUserId, UUID listingId) {
        User customerUser = userRepository.findById(customerUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        CustomerProfile customer = customerProfileRepository.findByUser(customerUser)
                .orElseThrow(() -> new RuntimeException("Customer profile not found"));

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        if (!listing.isActive()) {
            throw new RuntimeException("Listing is not active");
        }
        if (listing.getWorker().getUser().getId().equals(customerUserId)) {
            throw new RuntimeException("You cannot accept your own listing");
        }

        Category category = null;
        if (listing.getCategory() != null && !listing.getCategory().isBlank()) {
            category = categoryRepository.findByNameIgnoreCase(listing.getCategory()).orElse(null);
        }
        if (category == null) {
            category = categoryRepository.findAllByActiveTrueOrderByNameAsc().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("No categories found"));
        }

        JobRequest jobRequest = new JobRequest();
        jobRequest.setCustomer(customer);
        jobRequest.setCategory(category);
        jobRequest.setTitle("Заявка по услуге: " + listing.getTitle());
        jobRequest.setDescription(
                (listing.getDescription() == null || listing.getDescription().isBlank())
                        ? "Клиент принял вашу услугу из объявления."
                        : listing.getDescription()
        );
        jobRequest.setCity("Йошкар-Ола");
        if (listing.getPrice() != null) {
            jobRequest.setBudgetTo(BigDecimal.valueOf(listing.getPrice()));
        }
        jobRequest.setPhotos(listing.getPhotos());
        jobRequest.setStatus(JobRequestStatus.IN_PROGRESS);
        jobRequest = jobRequestRepository.save(jobRequest);

        JobOffer offer = new JobOffer();
        offer.setJobRequest(jobRequest);
        offer.setWorker(listing.getWorker());
        offer.setMessage("Клиент принял работу по вашему объявлению");
        offer.setPrice(BigDecimal.valueOf(listing.getPrice() != null ? listing.getPrice() : 0));
        offer.setStatus(JobOfferStatus.ACCEPTED);
        offer = jobOfferRepository.save(offer);

        jobRequest.setSelectedOffer(offer);
        jobRequestRepository.save(jobRequest);

        Deal deal = new Deal();
        deal.setJobRequest(jobRequest);
        deal.setJobOffer(offer);
        deal.setCustomer(customer);
        deal.setWorker(listing.getWorker());
        deal.setAgreedPrice(offer.getPrice());
        // Статус NEW — ждём подтверждения мастера
        deal.setStatus(DealStatus.NEW);
        deal.setCustomerConfirmed(false);
        deal.setWorkerConfirmed(false);
        // Сохраняем ссылку на объявление для фронта
        deal.setListingId(listing.getId());
        deal = dealRepository.save(deal);

        try {
            notificationService.notifyDealConfirmed(
                    listing.getWorker().getUser().getId(),
                    customer.getDisplayName(),
                    jobRequest.getTitle(),
                    deal.getId()
            );
        } catch (Exception ignored) {}

        return toDto(deal);
    }

    /**
     * Мастер принимает новую сделку: NEW → IN_PROGRESS.
     * Вызывается мастером, когда он видит заявку и готов работать.
     */
    @Transactional
    public DealDto workerStartDeal(UUID workerUserId, UUID dealId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        if (deal.getStatus() != DealStatus.NEW) {
            throw new RuntimeException("Deal is not in NEW status");
        }

        UUID dealWorkerUserId = deal.getWorker().getUser().getId();
        if (!dealWorkerUserId.equals(workerUserId)) {
            throw new RuntimeException("You are not the worker of this deal");
        }

        deal.setStatus(DealStatus.IN_PROGRESS);
        deal.setStartedAt(Instant.now());
        deal = dealRepository.save(deal);

        // 🔔 Уведомляем заказчика что мастер принял заказ
        try {
            String workerName = deal.getWorker().getDisplayName();
            String jobTitle = deal.getJobRequest().getTitle();
            notificationService.notifyDealConfirmed(
                    deal.getCustomer().getUser().getId(),
                    workerName,
                    jobTitle,
                    deal.getId()
            );
        } catch (Exception ignored) {}

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

        // 🔔 Уведомления при подтверждении
        try {
            String jobTitle = deal.getJobRequest().getTitle();
            boolean isCustomer = userId.equals(deal.getCustomer().getUser().getId());

            if (deal.getStatus() == DealStatus.COMPLETED) {
                // Обе стороны подтвердили — сделка завершена
                notificationService.notifyDealCompleted(
                        deal.getCustomer().getUser().getId(),
                        deal.getWorker().getUser().getId(),
                        deal.getCustomer().getDisplayName(),
                        deal.getWorker().getDisplayName(),
                        jobTitle
                );
            } else {
                // Одна сторона подтвердила — уведомляем другую
                UUID targetId = isCustomer
                        ? deal.getWorker().getUser().getId()
                        : deal.getCustomer().getUser().getId();
                String confirmerName = isCustomer
                        ? deal.getCustomer().getDisplayName()
                        : deal.getWorker().getDisplayName();
                notificationService.notifyDealConfirmed(targetId, confirmerName, jobTitle, deal.getId());
            }
        } catch (Exception ignored) {}

        return toDto(deal);
    }

    // Keep old method name for backward compat
    @Transactional
    public DealDto completeDeal(UUID userId, UUID dealId) {
        return confirmDeal(userId, dealId);
    }

    private DealDto toDto(Deal deal) {
        String customerName = deal.getCustomer().getDisplayName();
        String customerLastName = deal.getCustomer().getLastName();
        String customerAvatar = deal.getCustomer().getUser() != null
                ? deal.getCustomer().getUser().getAvatarUrl() : null;
        String workerName   = deal.getWorker().getDisplayName();
        String workerLastName = deal.getWorker().getLastName();
        String workerAvatar   = deal.getWorker().getUser() != null
                ? deal.getWorker().getUser().getAvatarUrl() : null;
        String title = deal.getJobRequest().getTitle();
        String description = deal.getJobRequest().getDescription();
        String category = deal.getJobRequest().getCategory() != null
                ? deal.getJobRequest().getCategory().getName() : null;
        String[] photos = deal.getJobRequest().getPhotos();

        boolean hasReview = reviewRepository.existsCustomerReviewByDealId(deal.getId());
        boolean hasWorkerReview = reviewRepository.existsWorkerReviewByDealId(deal.getId());

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
                deal.getCompletedAt(),
                hasReview,
                hasWorkerReview,
                photos,
                workerAvatar,
                workerLastName,
                customerAvatar,
                customerLastName,
                deal.getListingId()
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
                customerFirstName,
                deal.getJobRequest().getPhotos()
        );
    }

}