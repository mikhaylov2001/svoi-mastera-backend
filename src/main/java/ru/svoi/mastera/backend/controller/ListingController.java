package ru.svoi.mastera.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.svoi.mastera.backend.dto.ListingCreateDto;
import ru.svoi.mastera.backend.dto.ListingDto;
import ru.svoi.mastera.backend.service.ListingService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    // Создать объявление
    @PostMapping("/listings")
    public ListingDto create(@RequestHeader("X-User-Id") UUID userId,
                             @RequestBody ListingCreateDto body) {
        return listingService.create(userId, body);
    }

    // Обновить объявление
    @PutMapping("/listings/{listingId}")
    public ListingDto update(@RequestHeader("X-User-Id") UUID userId,
                             @PathVariable UUID listingId,
                             @RequestBody ListingCreateDto body) {
        return listingService.update(userId, listingId, body);
    }

    // Удалить (деактивировать) объявление
    @DeleteMapping("/listings/{listingId}")
    public void delete(@RequestHeader("X-User-Id") UUID userId,
                       @PathVariable UUID listingId) {
        listingService.delete(userId, listingId);
    }

    // Мои объявления
    @GetMapping("/workers/{workerUserId}/listings")
    public List<ListingDto> getByWorker(@PathVariable UUID workerUserId) {
        return listingService.getByWorker(workerUserId);
    }

    // Все активные объявления (для заказчиков)
    @GetMapping("/listings")
    public List<ListingDto> getAll() {
        return listingService.getAll();
    }

    // Восстановить из архива
    @PostMapping("/listings/{listingId}/restore")
    public ListingDto restore(@RequestHeader("X-User-Id") UUID userId,
                              @PathVariable UUID listingId) {
        return listingService.restore(userId, listingId);
    }
}