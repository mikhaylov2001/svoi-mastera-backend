package ru.svoi.mastera.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.svoi.mastera.backend.dto.UpsertWorkerServiceItemDto;
import ru.svoi.mastera.backend.dto.WorkerServiceItemDto;
import ru.svoi.mastera.backend.service.WorkerServiceItemService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WorkerServiceItemController {

    private final WorkerServiceItemService workerServiceItemService;

    // Public: list services of a worker (active only)
    @GetMapping("/api/v1/workers/{workerUserId}/services")
    public List<WorkerServiceItemDto> listByWorker(@PathVariable UUID workerUserId) {
        return workerServiceItemService.listByWorker(workerUserId);
    }

    // Worker: manage own services
    @GetMapping("/api/v1/worker/services")
    public List<WorkerServiceItemDto> listMy(@RequestHeader("X-User-Id") UUID userId) {
        return workerServiceItemService.listMy(userId);
    }

    @PostMapping("/api/v1/worker/services")
    public WorkerServiceItemDto create(@RequestHeader("X-User-Id") UUID userId,
                                       @RequestBody UpsertWorkerServiceItemDto dto) {
        return workerServiceItemService.create(userId, dto);
    }

    @PatchMapping("/api/v1/worker/services/{id}")
    public WorkerServiceItemDto update(@RequestHeader("X-User-Id") UUID userId,
                                       @PathVariable("id") UUID itemId,
                                       @RequestBody UpsertWorkerServiceItemDto dto) {
        return workerServiceItemService.update(userId, itemId, dto);
    }

    @DeleteMapping("/api/v1/worker/services/{id}")
    public void delete(@RequestHeader("X-User-Id") UUID userId,
                       @PathVariable("id") UUID itemId) {
        workerServiceItemService.delete(userId, itemId);
    }
}

