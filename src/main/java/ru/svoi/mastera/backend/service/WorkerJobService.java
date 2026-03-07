package ru.svoi.mastera.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svoi.mastera.backend.dto.CreateJobOfferDto;
import ru.svoi.mastera.backend.dto.JobOfferDto;
import ru.svoi.mastera.backend.dto.JobRequestDto;
import ru.svoi.mastera.backend.entity.JobOffer;
import ru.svoi.mastera.backend.entity.JobRequest;
import ru.svoi.mastera.backend.entity.User;
import ru.svoi.mastera.backend.entity.WorkerProfile;
import ru.svoi.mastera.backend.entity.enams.JobOfferStatus;
import ru.svoi.mastera.backend.entity.enams.JobRequestStatus;
import ru.svoi.mastera.backend.repository.JobOfferRepository;
import ru.svoi.mastera.backend.repository.JobRequestRepository;
import ru.svoi.mastera.backend.repository.UserRepository;
import ru.svoi.mastera.backend.repository.WorkerProfileRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkerJobService {
    private final JobRequestRepository jobRequestRepository;
    private final JobOfferRepository jobOfferRepository;
    private final UserRepository userRepository;
    private final WorkerProfileRepository workerProfileRepository;

    @Transactional(readOnly = true)
    public List<JobRequestDto> listOpenJobRequests() {
        List<JobRequest> list = jobRequestRepository
                .findAll()
                .stream()
                .filter(jr -> jr.getStatus() == JobRequestStatus.OPEN)
                .collect(Collectors.toList());
        return list.stream()
                .map(this::toJobRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public JobOfferDto createOffer(UUID userId, UUID jobRequestId, CreateJobOfferDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        WorkerProfile worker = workerProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Worker profile not found"));

        JobRequest jobRequest = jobRequestRepository.findById(jobRequestId)
                .orElseThrow(() -> new RuntimeException("Job request not found"));

        if (jobRequest.getStatus() != JobRequestStatus.OPEN) {
            throw new RuntimeException("Job request is not open");
        }
            JobOffer offer = new JobOffer();
            offer.setJobRequest(jobRequest);
            offer.setWorker(worker);
            offer.setMessage(dto.getMessage());
            offer.setPrice(dto.getPrice());
            offer.setEstimatedDays(dto.getEstimatedDays());
            offer.setStatus(JobOfferStatus.CREATED);

            offer = jobOfferRepository.save(offer);

            return toJobOfferDto(offer);

    }

    private JobRequestDto toJobRequestDto(JobRequest jr) {
        return new JobRequestDto(
                jr.getId(),
                jr.getCategory().getId(),
                jr.getTitle(),
                jr.getDescription(),
                jr.getCity(),
                jr.getAddressText(),
                jr.getCreatedAt(),
                jr.getScheduledAt(),
                jr.getBudgetFrom(),
                jr.getBudgetTo(),
                jr.getStatus() != null ? jr.getStatus().name() : null
        );
    }

    private JobOfferDto toJobOfferDto(JobOffer offer) {
        return new JobOfferDto(
                offer.getId(),
                offer.getJobRequest().getId(),
                offer.getWorker().getId(),
                offer.getMessage(),
                offer.getPrice(),
                offer.getEstimatedDays(),
                offer.getStatus() != null ? offer.getStatus().name() : null,
                offer.getCreatedAt()
        );
    }
    @Transactional(readOnly = true)
    public List<JobOfferDto> listOffersForRequest(UUID jobRequestId) {
        JobRequest jobRequest = jobRequestRepository.findById(jobRequestId)
                .orElseThrow(() -> new RuntimeException("Job request not found"));

        List<JobOffer> offers = jobOfferRepository.findAllByJobRequest(jobRequest);
        return offers.stream()
                .map(this::toJobOfferDto)
                .collect(Collectors.toList());
    }


}
