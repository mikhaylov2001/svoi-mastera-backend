package ru.svoi.mastera.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate template;

    public void sendOfferNotification(String customerUserId, String message) {
        template.convertAndSendToUser(customerUserId, "/queue/offers", message);
    }

    public void sendChatNotification(String partnerId, String message) {
        template.convertAndSendToUser(partnerId, "/queue/chat", message);
    }
}
