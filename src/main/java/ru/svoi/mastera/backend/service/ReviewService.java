package ru.svoi.mastera.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svoi.mastera.backend.dto.ReviewCreateDto;

import ru.svoi.mastera.backend.dto.ReviewDto;
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

    private ReviewDto toDto(Review review) {
        return new ReviewDto(
                review.getId(),
                review.getDeal().getId(),
                review.getAuthorUser().getId(),
                review.getTargetWorker().getUser().getId(),
                review.getRating(),
                review.getText(),
                review.getStatus() != null ? review.getStatus().name() : null,
                review.getCreatedAt()
        );
    }
}
