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
    private final NotificationService notificationService;
    private final JobRequestService jobRequestService;

    @Transactional
    public List<JobRequestDto> listOpenJobRequests() {
        List<JobRequest> list = jobRequestRepository
                .findAll()
                .stream()
                .filter(jr -> jr.getStatus() == JobRequestStatus.OPEN)
                .collect(Collectors.toList());
        for (JobRequest jr : list) {
            jobRequestService.reconcileJobRequestIfDealCompleted(jr);
        }
        return list.stream()
                .filter(jr -> jr.getStatus() == JobRequestStatus.OPEN)
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
        if (jobOfferRepository.existsByJobRequest_IdAndWorker_Id(jobRequest.getId(), worker.getId())) {
            throw new RuntimeException("Вы уже откликнулись на эту заявку");
        }
        JobOffer offer = new JobOffer();
        offer.setJobRequest(jobRequest);
        offer.setWorker(worker);
        offer.setMessage(dto.getMessage());
        offer.setPrice(dto.getPrice());
        offer.setEstimatedDays(dto.getEstimatedDays());
        offer.setStatus(JobOfferStatus.CREATED);

        offer = jobOfferRepository.save(offer);

        // 🔔 Уведомление заказчику: мастер откликнулся
        try {
            UUID customerUserId = jobRequest.getCustomer().getUser().getId();
            String workerLabel = workerOfferShortLabel(worker);
            notificationService.notifyNewOffer(customerUserId, workerLabel, jobRequest.getTitle(), jobRequest.getId());
        } catch (Exception ignored) {}

        return toJobOfferDto(offer);

    }

    /**
     * Имя и фамилия для отклика: учитываем {@link WorkerProfile#getLastName()};
     * если фамилии нет, но в displayName несколько слов — делим на имя и остаток.
     */
    private static String[] workerOfferNameParts(WorkerProfile worker) {
        String display = worker.getDisplayName() != null ? worker.getDisplayName().trim() : "";
        String lastName = worker.getLastName();
        if (lastName != null && !lastName.isBlank()) {
            return new String[] { display, lastName.trim() };
        }
        int sp = display.indexOf(' ');
        if (sp > 0) {
            String first = display.substring(0, sp).trim();
            String rest = display.substring(sp + 1).trim();
            return new String[] { first, rest.isEmpty() ? null : rest };
        }
        return new String[] { display, null };
    }

    private static String workerOfferShortLabel(WorkerProfile worker) {
        String[] p = workerOfferNameParts(worker);
        if (p[1] != null && !p[1].isBlank()) {
            return (p[0] + " " + p[1]).trim();
        }
        return p[0] != null && !p[0].isBlank() ? p[0] : "Мастер";
    }

    private JobRequestDto toJobRequestDto(JobRequest jr) {
        UUID customerId = null;
        String customerName = null;
        String customerLastName = null;
        String customerAvatar = null;
        if (jr.getCustomer() != null) {
            customerId = jr.getCustomer().getUser() != null ? jr.getCustomer().getUser().getId() : null;
            customerName = jr.getCustomer().getDisplayName();
            customerLastName = jr.getCustomer().getLastName();
            customerAvatar = jr.getCustomer().getUser() != null ? jr.getCustomer().getUser().getAvatarUrl() : null;
        }
        long offersCount = jobOfferRepository.countByJobRequest_Id(jr.getId());
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
                jr.getStatus() != null ? jr.getStatus().name() : null,
                jr.getPhotos(),
                customerId,
                customerName,
                customerLastName,
                customerAvatar,
                offersCount
        );
    }

    private JobOfferDto toJobOfferDto(JobOffer offer) {
        WorkerProfile w = offer.getWorker();
        String[] parts = workerOfferNameParts(w);
        return new JobOfferDto(
                offer.getId(),
                offer.getJobRequest().getId(),
                w.getId(),
                w.getUser().getId(),
                parts[0],
                parts[1],
                w.getUser().getAvatarUrl(),
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