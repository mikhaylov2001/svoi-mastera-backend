package ru.svoi.mastera.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svoi.mastera.backend.dto.CustomerStatsDto;
import ru.svoi.mastera.backend.dto.ReviewCreateDto;

import ru.svoi.mastera.backend.dto.ReviewDto;
import ru.svoi.mastera.backend.dto.WorkerStatsDto;
import ru.svoi.mastera.backend.entity.*;
import ru.svoi.mastera.backend.entity.enams.DealStatus;
import ru.svoi.mastera.backend.entity.enams.ReviewStatus;
import ru.svoi.mastera.backend.repository.DealRepository;
import ru.svoi.mastera.backend.repository.ReviewRepository;
import ru.svoi.mastera.backend.repository.WorkerProfileRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import ru.svoi.mastera.backend.repository.CustomerProfileRepository;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final DealRepository dealRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final CustomerProfileRepository customerProfileRepository;

    // Заказчик оставляет отзыв мастеру
    @Transactional
    public ReviewDto create(UUID authorUserId, UUID dealId, ReviewCreateDto dto) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        User authorUser = deal.getCustomer().getUser();

        if (!authorUser.getId().equals(authorUserId)) {
            throw new RuntimeException("You are not owner of this deal");
        }

        assertDealEligibleForReviews(deal);

        if (reviewRepository.existsCustomerReviewByDealId(dealId)) {
            throw new RuntimeException("Review for this deal already exists");
        }

        Review review = new Review();
        review.setDeal(deal);
        review.setAuthorUser(authorUser);
        review.setTargetWorker(deal.getWorker());
        review.setRating(dto.rating());
        review.setText(dto.text());
        review.setStatus(ru.svoi.mastera.backend.entity.enams.ReviewStatus.APPROVED);

        review = reviewRepository.save(review);
        return toDto(review);
    }

    // Мастер оставляет отзыв заказчику
    @Transactional
    public ReviewDto createByWorker(UUID authorUserId, UUID dealId, ReviewCreateDto dto) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        User authorUser = deal.getWorker().getUser();

        if (!authorUser.getId().equals(authorUserId)) {
            throw new RuntimeException("You are not the worker of this deal");
        }

        assertDealEligibleForReviews(deal);

        if (reviewRepository.existsWorkerReviewByDealId(dealId)) {
            throw new RuntimeException("Review for this deal already exists");
        }

        Review review = new Review();
        review.setDeal(deal);
        review.setAuthorUser(authorUser);
        review.setTargetWorker(null);
        review.setTargetCustomer(deal.getCustomer());
        review.setRating(dto.rating());
        review.setText(dto.text());
        review.setStatus(ru.svoi.mastera.backend.entity.enams.ReviewStatus.APPROVED);

        review = reviewRepository.save(review);
        return toDto(review);
    }

    /**
     * Отзыв допустим только когда сделка завершена и обе стороны нажали «Подтвердить выполнение»
     * ({@link DealStatus#COMPLETED} выставляется в {@link ru.svoi.mastera.backend.service.DealService#confirmDeal}).
     */
    private void assertDealEligibleForReviews(Deal deal) {
        if (deal.getStatus() != DealStatus.COMPLETED) {
            throw new RuntimeException("Отзыв можно оставить только после завершения сделки");
        }
        if (!deal.isCustomerConfirmed() || !deal.isWorkerConfirmed()) {
            throw new RuntimeException("Отзыв доступен только после подтверждения выполнения заказчиком и мастером");
        }
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> listByWorker(UUID workerUserId) {
        WorkerProfile worker = workerProfileRepository.findByUserId(workerUserId)
                .orElseThrow(() -> new RuntimeException("Worker profile not found"));

        List<Review> reviews = reviewRepository.findAllByTargetWorker(worker);
        return reviews.stream()
                .filter(ReviewService::isPubliclyVisibleReview)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> listByCustomer(UUID customerUserId) {
        CustomerProfile customer = customerProfileRepository.findByUserId(customerUserId)
                .orElseThrow(() -> new RuntimeException("Customer profile not found"));

        List<Review> reviews = reviewRepository.findAllByTargetCustomer(customer);
        return reviews.stream()
                .filter(ReviewService::isPubliclyVisibleReview)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /** Отзывы, учитываемые в рейтинге и в публичных списках. */
    static boolean isPubliclyVisibleReview(Review r) {
        ReviewStatus s = r.getStatus();
        return s == ReviewStatus.APPROVED || s == ReviewStatus.PUBLISHED;
    }

    @Transactional(readOnly = true)
    public CustomerStatsDto getCustomerStats(UUID customerUserId) {
        Optional<CustomerProfile> profile = customerProfileRepository.findByUserId(customerUserId);
        if (profile.isEmpty()) {
            return new CustomerStatsDto(0.0, 0L);
        }
        List<Review> reviews = reviewRepository.findAllByTargetCustomer(profile.get()).stream()
                .filter(ReviewService::isPubliclyVisibleReview)
                .collect(Collectors.toList());
        if (reviews.isEmpty()) {
            return new CustomerStatsDto(0.0, 0L);
        }
        double averageRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        averageRating = Math.round(averageRating * 10.0) / 10.0;
        return new CustomerStatsDto(averageRating, (long) reviews.size());
    }

    // Статистика мастера (рейтинг только по опубликованным отзывам)
    @Transactional(readOnly = true)
    public WorkerStatsDto getWorkerStats(UUID workerUserId) {
        Optional<WorkerProfile> opt = workerProfileRepository.findByUserId(workerUserId);
        if (opt.isEmpty()) {
            return new WorkerStatsDto(0.0, 0L, 0L, null, null, null, null, null, false);
        }
        WorkerProfile worker = opt.get();

        List<Review> reviews = reviewRepository.findAllByTargetWorker(worker).stream()
                .filter(ReviewService::isPubliclyVisibleReview)
                .collect(Collectors.toList());

        long completedWorks = dealRepository.findAllByWorker(worker)
                .stream()
                .filter(deal -> deal.getStatus() == ru.svoi.mastera.backend.entity.enams.DealStatus.COMPLETED)
                .count();

        Instant registeredAt = worker.getCreatedAt();

        if (reviews.isEmpty()) {
            return new WorkerStatsDto(
                    0.0, 0L, completedWorks, registeredAt,
                    worker.getDisplayName(),
                    worker.getLastName(),
                    worker.getUser() != null ? worker.getUser().getAvatarUrl() : null,
                    worker.getCity(),
                    worker.isVerified()
            );
        }

        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        averageRating = Math.round(averageRating * 10.0) / 10.0;

        return new WorkerStatsDto(
                averageRating, (long) reviews.size(), completedWorks, registeredAt,
                worker.getDisplayName(),
                worker.getLastName(),
                worker.getUser() != null ? worker.getUser().getAvatarUrl() : null,
                worker.getCity(),
                worker.isVerified()
        );
    }

    private ReviewDto toDto(Review review) {
        String authorName = "Клиент";
        String authorLastName = "";
        String authorAvatarUrl = null;
        if (review.getAuthorUser() != null) {
            authorAvatarUrl = review.getAuthorUser().getAvatarUrl();
            if (review.getAuthorUser().getCustomerProfile() != null) {
                authorName = review.getAuthorUser().getCustomerProfile().getDisplayName();
                authorLastName = review.getAuthorUser().getCustomerProfile().getLastName() != null
                        ? review.getAuthorUser().getCustomerProfile().getLastName() : "";
            } else if (review.getAuthorUser().getWorkerProfile() != null) {
                authorName = review.getAuthorUser().getWorkerProfile().getDisplayName();
                authorLastName = review.getAuthorUser().getWorkerProfile().getLastName() != null
                        ? review.getAuthorUser().getWorkerProfile().getLastName() : "";
            }
        }

        // Парсим бейджи из JSON строки
        List<String> badges = new ArrayList<>();
        if (review.getBadges() != null && !review.getBadges().isEmpty()) {
            try {
                // Простой парсинг JSON array: '["polite", "fast"]'
                String badgesStr = review.getBadges()
                        .replace("[", "")
                        .replace("]", "")
                        .replace("\"", "");
                if (!badgesStr.trim().isEmpty()) {
                    badges = Arrays.asList(badgesStr.split(","));
                    badges = badges.stream()
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(java.util.stream.Collectors.toList());
                }
            } catch (Exception e) {
                // Если парсинг не удался, badges остаётся пустым
            }
        }

        return new ReviewDto(
                review.getId(),
                review.getDeal().getId(),
                review.getAuthorUser().getId(),
                authorName,
                authorLastName,
                authorAvatarUrl,
                review.getTargetWorker() != null ? review.getTargetWorker().getUser().getId() : null,
                review.getTargetCustomer() != null ? review.getTargetCustomer().getUser().getId() : null,
                review.getRating(),
                review.getText(),
                review.getStatus() != null ? review.getStatus().name() : null,
                review.getCreatedAt(),
                badges
        );
    }
}