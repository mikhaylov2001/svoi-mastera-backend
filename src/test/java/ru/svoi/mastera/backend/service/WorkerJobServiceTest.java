package ru.svoi.mastera.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.svoi.mastera.backend.dto.CreateJobOfferDto;
import ru.svoi.mastera.backend.dto.JobOfferDto;
import ru.svoi.mastera.backend.entity.Category;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkerJobServiceTest {
    @Mock
    private JobRequestRepository jobRequestRepository;
    @Mock
    private JobOfferRepository jobOfferRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WorkerProfileRepository workerProfileRepository;

    @InjectMocks
    private WorkerJobService workerJobService;

    @Test
    void listOpenJobRequestsFiltersOpenOnly() {
        JobRequest open = new JobRequest(); open.setId(UUID.randomUUID()); open.setStatus(JobRequestStatus.OPEN); open.setCategory(new Category());
        JobRequest closed = new JobRequest(); closed.setId(UUID.randomUUID()); closed.setStatus(JobRequestStatus.COMPLETED); closed.setCategory(new Category());

        when(jobRequestRepository.findAll()).thenReturn(List.of(open, closed));

        var list = workerJobService.listOpenJobRequests();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getStatus()).isEqualTo(JobRequestStatus.OPEN.name());
    }

    @Test
    void createOfferThrowsWhenJobNotOpen() {
        UUID userId = UUID.randomUUID();
        UUID jobRequestId = UUID.randomUUID();

        User user = new User(); user.setId(userId);
        WorkerProfile worker = new WorkerProfile(); worker.setId(UUID.randomUUID()); worker.setUser(user);
        JobRequest request = new JobRequest(); request.setId(jobRequestId); request.setStatus(JobRequestStatus.IN_PROGRESS);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUser(user)).thenReturn(Optional.of(worker));
        when(jobRequestRepository.findById(jobRequestId)).thenReturn(Optional.of(request));

        var dto = new CreateJobOfferDto();
        dto.setMessage("hi"); dto.setPrice(java.math.BigDecimal.valueOf(1000));

        assertThrows(RuntimeException.class, () -> workerJobService.createOffer(userId, jobRequestId, dto));
    }
}
