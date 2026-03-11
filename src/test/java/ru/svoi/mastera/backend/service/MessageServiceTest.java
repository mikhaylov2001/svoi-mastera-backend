package ru.svoi.mastera.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.svoi.mastera.backend.dto.SendMessageDto;
import ru.svoi.mastera.backend.dto.UpdateMessageDto;
import ru.svoi.mastera.backend.entity.JobRequest;
import ru.svoi.mastera.backend.entity.Message;
import ru.svoi.mastera.backend.entity.User;
import ru.svoi.mastera.backend.repository.JobRequestRepository;
import ru.svoi.mastera.backend.repository.MessageRepository;
import ru.svoi.mastera.backend.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JobRequestRepository jobRequestRepository;

    @InjectMocks
    private MessageService messageService;

    @Test
    void sendShouldSaveMessageWithJobRequest() {
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID jobRequestId = UUID.randomUUID();

        User sender = new User(); sender.setId(senderId);
        User receiver = new User(); receiver.setId(receiverId);

        JobRequest jr = new JobRequest(); jr.setId(jobRequestId);

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(jobRequestRepository.findById(jobRequestId)).thenReturn(Optional.of(jr));
        when(messageRepository.save(any(Message.class))).thenAnswer(i -> i.getArgument(0));

        SendMessageDto dto = new SendMessageDto();
        dto.setReceiverId(receiverId);
        dto.setText("Hello");
        dto.setJobRequestId(jobRequestId);

        var saved = messageService.send(senderId, dto);

        assertThat(saved).isNotNull();
        assertThat(saved.text()).isEqualTo("Hello");
        assertThat(saved.jobRequestId()).isEqualTo(jobRequestId);

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void updateShouldNotAllowNotSender() {
        UUID userId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        User sender = new User(); sender.setId(UUID.randomUUID());
        Message msg = new Message(); msg.setId(messageId); msg.setSender(sender); msg.setText("a");

        when(messageRepository.findById(messageId)).thenReturn(Optional.of(msg));

        UpdateMessageDto dto = new UpdateMessageDto();
        dto.setText("newtext");

        assertThrows(RuntimeException.class, () -> messageService.update(userId, messageId, dto));
    }

    @Test
    void getConversationsReturnsList() {
        UUID userId = UUID.randomUUID();
        Message m = new Message();
        UUID partnerId = UUID.randomUUID();
        User sender = new User(); sender.setId(userId);
        User receiver = new User(); receiver.setId(partnerId);
        m.setSender(sender); m.setReceiver(receiver); m.setText("hi");

        when(messageRepository.findConversation(userId, partnerId)).thenReturn(List.of(m));

        var dialogs = messageService.getConversation(userId, partnerId);
        assertThat(dialogs).hasSize(1);
    }
}
