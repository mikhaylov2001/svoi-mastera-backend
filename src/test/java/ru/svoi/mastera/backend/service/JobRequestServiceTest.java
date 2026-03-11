package ru.svoi.mastera.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.svoi.mastera.backend.dto.CreateJobRequestDto;
import ru.svoi.mastera.backend.dto.JobRequestDto;
import ru.svoi.mastera.backend.entity.Category;
import ru.svoi.mastera.backend.entity.CustomerProfile;
import ru.svoi.mastera.backend.entity.JobRequest;
import ru.svoi.mastera.backend.entity.User;
import ru.svoi.mastera.backend.repository.CategoryRepository;
import ru.svoi.mastera.backend.repository.CustomerProfileRepository;
import ru.svoi.mastera.backend.repository.JobRequestRepository;
import ru.svoi.mastera.backend.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobRequestServiceTest {

    @Mock
    private JobRequestRepository jobRequestRepository;
    @Mock
    private CustomerProfileRepository customerProfileRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private JobRequestService jobRequestService;

    @Test
    void createJobRequestAndGetById() {
        UUID userId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        CustomerProfile customer = new CustomerProfile();
        customer.setId(UUID.randomUUID());
        customer.setUser(user);

        Category category = new Category();
        category.setId(catId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(customerProfileRepository.findByUser(user)).thenReturn(Optional.of(customer));
        when(categoryRepository.findById(catId)).thenReturn(Optional.of(category));
        when(jobRequestRepository.save(any(JobRequest.class))).thenAnswer(invocation -> {
            JobRequest jr = invocation.getArgument(0);
            jr.setId(UUID.randomUUID());
            return jr;
        });

        CreateJobRequestDto dto = new CreateJobRequestDto();
        dto.setTitle("Test");
        dto.setDescription("Desc");
        dto.setCategoryId(catId);
        dto.setCity("Москва");

        JobRequestDto created = jobRequestService.create(userId, dto);
        assertThat(created).isNotNull();
        assertThat(created.getTitle()).isEqualTo("Test");
        assertThat(created.getStatus()).isEqualTo("OPEN");

        JobRequest savedRequest = new JobRequest();
        savedRequest.setId(created.getId());
        savedRequest.setCategory(category);
        savedRequest.setTitle("Test");
        when(jobRequestRepository.findById(created.getId())).thenReturn(Optional.of(savedRequest));
        JobRequestDto byId = jobRequestService.getById(userId, created.getId());
        assertThat(byId).isNotNull();

        verify(jobRequestRepository, atLeastOnce()).findById(any(UUID.class));
    }

    @Test
    void getByIdThrowsWhenNotFound() {
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        when(jobRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> jobRequestService.getById(userId, requestId));
    }
}
