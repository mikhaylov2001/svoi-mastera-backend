package ru.svoi.mastera.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.svoi.mastera.backend.dto.WorkerStatsDto;
import ru.svoi.mastera.backend.dto.WorkerCompletedWorkDto;
import ru.svoi.mastera.backend.service.ReviewService;
import ru.svoi.mastera.backend.service.DealService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workers")
@RequiredArgsConstructor
public class WorkerPublicController {

    private final ReviewService reviewService;
    private final DealService dealService;

    // Получить статистику мастера (рейтинг + количество отзывов)
    @GetMapping("/{workerUserId}/stats")
    public WorkerStatsDto getWorkerStats(@PathVariable UUID workerUserId) {
        return reviewService.getWorkerStats(workerUserId);
    }

    // ✅ УДАЛЕНО: getWorkerReviews - дублирует ReviewController
    // Используйте существующий endpoint: GET /api/v1/reviews/worker/{workerUserId}

    // Получить завершённые работы мастера
    @GetMapping("/{workerUserId}/completed-works")
    public List<WorkerCompletedWorkDto> getWorkerCompletedWorks(@PathVariable UUID workerUserId) {
        return dealService.getWorkerCompletedWorks(workerUserId);
    }
}