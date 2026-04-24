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
    private final PaymentRepository paymentRepository;

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
            // Уведомление мастеру: новый заказ ждёт принятия
            notificationService.notifyDealNew(
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
            notificationService.notifyDealStarted(
                    deal.getCustomer().getUser().getId(),
                    deal.getWorker().getDisplayName(),
                    deal.getJobRequest().getTitle(),
                    deal.getId()
            );
        } catch (Exception ignored) {}

        return toDto(deal);
    }

    /**
     * Отмена активной сделки (IN_PROGRESS) — обе стороны могут отменить,
     * пока работа не завершена (COMPLETED / AWAITING_PAYMENT).
     */
    @Transactional
    public DealDto cancelActiveDeal(UUID userId, UUID dealId, String reason) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        if (deal.getStatus() != DealStatus.IN_PROGRESS) {
            throw new RuntimeException("Only active (IN_PROGRESS) deals can be cancelled this way");
        }

        UUID customerUserId = deal.getCustomer().getUser().getId();
        UUID workerUserId   = deal.getWorker().getUser().getId();
        if (!userId.equals(customerUserId) && !userId.equals(workerUserId)) {
            throw new RuntimeException("You are not part of this deal");
        }

        boolean isCustomer = userId.equals(customerUserId);

        deal.setStatus(DealStatus.CANCELLED);
        deal.setCancelledAt(Instant.now());
        String r = (reason == null || reason.isBlank())
                ? (isCustomer ? "Отменено заказчиком" : "Отменено мастером")
                : reason.trim();
        if (r.length() > 1000) r = r.substring(0, 1000);
        deal.setCancellationReason(r);

        if (deal.getJobRequest() != null) {
            deal.getJobRequest().setStatus(JobRequestStatus.CANCELLED);
        }

        deal = dealRepository.save(deal);

        // 🔔 Уведомления обеим сторонам
        try {
            String cancellerName = isCustomer
                    ? deal.getCustomer().getDisplayName()
                    : deal.getWorker().getDisplayName();
            UUID otherUserId = isCustomer ? workerUserId : customerUserId;
            notificationService.notifyDealCancelled(
                    userId, otherUserId, cancellerName,
                    deal.getJobRequest().getTitle(), isCustomer);
        } catch (Exception ignored) {}

        return toDto(deal);
    }

    /**
     * Отмена сделки в статусе NEW — заказчик передумал или мастер отказался.
     * Доступно обеим сторонам, пока заказ не принят в работу.
     */
    @Transactional
    public DealDto cancelPendingDeal(UUID userId, UUID dealId, String reason) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        if (deal.getStatus() != DealStatus.NEW) {
            throw new RuntimeException("Only pending (NEW) deals can be cancelled");
        }

        UUID customerUserId = deal.getCustomer().getUser().getId();
        UUID workerUserId = deal.getWorker().getUser().getId();
        if (!userId.equals(customerUserId) && !userId.equals(workerUserId)) {
            throw new RuntimeException("You are not part of this deal");
        }

        boolean isCustomer = userId.equals(customerUserId);

        deal.setStatus(DealStatus.CANCELLED);
        deal.setCancelledAt(Instant.now());
        String r = (reason == null || reason.isBlank())
                ? (isCustomer ? "Отменено заказчиком" : "Отклонено мастером")
                : reason.trim();
        if (r.length() > 1000) {
            r = r.substring(0, 1000);
        }
        deal.setCancellationReason(r);

        if (deal.getJobRequest() != null) {
            deal.getJobRequest().setStatus(JobRequestStatus.CANCELLED);
        }
        if (deal.getJobOffer() != null) {
            deal.getJobOffer().setStatus(isCustomer ? JobOfferStatus.WITHDRAWN : JobOfferStatus.REJECTED);
        }

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

        String jobTitle = deal.getJobRequest() != null ? deal.getJobRequest().getTitle() : "Задача";
        boolean isCustomer = userId.equals(customerUserId);

        // Оба подтвердили → ждём оплату от заказчика
        if (deal.isCustomerConfirmed() && deal.isWorkerConfirmed()) {
            deal.setStatus(DealStatus.AWAITING_PAYMENT);
        }

        deal = dealRepository.save(deal);

        // 🔔 Уведомления при подтверждении
        try {
            if (deal.getStatus() == DealStatus.AWAITING_PAYMENT) {
                // Обе стороны подтвердили — просим заказчика оплатить
                String amount = deal.getAgreedPrice() != null
                        ? deal.getAgreedPrice().toPlainString() : "договорная";
                notificationService.notifyPaymentRequired(
                        customerUserId,
                        deal.getWorker().getDisplayName(),
                        jobTitle, amount, deal.getId()
                );
                notificationService.notifyCustomerConfirmed(
                        workerUserId,
                        deal.getCustomer().getDisplayName(),
                        jobTitle
                );
            } else if (isCustomer) {
                // Только заказчик подтвердил → уведомляем мастера
                notificationService.notifyDealConfirmed(workerUserId,
                        deal.getCustomer().getDisplayName(), jobTitle, deal.getId());
            } else {
                // Только мастер подтвердил → уведомляем заказчика
                notificationService.notifyWorkerConfirmed(customerUserId,
                        deal.getWorker().getDisplayName(), jobTitle);
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

        // Статус последнего платежа
        String paymentStatus = paymentRepository
                .findFirstByDealIdOrderByCreatedAtDesc(deal.getId())
                .map(p -> p.getStatus().name())
                .orElse(null);

        DealDto dto = new DealDto();
        dto.setId(deal.getId());
        dto.setJobRequestId(deal.getJobRequest().getId());
        dto.setJobOfferId(deal.getJobOffer().getId());
        dto.setCustomerId(deal.getCustomer().getUser().getId());
        dto.setWorkerId(deal.getWorker().getUser().getId());
        dto.setCustomerName(customerName);
        dto.setWorkerName(workerName);
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setCategory(category);
        dto.setAgreedPrice(deal.getAgreedPrice());
        dto.setStatus(deal.getStatus() != null ? deal.getStatus().name() : null);
        dto.setCustomerConfirmed(deal.isCustomerConfirmed());
        dto.setWorkerConfirmed(deal.isWorkerConfirmed());
        dto.setCreatedAt(deal.getCreatedAt());
        dto.setStartedAt(deal.getStartedAt());
        dto.setCompletedAt(deal.getCompletedAt());
        dto.setHasReview(hasReview);
        dto.setHasWorkerReview(hasWorkerReview);
        dto.setPhotos(photos);
        dto.setWorkerAvatar(workerAvatar);
        dto.setWorkerLastName(workerLastName);
        dto.setCustomerAvatar(customerAvatar);
        dto.setCustomerLastName(customerLastName);
        dto.setListingId(deal.getListingId());
        dto.setPaymentStatus(paymentStatus);
        dto.setCancellationReason(deal.getCancellationReason());
        return dto;
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