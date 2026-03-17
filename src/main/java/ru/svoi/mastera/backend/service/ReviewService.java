package ru.svoi.mastera.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svoi.mastera.backend.dto.ReviewCreateDto;

import ru.svoi.mastera.backend.dto.ReviewDto;
import ru.svoi.mastera.backend.dto.WorkerStatsDto;
import ru.svoi.mastera.backend.entity.*;
import ru.svoi.mastera.backend.entity.enams.DealStatus;
import ru.svoi.mastera.backend.repository.DealRepository;
import ru.svoi.mastera.backend.repository.ReviewRepository;
import ru.svoi.mastera.backend.repository.WorkerProfileRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final DealRepository dealRepository;
    private final WorkerProfileRepository workerProfileRepository;

    @Transactional
    public ReviewDto create(UUID authorUserId, UUID dealId, ReviewCreateDto dto) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        User authorUser = deal.getCustomer().getUser();

        if (!authorUser.getId().equals(authorUserId)) {
            throw new RuntimeException("You are not owner of this deal");
        }

        if (deal.getStatus() != DealStatus.COMPLETED) {
            throw new RuntimeException("Deal must be completed before review");
        }

        Review review = new Review();
        review.setDeal(deal);
        review.setAuthorUser(authorUser);
        review.setTargetWorker(deal.getWorker());
        review.setRating(dto.rating());
        review.setText(dto.text());
        // status оставляем MODERATION по умолчанию

        review = reviewRepository.save(review);
        return toDto(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> listByWorker(UUID workerUserId) {
        WorkerProfile worker = workerProfileRepository.findByUserId(workerUserId)
                .orElseThrow(() -> new RuntimeException("Worker profile not found"));

        List<Review> reviews = reviewRepository.findAllByTargetWorker(worker);
        return reviews.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkerStatsDto getWorkerStats(UUID workerUserId) {
        WorkerProfile worker = workerProfileRepository.findByUserId(workerUserId)
                .orElseThrow(() -> new RuntimeException("Worker profile not found"));

        List<Review> reviews = reviewRepository.findAllByTargetWorker(worker);

        if (reviews.isEmpty()) {
            return new WorkerStatsDto(0.0, 0L);
        }

        // Вычисляем средний рейтинг
        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        // Округляем до 1 знака после запятой
        averageRating = Math.round(averageRating * 10.0) / 10.0;

        return new WorkerStatsDto(averageRating, (long) reviews.size());
    }

    private ReviewDto toDto(Review review) {
        // ✅ ИСПРАВЛЕНО: Получаем имя автора
        String authorName = "Клиент";
        if (review.getAuthorUser() != null) {
            if (review.getAuthorUser().getCustomerProfile() != null) {
                authorName = review.getAuthorUser().getCustomerProfile().getDisplayName();
            } else if (review.getAuthorUser().getWorkerProfile() != null) {
                authorName = review.getAuthorUser().getWorkerProfile().getDisplayName();
            }
        }

        return new ReviewDto(
                review.getId(),
                review.getDeal().getId(),
                review.getAuthorUser().getId(),
                authorName,  // ✅ Теперь переменная определена
                review.getTargetWorker().getUser().getId(),
                review.getRating(),
                review.getText(),
                review.getStatus() != null ? review.getStatus().name() : null,
                review.getCreatedAt()
        );
    }
}
