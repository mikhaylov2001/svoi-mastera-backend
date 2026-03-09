package ru.svoi.mastera.backend.controller;

import org.springframework.web.bind.annotation.*;
import ru.svoi.mastera.backend.dto.DealDto;
import ru.svoi.mastera.backend.service.DealService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/deals")
public class DealController {

    private final DealService dealService;

    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @PostMapping("/accept")
    public DealDto accept(@RequestHeader("X-User-Id") UUID customerUserId,
                          @RequestParam("jobRequestId") UUID jobRequestId,
                          @RequestParam("offerId") UUID offerId) {
        return dealService.acceptOffer(customerUserId, jobRequestId, offerId);
    }

    @GetMapping
    public List<DealDto> myDeals(@RequestHeader("X-User-Id") UUID userId) {
        return dealService.listMyDeals(userId);
    }

    @GetMapping("/{id}")
    public DealDto getById(@PathVariable UUID id) {
        return dealService.getById(id);
    }

    // Подтвердить выполнение (обе стороны)
    @PostMapping("/{id}/confirm")
    public DealDto confirm(@RequestHeader("X-User-Id") UUID userId,
                           @PathVariable("id") UUID dealId) {
        return dealService.confirmDeal(userId, dealId);
    }

    // Backward compat
    @PostMapping("/{id}/complete")
    public DealDto complete(@RequestHeader("X-User-Id") UUID userId,
                            @PathVariable("id") UUID dealId) {
        return dealService.completeDeal(userId, dealId);
    }
}