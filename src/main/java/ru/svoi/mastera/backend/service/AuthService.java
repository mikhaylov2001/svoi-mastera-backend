package ru.svoi.mastera.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svoi.mastera.backend.dto.AuthResponse;
import ru.svoi.mastera.backend.dto.LoginRequest;
import ru.svoi.mastera.backend.dto.RegisterRequest;
import ru.svoi.mastera.backend.dto.UserDto;
import ru.svoi.mastera.backend.entity.CustomerProfile;
import ru.svoi.mastera.backend.entity.User;
import ru.svoi.mastera.backend.entity.WorkerProfile;
import ru.svoi.mastera.backend.entity.enams.UserStatus;
import ru.svoi.mastera.backend.repository.CustomerProfileRepository;
import ru.svoi.mastera.backend.repository.UserRepository;
import ru.svoi.mastera.backend.repository.WorkerProfileRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WorkerProfileRepository workerProfileRepository;
    private final CustomerProfileRepository customerProfileRepository;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with this email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);

        user = userRepository.save(user);

        boolean asWorker = Boolean.TRUE.equals(request.getAsWorker());
        boolean asCustomer = request.getAsCustomer() == null || Boolean.TRUE.equals(request.getAsCustomer());

        if (asWorker) {
            WorkerProfile worker = new WorkerProfile();
            worker.setUser(user);
            worker.setDisplayName(request.getDisplayName());
            worker.setActive(true);
            worker.setVerified(false);
            workerProfileRepository.save(worker);
        }

        if (asCustomer) {
            CustomerProfile customer = new CustomerProfile();
            customer.setUser(user);
            customer.setDisplayName(request.getDisplayName());
            customerProfileRepository.save(customer);
        }

        boolean hasWorker = asWorker;
        boolean hasCustomer = asCustomer;

        UserDto userDto = toUserDto(user, hasWorker, hasCustomer);
        String fakeToken = UUID.randomUUID().toString();

        return new AuthResponse(fakeToken, userDto);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        boolean hasWorker = user.getWorkerProfile() != null;
        boolean hasCustomer = user.getCustomerProfile() != null;

        UserDto userDto = toUserDto(user, hasWorker, hasCustomer);
        String fakeToken = UUID.randomUUID().toString();

        return new AuthResponse(fakeToken, userDto);
    }

    @Transactional(readOnly = true)
    public UserDto getMe(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean hasWorker = user.getWorkerProfile() != null;
        boolean hasCustomer = user.getCustomerProfile() != null;

        return toUserDto(user, hasWorker, hasCustomer);
    }

    private UserDto toUserDto(User user, boolean hasWorker, boolean hasCustomer) {
        String displayName = null;
        if (user.getCustomerProfile() != null) {
            displayName = user.getCustomerProfile().getDisplayName();
        } else if (user.getWorkerProfile() != null) {
            displayName = user.getWorkerProfile().getDisplayName();
        }
        return new UserDto(
                user.getId(),
                user.getEmail(),
                displayName,
                hasWorker,
                hasCustomer
        );
    }
}
