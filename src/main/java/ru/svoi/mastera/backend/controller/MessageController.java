package ru.svoi.mastera.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.svoi.mastera.backend.dto.ConversationDto;
import ru.svoi.mastera.backend.dto.MessageDto;
import ru.svoi.mastera.backend.dto.SendMessageDto;
import ru.svoi.mastera.backend.service.MessageService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // Send a message
    @PostMapping
    public MessageDto send(@RequestHeader("X-User-Id") UUID userId,
                           @RequestBody SendMessageDto dto) {
        return messageService.send(userId, dto);
    }

    // Get conversation with specific user
    @GetMapping("/with/{partnerId}")
    public List<MessageDto> getConversation(@RequestHeader("X-User-Id") UUID userId,
                                            @PathVariable UUID partnerId) {
        messageService.markRead(userId, partnerId);
        return messageService.getConversation(userId, partnerId);
    }

    // Get all conversations (chat list)
    @GetMapping("/conversations")
    public List<ConversationDto> getConversations(@RequestHeader("X-User-Id") UUID userId) {
        return messageService.getConversations(userId);
    }

    // Get unread count
    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount(@RequestHeader("X-User-Id") UUID userId) {
        return Map.of("count", messageService.getUnreadCount(userId));
    }
}