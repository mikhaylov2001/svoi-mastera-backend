package ru.svoi.mastera.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.svoi.mastera.backend.dto.AuthResponse;
import ru.svoi.mastera.backend.dto.LoginRequest;
import ru.svoi.mastera.backend.dto.RegisterRequest;
import ru.svoi.mastera.backend.entity.CustomerProfile;
import ru.svoi.mastera.backend.entity.User;
import ru.svoi.mastera.backend.entity.WorkerProfile;
import ru.svoi.mastera.backend.entity.enams.UserStatus;
import ru.svoi.mastera.backend.repository.CustomerProfileRepository;
import ru.svoi.mastera.backend.repository.UserRepository;
import ru.svoi.mastera.backend.repository.WorkerProfileRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private WorkerProfileRepository workerProfileRepository;
    @Mock
    private CustomerProfileRepository customerProfileRepository;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void registerCreatesBothProfilesWhenRequestSpecifiesBoth() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setDisplayName("Test User");
        request.setAsWorker(true);
        request.setAsCustomer(true);

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hash");
        when(userRepository.save(any())).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(workerProfileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(customerProfileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(response.getUser().isHasWorkerProfile()).isTrue();
        assertThat(response.getUser().isHasCustomerProfile()).isTrue();

        verify(userRepository).save(any(User.class));
        verify(workerProfileRepository).save(any(WorkerProfile.class));
        verify(customerProfileRepository).save(any(CustomerProfile.class));
    }

    @Test
    void loginThrowsOnWrongCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("a@b.com");
        request.setPassword("bad");

        User user = new User();
        user.setEmail("a@b.com");
        user.setPasswordHash("hash");

        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertThat(ex.getMessage()).isEqualTo("Invalid credentials");
    }

    @Test
    void getMeReturnsUserDto() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("user@test.com");
        user.setStatus(UserStatus.ACTIVE);
        CustomerProfile profile = new CustomerProfile();
        profile.setDisplayName("Cust");
        profile.setUser(user);
        user.setCustomerProfile(profile);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var userDto = authService.getMe(userId);

        assertThat(userDto).isNotNull();
        assertThat(userDto.getDisplayName()).isEqualTo("Cust");
        assertThat(userDto.isHasCustomerProfile()).isTrue();
    }
}
