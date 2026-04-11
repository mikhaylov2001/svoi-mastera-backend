package ru.svoi.mastera.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.svoi.mastera.backend.dto.ReviewCreateDto;
import ru.svoi.mastera.backend.dto.ReviewDto;
import ru.svoi.mastera.backend.service.ReviewService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // создать отзыв по сделке (заказчик → мастеру)
    @PostMapping("/deals/{dealId}/reviews")
    public ReviewDto create(@RequestHeader("X-User-Id") UUID userId,
                            @PathVariable UUID dealId,
                            @RequestBody ReviewCreateDto body) {
        return reviewService.create(userId, dealId, body);
    }

    // создать отзыв по сделке (мастер → заказчику)
    @PostMapping("/deals/{dealId}/reviews/worker")
    public ReviewDto createByWorker(@RequestHeader("X-User-Id") UUID userId,
                                    @PathVariable UUID dealId,
                                    @RequestBody ReviewCreateDto body) {
        return reviewService.createByWorker(userId, dealId, body);
    }

    // список отзывов по мастеру
    @GetMapping("/workers/{workerUserId}/reviews")
    public List<ReviewDto> listByWorker(@PathVariable UUID workerUserId) {
        return reviewService.listByWorker(workerUserId);
    }





}