package ru.svoi.mastera.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsDto {
    private Boolean newDeals;
    private Boolean messages;
    private Boolean reviews;
    private Boolean system;
}