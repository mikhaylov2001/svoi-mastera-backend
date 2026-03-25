package ru.svoi.mastera.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageDto {
    private UUID receiverId;
    private UUID jobRequestId; // optional
    private String text;
    private String attachmentUrl;  // URL файла после загрузки (optional)
    private String attachmentType;
}