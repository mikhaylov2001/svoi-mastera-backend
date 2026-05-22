package ru.svoi.mastera.backend.dto;

public record ListingCreateDto(
        String title,
        String description,
        Integer price,
        String priceUnit,
        String category,
        String city,
        String addressText,
        String[] photos
) {}