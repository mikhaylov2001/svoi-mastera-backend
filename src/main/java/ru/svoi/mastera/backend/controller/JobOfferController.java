package ru.svoi.mastera.backend.controller;

import org.springframework.web.bind.annotation.*;
import ru.svoi.mastera.backend.dto.JobOfferDto;
import ru.svoi.mastera.backend.service.WorkerJobService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/job-requests")
public class JobOfferController {

    private final WorkerJobService workerJobService;

    public JobOfferController(WorkerJobService workerJobService) {
        this.workerJobService = workerJobService;
    }

    @GetMapping("/{id}/offers")
    public List<JobOfferDto> listOffers(@PathVariable("id") UUID jobRequestId) {
        return workerJobService.listOffersForRequest(jobRequestId);
    }
}
