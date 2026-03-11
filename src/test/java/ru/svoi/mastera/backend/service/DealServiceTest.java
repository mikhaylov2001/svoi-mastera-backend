package ru.svoi.mastera.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.svoi.mastera.backend.dto.DealDto;
import ru.svoi.mastera.backend.entity.*;
import ru.svoi.mastera.backend.entity.enams.DealStatus;
import ru.svoi.mastera.backend.entity.enams.JobRequestStatus;
import ru.svoi.mastera.backend.repository.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {

    @Mock
    private DealRepository dealRepository;
    @Mock
    private JobRequestRepository jobRequestRepository;
    @Mock
    private JobOfferRepository jobOfferRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CustomerProfileRepository customerProfileRepository;
    @Mock
    private WorkerProfileRepository workerProfileRepository;

    @InjectMocks
    private DealService dealService;

    private UUID customerUserId;
    private UUID workerUserId;
    private UUID dealId;

    @BeforeEach
    void setUp() {
        customerUserId = UUID.randomUUID();
        workerUserId = UUID.randomUUID();
        dealId = UUID.randomUUID();
    }

    @Test
    void confirmDeal_setsDealCompletedAndJobRequestCompleted_whenBothSidesConfirmed() {
        User customerUser = new User();
        customerUser.setId(customerUserId);
        CustomerProfile customer = new CustomerProfile();
        customer.setUser(customerUser);

        User workerUser = new User();
        workerUser.setId(workerUserId);
        WorkerProfile worker = new WorkerProfile();
        worker.setUser(workerUser);

        JobRequest jobRequest = new JobRequest();
        jobRequest.setStatus(JobRequestStatus.IN_PROGRESS);

        JobOffer jobOffer = new JobOffer();
        jobOffer.setId(UUID.randomUUID());

        Deal deal = new Deal();
        deal.setId(dealId);
        deal.setCustomer(customer);
        deal.setWorker(worker);
        deal.setJobRequest(jobRequest);
        deal.setJobOffer(jobOffer);
        deal.setStatus(DealStatus.IN_PROGRESS);
        deal.setCustomerConfirmed(false);
        deal.setWorkerConfirmed(true);
        deal.setStartedAt(Instant.now());
        deal.setAgreedPrice(BigDecimal.valueOf(1000));

        when(dealRepository.findById(dealId)).thenReturn(Optional.of(deal));
        when(dealRepository.save(any(Deal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DealDto result = dealService.confirmDeal(customerUserId, dealId);

        assertThat(result.getStatus()).isEqualTo(DealStatus.COMPLETED.name());
        assertThat(deal.getStatus()).isEqualTo(DealStatus.COMPLETED);
        assertThat(deal.getJobRequest().getStatus()).isEqualTo(JobRequestStatus.COMPLETED);
        assertThat(deal.isCustomerConfirmed()).isTrue();
        assertThat(deal.isWorkerConfirmed()).isTrue();

        verify(dealRepository, times(1)).findById(dealId);
        verify(dealRepository, times(1)).save(deal);
    }
}
