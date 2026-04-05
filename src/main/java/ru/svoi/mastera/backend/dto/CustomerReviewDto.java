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
    private String workerName;   // кому оставлен отзыв
    private String workerAvatar;
    private Instant createdAt;
}