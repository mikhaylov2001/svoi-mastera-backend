package ru.svoi.mastera.backend.service;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.svoi.mastera.backend.dto.CreateJobRequestDto;
import ru.svoi.mastera.backend.dto.JobRequestDto;
import ru.svoi.mastera.backend.entity.Category;
import ru.svoi.mastera.backend.entity.CustomerProfile;
import ru.svoi.mastera.backend.entity.JobRequest;
import ru.svoi.mastera.backend.entity.User;
import ru.svoi.mastera.backend.entity.Deal;
import ru.svoi.mastera.backend.entity.enams.DealStatus;
import ru.svoi.mastera.backend.entity.enams.JobRequestStatus;
import ru.svoi.mastera.backend.repository.CategoryRepository;
import ru.svoi.mastera.backend.repository.CustomerProfileRepository;
import ru.svoi.mastera.backend.repository.DealRepository;
import ru.svoi.mastera.backend.repository.JobOfferRepository;
import ru.svoi.mastera.backend.repository.JobRequestRepository;
import ru.svoi.mastera.backend.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.svoi.mastera.backend.entity.enams.DealStatus.COMPLETED;
import static ru.svoi.mastera.backend.entity.enams.DealStatus.IN_PROGRESS;
import static ru.svoi.mastera.backend.entity.enams.DealStatus.NEW;

@Service
@RequiredArgsConstructor
public class JobRequestService {
    private final JobRequestRepository jobRequestRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final JobOfferRepository jobOfferRepository;
    private final DealRepository dealRepository;
    private final DealService dealService;

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
        jobRequest.setBudgetFrom(dto.getBudgetFrom());
        jobRequest.setBudgetTo(dto.getBudgetTo());
        jobRequest.setPhotos(dto.getPhotos()); // ✅ ДОБАВЛЕНО: сохраняем фото
        jobRequest.setStatus(JobRequestStatus.OPEN);
        return jobRequest;
    }

    private JobRequestDto toDto(JobRequest jr) {
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
        boolean customerGuarantee = jr.getCustomer() != null
                && jr.getCustomer().getGuaranteeTermsAcceptedAt() != null;
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
                offersCount,
                customerGuarantee
        );
    }

    @Transactional
    public List<JobRequestDto> getMy(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CustomerProfile customer = customerProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Customer profile not found"));

        List<JobRequest> list = jobRequestRepository.findAllByCustomerOrderByCreatedAtDesc(customer);
        for (JobRequest jr : list) {
            reconcileJobRequestIfDealCompleted(jr);
        }
        return list.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Если по заявке уже есть сделка COMPLETED, а статус заявки не обновился — синхронизируем (устаревшие данные).
     */
    public void reconcileJobRequestIfDealCompleted(JobRequest jr) {
        if (jr.getStatus() == JobRequestStatus.COMPLETED || jr.getStatus() == JobRequestStatus.CANCELLED) {
            return;
        }
        if (!dealRepository.existsByJobRequest_IdAndStatus(jr.getId(), DealStatus.COMPLETED)) {
            return;
        }
        jr.setStatus(JobRequestStatus.COMPLETED);
        jobRequestRepository.save(jr);
    }

    @Transactional
    public JobRequestDto getById(UUID userId, UUID requestId) {
        JobRequest jobRequest = jobRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job request not found"));

        boolean owner = jobRequest.getCustomer() != null
                && jobRequest.getCustomer().getUser() != null
                && jobRequest.getCustomer().getUser().getId().equals(userId);
        boolean open = jobRequest.getStatus() == JobRequestStatus.OPEN;
        if (!owner && !open) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        if (owner) {
            reconcileJobRequestIfDealCompleted(jobRequest);
        }
        return toDto(jobRequest);
    }

    @Transactional
    public JobRequestDto update(UUID userId, UUID requestId, CreateJobRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        CustomerProfile customer = customerProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer profile not found"));

        JobRequest jr = jobRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job request not found"));

        if (!jr.getCustomer().getId().equals(customer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your request");
        }
        if (jr.getStatus() != JobRequestStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only open requests can be edited");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));

        jr.setCategory(category);
        jr.setTitle(dto.getTitle());
        jr.setDescription(dto.getDescription() != null ? dto.getDescription() : "Без описания");
        jr.setCity(dto.getCity());
        jr.setAddressText(dto.getAddressText());
        jr.setScheduledAt(dto.getScheduledAt());
        jr.setBudgetFrom(dto.getBudgetFrom());
        jr.setBudgetTo(dto.getBudgetTo());
        if (dto.getPhotos() != null) {
            jr.setPhotos(dto.getPhotos());
        }
        jr = jobRequestRepository.save(jr);
        return toDto(jr);
    }

    /**
     * Заказчик снимает заявку с публикации: OPEN → CANCELLED;
     * при активной сделке сначала отменяем её через {@link DealService}, затем помечаем заявку CANCELLED.
     */
    @Transactional
    public JobRequestDto cancelByCustomer(UUID userId, UUID requestId) {
        JobRequest jr = jobRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job request not found"));

        if (jr.getCustomer() == null || jr.getCustomer().getUser() == null
                || !jr.getCustomer().getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your request");
        }

        JobRequestStatus st = jr.getStatus();
        if (st == JobRequestStatus.CANCELLED || st == JobRequestStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Эту заявку уже нельзя убрать");
        }

        if (st == JobRequestStatus.OPEN) {
            jr.setStatus(JobRequestStatus.CANCELLED);
            jobRequestRepository.save(jr);
            return toDto(jr);
        }

        String reason = "Заявка снята заказчиком";
        List<Deal> deals = dealRepository.findAllByJobRequest_Id(requestId);
        for (Deal d : deals) {
            DealStatus ds = d.getStatus();
            if (ds == DealStatus.CANCELLED || ds == DealStatus.REFUNDED || ds == COMPLETED) {
                continue;
            }
            if (ds == NEW) {
                dealService.cancelPendingDeal(userId, d.getId(), reason);
            } else if (ds == IN_PROGRESS) {
                dealService.cancelActiveDeal(userId, d.getId(), reason);
            }
        }

        JobRequest refreshed = jobRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job request not found"));
        refreshed.setStatus(JobRequestStatus.CANCELLED);
        refreshed.setSelectedOffer(null);
        jobRequestRepository.save(refreshed);
        return toDto(refreshed);
    }
}