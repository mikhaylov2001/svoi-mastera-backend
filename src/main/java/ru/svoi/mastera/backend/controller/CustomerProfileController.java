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
@RequestMapping("/api/v1/customer-profiles")
@RequiredArgsConstructor
public class CustomerProfileController {

    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;

    @GetMapping("/me")
    public CustomerProfileDto me(@RequestHeader("X-User-Id") UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CustomerProfile profile = customerProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Customer profile not found"));

        return new CustomerProfileDto(
                profile.getId(),
                profile.getDisplayName(),
                profile.getCity(),
                profile.getCreatedAt()
        );
    }
}
