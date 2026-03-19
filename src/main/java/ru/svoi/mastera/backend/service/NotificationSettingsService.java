package ru.svoi.mastera.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svoi.mastera.backend.dto.NotificationSettingsDto;
import ru.svoi.mastera.backend.entity.NotificationSettings;
import ru.svoi.mastera.backend.entity.User;
import ru.svoi.mastera.backend.repository.NotificationSettingsRepository;
import ru.svoi.mastera.backend.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationSettingsService {
    private final NotificationSettingsRepository settingsRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public NotificationSettingsDto getSettings(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        NotificationSettings settings = settingsRepository.findByUser(user)
                .orElseGet(() -> createDefaultSettings(user));

        return toDto(settings);
    }

    @Transactional
    public NotificationSettingsDto updateSettings(UUID userId, NotificationSettingsDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        NotificationSettings settings = settingsRepository.findByUser(user)
                .orElseGet(() -> createDefaultSettings(user));

        // Обновляем настройки
        settings.setEmailNotifications(dto.getEmailNotifications());
        settings.setPushNotifications(dto.getPushNotifications());
        settings.setNewDeals(dto.getNewDeals());
        settings.setDealUpdates(dto.getDealUpdates());
        settings.setMessages(dto.getMessages());
        settings.setReviews(dto.getReviews());

        settings = settingsRepository.save(settings);
        return toDto(settings);
    }

    private NotificationSettings createDefaultSettings(User user) {
        NotificationSettings settings = new NotificationSettings();
        settings.setUser(user);
        settings.setEmailNotifications(true);
        settings.setPushNotifications(false);
        settings.setNewDeals(true);
        settings.setDealUpdates(true);
        settings.setMessages(true);
        settings.setReviews(true);
        return settingsRepository.save(settings);
    }

    private NotificationSettingsDto toDto(NotificationSettings settings) {
        return new NotificationSettingsDto(
                settings.getEmailNotifications(),
                settings.getPushNotifications(),
                settings.getNewDeals(),
                settings.getDealUpdates(),
                settings.getMessages(),
                settings.getReviews()
        );
    }
}