package ru.svoi.mastera.backend.service;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svoi.mastera.backend.dto.CreateJobRequestDto;
import ru.svoi.mastera.backend.dto.JobRequestDto;
import ru.svoi.mastera.backend.entity.Category;
import ru.svoi.mastera.backend.entity.CustomerProfile;
import ru.svoi.mastera.backend.entity.JobRequest;
import ru.svoi.mastera.backend.entity.User;
import ru.svoi.mastera.backend.entity.enams.JobRequestStatus;
import ru.svoi.mastera.backend.repository.CategoryRepository;
import ru.svoi.mastera.backend.repository.CustomerProfileRepository;
import ru.svoi.mastera.backend.repository.JobRequestRepository;
import ru.svoi.mastera.backend.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobRequestService {
    private final JobRequestRepository jobRequestRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public JobRequestDto create(UUID userId, CreateJobRequestDto dto){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        CustomerProfile customer = customerProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Customer profile not found"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        JobRequest jobRequest = getJobRequest(dto, customer, category);

        jobRequest = jobRequestRepository.save(jobRequest);

        return toDto(jobRequest);
    }

    private static @NonNull JobRequest getJobRequest(CreateJobRequestDto dto, CustomerProfile customer, Category category) {
        JobRequest jobRequest = new JobRequest();
        jobRequest.setCustomer(customer);
        jobRequest.setCategory(category);
        jobRequest.setTitle(dto.getTitle());
        jobRequest.setDescription(dto.getDescription());
        jobRequest.setCity(dto.getCity());
        jobRequest.setAddressText(dto.getAddressText());
        jobRequest.setScheduledAt(dto.getScheduledAt());
        // если в entity budgetFrom/budgetTo - BigDecimal, тут нужно конвертнуть

        jobRequest.setBudgetFrom(dto.getBudgetFrom());
        jobRequest.setBudgetTo(dto.getBudgetTo());
        jobRequest.setStatus(JobRequestStatus.OPEN);
        return jobRequest;
    }

    private JobRequestDto toDto(JobRequest jr) {
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

    @Transactional(readOnly = true)
    public List<JobRequestDto> getMy(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CustomerProfile customer = customerProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Customer profile not found"));

        List<JobRequest> list = jobRequestRepository.findAllByCustomerOrderByCreatedAtDesc(customer);
        return list.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JobRequestDto getById(UUID userId, UUID requestId) {
        // userId можно использовать для проверки прав, пока просто игнорируем
        JobRequest jobRequest = jobRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Job request not found"));

        return toDto(jobRequest);
    }
}
