package ru.svoi.mastera.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svoi.mastera.backend.dto.CustomerReviewDto;
import ru.svoi.mastera.backend.dto.JobRequestDto;
import ru.svoi.mastera.backend.dto.PublicCustomerProfileDto;
import ru.svoi.mastera.backend.entity.CustomerProfile;
import ru.svoi.mastera.backend.entity.JobRequest;
import ru.svoi.mastera.backend.entity.Review;
import ru.svoi.mastera.backend.entity.enams.JobRequestStatus;
import ru.svoi.mastera.backend.entity.enams.ReviewStatus;
import ru.svoi.mastera.backend.repository.CustomerProfileRepository;
import ru.svoi.mastera.backend.repository.JobRequestRepository;
import ru.svoi.mastera.backend.repository.ReviewRepository;
import ru.svoi.mastera.backend.repository.WorkerProfileRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicCustomerService {

    private final CustomerProfileRepository customerProfileRepository;
    private final JobRequestRepository jobRequestRepository;
    private final ReviewRepository reviewRepository;
    private final WorkerProfileRepository workerProfileRepository;

    @Transactional(readOnly = true)
    public PublicCustomerProfileDto getProfile(UUID userId) {
        CustomerProfile profile = customerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found for userId: " + userId));

        List<JobRequest> requests = jobRequestRepository.findAllByCustomerOrderByCreatedAtDesc(profile);

        int total     = requests.size();
        int completed = (int) requests.stream().filter(r -> r.getStatus() == JobRequestStatus.COMPLETED).count();
        int open      = (int) requests.stream().filter(r -> r.getStatus() == JobRequestStatus.OPEN).count();

        return new PublicCustomerProfileDto(
                userId, // возвращаем userId — именно его фронт использует для навигации
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
    public List<JobRequestDto> getRequests(UUID userId) {
        CustomerProfile profile = customerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found for userId: " + userId));

        return jobRequestRepository.findAllByCustomerOrderByCreatedAtDesc(profile)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerReviewDto> getReviews(UUID userId) {
        CustomerProfile profile = customerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found for userId: " + userId));

        return reviewRepository.findAllByTargetCustomerOrderByCreatedAtDesc(profile)
                .stream()
                .filter(r -> {
                    ReviewStatus s = r.getStatus();
                    return s == ReviewStatus.APPROVED || s == ReviewStatus.PUBLISHED;
                })
                .map(r -> {
                    String authorName = null;
                    String authorAvatar = null;
                    String authorLastName = null;
                    UUID authorUserId = null;
                    if (r.getAuthorUser() != null) {
                        authorUserId = r.getAuthorUser().getId();
                        authorAvatar = r.getAuthorUser().getAvatarUrl();
                        var wpOpt = workerProfileRepository.findByUserId(authorUserId);
                        if (wpOpt.isPresent()) {
                            var wp = wpOpt.get();
                            authorName = wp.getDisplayName();
                            authorLastName = wp.getLastName();
                        }
                    }
                    return new CustomerReviewDto(
                            r.getId(),
                            r.getRating(),
                            r.getText(),
                            authorName,
                            authorAvatar,
                            authorUserId,
                            authorLastName,
                            r.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    private JobRequestDto toDto(JobRequest jr) {
        UUID custId = jr.getCustomer() != null && jr.getCustomer().getUser() != null
                ? jr.getCustomer().getUser().getId() : null;
        String custName     = jr.getCustomer() != null ? jr.getCustomer().getDisplayName() : null;
        String custLastName = jr.getCustomer() != null ? jr.getCustomer().getLastName() : null;
        String custAvatar   = jr.getCustomer() != null && jr.getCustomer().getUser() != null
                ? jr.getCustomer().getUser().getAvatarUrl() : null;

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
                custName,
                custLastName,
                custAvatar
        );
    }
}