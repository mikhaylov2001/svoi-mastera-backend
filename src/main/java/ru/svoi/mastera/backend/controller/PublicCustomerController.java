package ru.svoi.mastera.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
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

    // GET /api/v1/customers/{customerId}/profile
    @GetMapping("/{customerId}/profile")
    public PublicCustomerProfileDto getProfile(@PathVariable UUID customerId) {
        return publicCustomerService.getProfile(customerId);
    }

    // GET /api/v1/customers/{customerId}/requests
    @GetMapping("/{customerId}/requests")
    public List<JobRequestDto> getRequests(@PathVariable UUID customerId) {
        return publicCustomerService.getRequests(customerId);
    }
}