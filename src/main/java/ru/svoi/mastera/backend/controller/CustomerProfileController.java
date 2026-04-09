package ru.svoi.mastera.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.svoi.mastera.backend.dto.CustomerProfileDto;
import ru.svoi.mastera.backend.entity.CustomerProfile;
import ru.svoi.mastera.backend.entity.User;
import ru.svoi.mastera.backend.repository.CustomerProfileRepository;
import ru.svoi.mastera.backend.repository.UserRepository;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CustomerProfileController {

    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;

    @GetMapping("/customer-profiles/me")
    public CustomerProfileDto me(@RequestHeader("X-User-Id") UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CustomerProfile profile = customerProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Customer profile not found"));

        return toDto(profile);
    }

    // Публичный профиль заказчика по userId
    @GetMapping("/customers/{customerId}/profile")
    public CustomerProfileDto publicProfile(@PathVariable UUID customerId) {
        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CustomerProfile profile = customerProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Customer profile not found"));

        return toDto(profile);
    }

    private CustomerProfileDto toDto(CustomerProfile profile) {
        String avatarUrl = profile.getUser() != null ? profile.getUser().getAvatarUrl() : null;
        return new CustomerProfileDto(
                profile.getId(),
                profile.getDisplayName(),
                profile.getLastName(),
                profile.getCity(),
                avatarUrl,
                profile.getCreatedAt()
        );
    }
}