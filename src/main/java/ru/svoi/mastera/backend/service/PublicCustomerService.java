package ru.svoi.mastera.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svoi.mastera.backend.dto.JobRequestDto;
import ru.svoi.mastera.backend.dto.PublicCustomerProfileDto;
import ru.svoi.mastera.backend.entity.CustomerProfile;
import ru.svoi.mastera.backend.entity.JobRequest;
import ru.svoi.mastera.backend.entity.enams.JobRequestStatus;
import ru.svoi.mastera.backend.repository.CustomerProfileRepository;
import ru.svoi.mastera.backend.repository.JobRequestRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicCustomerService {

    private final CustomerProfileRepository customerProfileRepository;
    private final JobRequestRepository jobRequestRepository;

    @Transactional(readOnly = true)
    public PublicCustomerProfileDto getProfile(UUID customerId) {
        CustomerProfile profile = customerProfileRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        List<JobRequest> requests = jobRequestRepository.findAllByCustomerOrderByCreatedAtDesc(profile);

        int total     = requests.size();
        int completed = (int) requests.stream().filter(r -> r.getStatus() == JobRequestStatus.COMPLETED).count();
        int open      = (int) requests.stream().filter(r -> r.getStatus() == JobRequestStatus.OPEN).count();

        return new PublicCustomerProfileDto(
                profile.getId(),
                profile.getDisplayName(),
                profile.getLastName(),
                profile.getCity(),
                profile.getUser() != null ? profile.getUser().getAvatarUrl() : null,
                profile.getUser() != null ? profile.getUser().getCreatedAt() : null,
                total,
                completed,
                open
        );
    }

    @Transactional(readOnly = true)
    public List<JobRequestDto> getRequests(UUID customerId) {
        CustomerProfile profile = customerProfileRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return jobRequestRepository.findAllByCustomerOrderByCreatedAtDesc(profile)
                .stream()
                .filter(r -> r.getStatus() == JobRequestStatus.OPEN)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private JobRequestDto toDto(JobRequest jr) {
        UUID custId = jr.getCustomer() != null && jr.getCustomer().getUser() != null
                ? jr.getCustomer().getUser().getId() : null;
        String custName = jr.getCustomer() != null ? jr.getCustomer().getDisplayName() : null;

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
                custId,
                custName
        );
    }
}