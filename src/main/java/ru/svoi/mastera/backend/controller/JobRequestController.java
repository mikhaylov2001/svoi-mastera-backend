package ru.svoi.mastera.backend.controller;

import org.springframework.web.bind.annotation.*;
import ru.svoi.mastera.backend.dto.CreateJobRequestDto;
import ru.svoi.mastera.backend.dto.JobRequestDto;
import ru.svoi.mastera.backend.service.JobRequestService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/job-requests")
public class JobRequestController {

    private final JobRequestService jobRequestService;

    public JobRequestController(JobRequestService jobRequestService) {
        this.jobRequestService = jobRequestService;
    }

    @PostMapping
    public JobRequestDto create(@RequestHeader("X-User-Id") UUID userId,
                                @RequestBody CreateJobRequestDto dto) {
        return jobRequestService.create(userId, dto);
    }

    @GetMapping("/my")
    public List<JobRequestDto> my(@RequestHeader("X-User-Id") UUID userId) {
        return jobRequestService.getMy(userId);
    }

    @GetMapping("/{id}")
    public JobRequestDto getById(@RequestHeader("X-User-Id") UUID userId,
                                 @PathVariable UUID id) {
        return jobRequestService.getById(userId, id);
    }
}
