package ru.svoi.mastera.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerReviewDto {
    private UUID id;
    private Integer rating;
    private String text;
    /** Имя мастера — автор отзыва о заказчике */
    private String workerName;
    private String workerAvatar;
    /** User id мастера (для ссылки на профиль /workers/{id}) */
    private UUID authorUserId;
    private String authorLastName;
    private Instant createdAt;
}