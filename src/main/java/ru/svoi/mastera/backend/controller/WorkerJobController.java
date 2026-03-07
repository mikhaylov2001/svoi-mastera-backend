package ru.svoi.mastera.backend.controller;

import org.springframework.web.bind.annotation.*;
import ru.svoi.mastera.backend.dto.CreateJobOfferDto;
import ru.svoi.mastera.backend.dto.JobOfferDto;
import ru.svoi.mastera.backend.dto.JobRequestDto;
import ru.svoi.mastera.backend.service.WorkerJobService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/worker")
public class WorkerJobController {

    private final WorkerJobService workerJobService;

    public WorkerJobController(WorkerJobService workerJobService) {
        this.workerJobService = workerJobService;
    }

    @GetMapping("/job-requests")
    public List<JobRequestDto> listOpenJobRequests() {
        return workerJobService.listOpenJobRequests();
    }

    @PostMapping("/job-requests/{id}/offers")
    public JobOfferDto createOffer(@RequestHeader("X-User-Id") UUID userId,
                                   @PathVariable("id") UUID jobRequestId,
                                   @RequestBody CreateJobOfferDto dto) {
        return workerJobService.createOffer(userId, jobRequestId, dto);
    }
}
