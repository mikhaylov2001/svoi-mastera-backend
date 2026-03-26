package ru.svoi.mastera.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "messages")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_request_id")
    private JobRequest jobRequest;

    @Column(nullable = false, length = 4000)
    private String text;

    @Column(nullable = false)
    private boolean isRead = false;

    // URL файла на сервере или base64 данные (для голосовых без файлового сервера)
    @Column(name = "attachment_url", columnDefinition = "TEXT")
    private String attachmentUrl;

    // Тип вложения: "image" | "video" | "voice" | "file" | "location"
    @Column(name = "attachment_type", length = 50)
    private String attachmentType;
}