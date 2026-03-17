package ru.svoi.mastera.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.svoi.mastera.backend.dto.ReviewDto;
import ru.svoi.mastera.backend.dto.WorkerStatsDto;
import ru.svoi.mastera.backend.service.ReviewService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workers")
@RequiredArgsConstructor
public class WorkerPublicController {

    private final ReviewService reviewService;

    // Получить статистику мастера (рейтинг + количество отзывов)
    @GetMapping("/{workerUserId}/stats")
    public WorkerStatsDto getWorkerStats(@PathVariable UUID workerUserId) {
        return reviewService.getWorkerStats(workerUserId);
    }

    // Получить отзывы мастера
    @GetMapping("/{workerUserId}/reviews")
    public List<ReviewDto> getWorkerReviews(@PathVariable UUID workerUserId) {
        return reviewService.listByWorker(workerUserId);
    }
}