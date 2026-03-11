package ru.svoi.mastera.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svoi.mastera.backend.dto.ConversationDto;
import ru.svoi.mastera.backend.dto.MessageDto;
import ru.svoi.mastera.backend.dto.SendMessageDto;
import ru.svoi.mastera.backend.dto.UpdateMessageDto;
import ru.svoi.mastera.backend.entity.JobRequest;
import ru.svoi.mastera.backend.entity.Message;
import ru.svoi.mastera.backend.entity.User;
import ru.svoi.mastera.backend.repository.JobRequestRepository;
import ru.svoi.mastera.backend.repository.MessageRepository;
import ru.svoi.mastera.backend.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final JobRequestRepository jobRequestRepository;

    @Transactional
    public MessageDto send(UUID senderId, SendMessageDto dto) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Message msg = new Message();
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setText(dto.getText());
        msg.setRead(false);

        if (dto.getJobRequestId() != null) {
            JobRequest jr = jobRequestRepository.findById(dto.getJobRequestId()).orElse(null);
            msg.setJobRequest(jr);
        }

        msg = messageRepository.save(msg);
        return toDto(msg);
    }

    @Transactional
    public MessageDto update(UUID userId, UUID messageId, UpdateMessageDto dto) {
        if (dto == null || dto.getText() == null || dto.getText().trim().isEmpty()) {
            throw new RuntimeException("Text is required");
        }

        Message msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!msg.getSender().getId().equals(userId)) {
            throw new RuntimeException("You are not sender of this message");
        }

        msg.setText(dto.getText().trim());
        msg = messageRepository.save(msg);
        return toDto(msg);
    }

    @Transactional
    public void deleteMessage(UUID userId, UUID messageId) {
        Message msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!msg.getSender().getId().equals(userId)) {
            throw new RuntimeException("You are not sender of this message");
        }

        messageRepository.delete(msg);
    }

    @Transactional
    public void deleteConversation(UUID userId, UUID partnerId) {
        // ensure user exists (same style as other methods)
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        messageRepository.deleteConversation(userId, partnerId);
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getConversation(UUID userId, UUID partnerId) {
        List<Message> msgs = messageRepository.findConversation(userId, partnerId);
        return msgs.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public void markRead(UUID userId, UUID partnerId) {
        List<Message> msgs = messageRepository.findConversation(userId, partnerId);
        for (Message m : msgs) {
            if (m.getReceiver().getId().equals(userId) && !m.isRead()) {
                m.setRead(true);
                messageRepository.save(m);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<ConversationDto> getConversations(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Message> latest = messageRepository.findLatestPerPartner(userId);

        return latest.stream().map(m -> {
            UUID partnerId = m.getSender().getId().equals(userId)
                    ? m.getReceiver().getId()
                    : m.getSender().getId();
            User partner = m.getSender().getId().equals(userId) ? m.getReceiver() : m.getSender();
            String partnerName = getDisplayName(partner);

            // Count unread from this partner
            long unread = messageRepository.findConversation(userId, partnerId).stream()
                    .filter(msg -> msg.getReceiver().getId().equals(userId) && !msg.isRead())
                    .count();

            return new ConversationDto(
                    partnerId,
                    partnerName,
                    partner.getAvatarUrl(),
                    m.getText(),
                    m.getCreatedAt(),
                    unread
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return messageRepository.countByReceiverAndIsReadFalse(user);
    }

    private String getDisplayName(User user) {
        if (user.getCustomerProfile() != null) return user.getCustomerProfile().getDisplayName();
        if (user.getWorkerProfile() != null) return user.getWorkerProfile().getDisplayName();
        return "Пользователь";
    }

    private MessageDto toDto(Message m) {
        return new MessageDto(
                m.getId(),
                m.getSender().getId(),
                m.getReceiver().getId(),
                getDisplayName(m.getSender()),
                m.getSender().getAvatarUrl(),
                m.getJobRequest() != null ? m.getJobRequest().getId() : null,
                m.getText(),
                m.isRead(),
                m.getCreatedAt()
        );
    }
}