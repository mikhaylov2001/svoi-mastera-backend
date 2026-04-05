package ru.svoi.mastera.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.svoi.mastera.backend.dto.CustomerReviewDto;
import ru.svoi.mastera.backend.dto.JobRequestDto;
import ru.svoi.mastera.backend.dto.PublicCustomerProfileDto;
import ru.svoi.mastera.backend.service.PublicCustomerService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class PublicCustomerController {

    private final PublicCustomerService publicCustomerService;

    @GetMapping("/{customerId}/profile")
    public PublicCustomerProfileDto getProfile(@PathVariable UUID customerId) {
        return publicCustomerService.getProfile(customerId);
    }

    @GetMapping("/{customerId}/requests")
    public List<JobRequestDto> getRequests(@PathVariable UUID customerId) {
        return publicCustomerService.getRequests(customerId);
    }

    @GetMapping("/{customerId}/reviews")
    public List<CustomerReviewDto> getReviews(@PathVariable UUID customerId) {
        return publicCustomerService.getReviews(customerId);
    }
}