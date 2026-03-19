package ru.svoi.mastera.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svoi.mastera.backend.dto.ChangePasswordDto;
import ru.svoi.mastera.backend.dto.UpdateProfileDto;
import ru.svoi.mastera.backend.dto.UserProfileDto;
import ru.svoi.mastera.backend.entity.CustomerProfile;
import ru.svoi.mastera.backend.entity.User;
import ru.svoi.mastera.backend.entity.WorkerProfile;
import ru.svoi.mastera.backend.repository.CustomerProfileRepository;
import ru.svoi.mastera.backend.repository.UserRepository;
import ru.svoi.mastera.backend.repository.WorkerProfileRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String displayName = null;
        String phone = null;
        String city = null;
        String role = null;

        // Проверяем какой профиль у пользователя
        CustomerProfile customerProfile = customerProfileRepository.findByUser(user).orElse(null);
        WorkerProfile workerProfile = workerProfileRepository.findByUser(user).orElse(null);

        if (workerProfile != null) {
            displayName = workerProfile.getDisplayName();
            phone = workerProfile.getPhone();
            city = workerProfile.getCity();
            role = "WORKER";
        } else if (customerProfile != null) {
            displayName = customerProfile.getDisplayName();
            phone = customerProfile.getPhone();
            city = customerProfile.getCity();
            role = "CUSTOMER";
        }

        return new UserProfileDto(
                user.getId(),
                displayName,
                user.getEmail(),
                phone,
                city,
                role,
                user.getCreatedAt()
        );
    }

    @Transactional
    public UserProfileDto updateProfile(UUID userId, UpdateProfileDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Обновляем email в User
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            // Проверяем что email не занят
            if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(dto.getEmail());
            userRepository.save(user);
        }

        // Обновляем профиль (Customer или Worker)
        CustomerProfile customerProfile = customerProfileRepository.findByUser(user).orElse(null);
        WorkerProfile workerProfile = workerProfileRepository.findByUser(user).orElse(null);

        if (workerProfile != null) {
            if (dto.getDisplayName() != null) workerProfile.setDisplayName(dto.getDisplayName());
            if (dto.getPhone() != null) workerProfile.setPhone(dto.getPhone());
            if (dto.getCity() != null) workerProfile.setCity(dto.getCity());
            workerProfileRepository.save(workerProfile);
        } else if (customerProfile != null) {
            if (dto.getDisplayName() != null) customerProfile.setDisplayName(dto.getDisplayName());
            if (dto.getPhone() != null) customerProfile.setPhone(dto.getPhone());
            if (dto.getCity() != null) customerProfile.setCity(dto.getCity());
            customerProfileRepository.save(customerProfile);
        }

        return getProfile(userId);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверяем текущий пароль
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Проверяем новый пароль
        if (dto.getNewPassword() == null || dto.getNewPassword().length() < 6) {
            throw new RuntimeException("New password must be at least 6 characters");
        }

        // Обновляем пароль
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }
}