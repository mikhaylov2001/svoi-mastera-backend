package ru.svoi.mastera.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.svoi.mastera.backend.dto.UpsertWorkerServiceItemDto;
import ru.svoi.mastera.backend.entity.User;
import ru.svoi.mastera.backend.entity.WorkerProfile;
import ru.svoi.mastera.backend.entity.WorkerServiceItem;
import ru.svoi.mastera.backend.repository.UserRepository;
import ru.svoi.mastera.backend.repository.WorkerProfileRepository;
import ru.svoi.mastera.backend.repository.WorkerServiceItemRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkerServiceItemServiceTest {

    @Mock
    private WorkerServiceItemRepository workerServiceItemRepository;
    @Mock
    private WorkerProfileRepository workerProfileRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WorkerServiceItemService service;

    @Test
    void createThrowsWhenTitleEmpty() {
        UUID userId = UUID.randomUUID();
        User user = new User(); user.setId(userId);
        WorkerProfile worker = new WorkerProfile(); worker.setId(UUID.randomUUID()); worker.setUser(user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUser(user)).thenReturn(Optional.of(worker));

        var dto = new UpsertWorkerServiceItemDto();
        dto.setTitle("   ");

        assertThrows(RuntimeException.class, () -> service.create(userId, dto));
    }

    @Test
    void listMyReturnsResults() {
        UUID userId = UUID.randomUUID();
        User user = new User(); user.setId(userId);
        WorkerProfile worker = new WorkerProfile(); worker.setId(UUID.randomUUID()); worker.setUser(user);

        WorkerServiceItem item = new WorkerServiceItem(); item.setWorkerProfile(worker);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUser(user)).thenReturn(Optional.of(worker));
        when(workerServiceItemRepository.findAllByWorkerProfileOrderByCreatedAtDesc(worker)).thenReturn(java.util.List.of(item));

        var results = service.listMy(userId);
        assertThat(results).hasSize(1);
    }
}
