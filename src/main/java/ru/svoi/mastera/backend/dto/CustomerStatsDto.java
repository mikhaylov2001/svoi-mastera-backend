package ru.svoi.mastera.backend.dto;

/** Публичная статистика заказчика: отзывы, которые мастера оставили заказчику. */
public record CustomerStatsDto(
        Double averageRating,
        Long reviewsCount
) {}
